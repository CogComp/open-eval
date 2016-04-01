package actors;

import java.util.ArrayList;
import java.util.List;

import actors.Messages.*;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
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

public class JobProcessingActor extends UntypedActor {

    public static Props props = Props.create(JobProcessingActor.class);

    private int completed;
    private int total;
    private int skipped;
    private String conf_id;
    private String record_id;
    private String url;

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
            Evaluator evaluator = Core.getEvaluator(conf_id);
            ClassificationTester eval = new ClassificationTester();
            List<TextAnnotation> unprocessedInstances = job.getUnprocessedInstances();
            List<TextAnnotation> goldInstances = job.getGoldInstances();
            completed = 0;
            skipped = 0;
            total = unprocessedInstances.size();

            System.out.println("Created Job Processor Worker");
            System.out.println("Sending and recieving annotations:");
            try {
                int maxBatchSize = learnerSettings.maxNumInstancesAccepted;
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
                                    Core.evaluate(evaluator, eval, goldInstance, learnerInstancesResponse.textAnnotations[batchIndex]);
                                    completed++;
                                } else {
                                    skipped++;
                                }
                                if(completed+skipped < total)
                                    Core.storeResultsOfRunInDatabase(eval, record_id, true);
                                else
                                    Core.storeResultsOfRunInDatabase(eval, record_id, false);
                            }
                            master.tell(new StatusUpdate(completed, skipped, total), getSelf());
                            System.out.println(String.format("Completed batch of size %s", batchSize));
                        }
                    });

                    response.get(30000);
                }
            } catch (Exception ex) {
                System.out.println("Error sending and receiving text annotations");
                Core.storeResultsOfRunInDatabase(eval, record_id, false);
            }
            System.out.println("Done");
        } else
            unhandled(message);
    }

    private List<TextAnnotation> makeBatch(List<TextAnnotation> unprocessedInstances, int startIndex, int batchSize){
        List<TextAnnotation> batch = new ArrayList<>();
        for (int i=0;i<batchSize;i++){
            batch.add(unprocessedInstances.get(startIndex + i));
        }
        return batch;
    }

}
