package actors;

import java.util.ArrayList;
import java.util.List;

import actors.Messages.*;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.util.Timeout;
import akka.actor.UntypedActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
import edu.illinois.cs.cogcomp.core.experiments.evaluators.Evaluator;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import models.Job;
import controllers.Core;
import models.LearnerInstancesResponse;
import models.LearnerSettings;
import play.libs.F;
import play.libs.F.Promise;
import play.libs.ws.WSResponse;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import static akka.pattern.Patterns.ask;


public class JobProcessingActor extends UntypedActor {

    public static Props props = Props.create(JobProcessingActor.class);

    private int completed;
    private int total;
    private int skipped;
    private int learnerTimeout;
    private String conf_id;
    private String record_id;
    private String url;

    Config conf;

    public JobProcessingActor() {
        conf = ConfigFactory.load();
        learnerTimeout = conf.getInt("learner.default.timeout");
    }

    /**
     * When a StartJobMessage is received, the corresponding Job is extracted
     * and the unprocessed instances are sent and received one at a time to the
     * solver. Every time a text annotation is processed, JobProcessingActor
     * notifies its parent, the MasterActor, of its progress.
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof SetUpJobMessage) {
            ActorRef master = getSender();
            SetUpJobMessage jobInfo = (SetUpJobMessage) message;
            this.conf_id = jobInfo.getConf_id();
            this.record_id = jobInfo.getRecord_id();
            this.url = jobInfo.getUrl();
            LearnerSettings learnerSettings = jobInfo.getLearnerSettings();
            Job job = Core.setUpJob(conf_id, url, record_id);
            ClassificationTester eval = new ClassificationTester();
            Evaluator evaluator;
            String viewName;
            try {
                evaluator = Core.getEvaluator(conf_id);
                viewName = Core.getEvaluatorView(conf_id);
            }
            catch(Exception e){
                master.tell(new StatusUpdate(0, 0, 0, record_id, eval, "Error receiving evaluator from database"), getSelf());
                Core.storeResultsOfRunInDatabase(eval, record_id, false);
                return;
            }
            if(job.getError() != null){
                master.tell(new StatusUpdate(0, 0, 0, record_id, eval, job.getError()), getSelf());
                Core.storeResultsOfRunInDatabase(eval, record_id, false);
                return;
            }
            List<TextAnnotation> unprocessedInstances = job.getUnprocessedInstances();
            List<TextAnnotation> goldInstances = job.getGoldInstances();
            completed = 0;
            skipped = 0;
            total = unprocessedInstances.size();
            master.tell(new StatusUpdate(completed, skipped, total, record_id, eval, null), getSelf());

            System.out.println("Created Job Processor Worker");
            System.out.println("Sending and recieving annotations:");
            try {
                int maxBatchSize = learnerSettings.maxNumInstancesAccepted;
                int killCheckCounter = 1;
                for (int startIndex = 0; startIndex < unprocessedInstances.size(); startIndex+=maxBatchSize) {
                    int batchSize = Math.min(maxBatchSize, unprocessedInstances.size() - startIndex);
                    List<TextAnnotation> batch = makeBatch(unprocessedInstances, startIndex, batchSize);
                    Promise<LearnerInstancesResponse> response = job.sendAndReceiveRequestsFromSolver(batch);

                    int batchStartIndex = startIndex;
                    response.onRedeem(new F.Callback<LearnerInstancesResponse>() {
                        @Override
                        public void invoke(LearnerInstancesResponse learnerInstancesResponse) throws Throwable {
                            for(int batchIndex = 0;batchIndex<batchSize;batchIndex++){
                                if (learnerInstancesResponse.textAnnotations[batchIndex] != null){
                                    TextAnnotation goldInstance = goldInstances.get(batchStartIndex + batchIndex);
                                    Core.evaluate(evaluator, eval, goldInstance, learnerInstancesResponse.textAnnotations[batchIndex], viewName);
                                    completed++;
                                } else {
                                    skipped++;
                                }
                            }

                            if(completed+skipped < total)
                                Core.storeResultsOfRunInDatabase(eval, record_id, true);
                            else
                                Core.storeResultsOfRunInDatabase(eval, record_id, false);

                            master.tell(new StatusUpdate(completed, skipped, total, record_id, eval, null), getSelf());
                            System.out.println(String.format("Completed batch of size %s", batchSize));
                        }
                    });
                    response.get(learnerTimeout);
                    if (killCheckCounter == 5) {
	                    if (killCommandHasBeenSent()) {
                            System.out.println("Exiting");
                            Core.storeResultsOfRunInDatabase(eval, record_id, false);
                            break;
                        }
                        killCheckCounter = 1;
                    } else {
                        killCheckCounter++;
                    }
                }
            } catch (Exception ex) {
                System.out.println("Err sending and receiving text annotations" + ex.getMessage());
                master.tell(new StatusUpdate(completed, skipped, total, record_id, eval, "Error receiving and sending text annotations"), getSelf());
                Core.storeResultsOfRunInDatabase(eval, record_id, false);
            }
            System.out.println("Done");
        } else
            unhandled(message);
    }

    private boolean killCommandHasBeenSent() throws Exception {
        Timeout timeout = new Timeout(learnerTimeout);
        Future<Object> masterResponse = ask(getSender(), new KillStatus(false, record_id), 5000);
        Object result = (Object) Await.result(masterResponse, timeout.duration());
        return (result instanceof KillStatus);
    }

    private List<TextAnnotation> makeBatch(List<TextAnnotation> unprocessedInstances, int startIndex, int batchSize){
        List<TextAnnotation> batch = new ArrayList<>();
        for (int i=0;i<batchSize;i++){
            batch.add(unprocessedInstances.get(startIndex + i));
        }
        return batch;
    }

}
