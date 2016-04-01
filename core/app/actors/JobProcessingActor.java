package actors;

import java.net.ConnectException;
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
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
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
    public void onReceive(Object message){
        if (message instanceof SetUpJobMessage) {
            ActorRef master = getSender();
            SetUpJobMessage jobInfo = (SetUpJobMessage) message;
            this.conf_id = jobInfo.getConf_id();
            this.record_id = jobInfo.getRecord_id();
            this.url = jobInfo.getUrl();
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
            for (int i = 0; i < unprocessedInstances.size(); i++) {
                Promise<WSResponse> response;
                try {
                    response = job.sendAndReceiveRequestFromSolver(unprocessedInstances.get(i));
                } catch(ConnectException e){
                    System.out.println("ERROR:"+e);
                    Core.storeResultsOfRunInDatabase(eval, record_id, false);
                    skipped+=total-i;
                    master.tell(new StatusUpdate(completed, skipped, total), getSelf());
                    break;
                }
                if(response == null){
                    skipped+=total-i;
                    master.tell(new StatusUpdate(completed, skipped, total), getSelf());
                    break;
                }
                TextAnnotation goldInstance = goldInstances.get(i);
                response.onFailure(new F.Callback<Throwable>(){
                    @Override
                    public void invoke(Throwable error) {
                        System.out.println("ERROR ERROR ERROR");
                        skipped++;
                        master.tell(new StatusUpdate(completed, skipped, total), getSelf());
                        if (completed + skipped >= total)
                            Core.storeResultsOfRunInDatabase(eval, record_id, false);
                    }
                });
                System.out.println("INITIALIZED ERROR CHECK");
                response.onRedeem(new F.Callback<WSResponse>() {
                    @Override
                    public void invoke(WSResponse wsResponse) throws Throwable {
                        TextAnnotation predictedInstance;
                        try {
                            String resultJson = wsResponse.getBody();
                            predictedInstance = SerializationHelper.deserializeFromJson(resultJson);
                        } catch (Exception e) {
                            System.out.println(e);
                            skipped++;
                            master.tell(new StatusUpdate(completed, skipped, total), getSelf());
                            if (completed + skipped < total)

                                Core.storeResultsOfRunInDatabase(eval, record_id, true);
                            else
                                Core.storeResultsOfRunInDatabase(eval, record_id, false);
                            return;
                        }
                        Core.evaluate(evaluator, eval, goldInstance, predictedInstance);
                        completed++;
                        master.tell(new StatusUpdate(completed, skipped, total), getSelf());

                        System.out.println("Completed(worker):" + completed);
                        if (completed + skipped < total)
                            Core.storeResultsOfRunInDatabase(eval, record_id, true);
                        else
                            Core.storeResultsOfRunInDatabase(eval, record_id, false);
                    }
                });
                try{
                    response.get(5000);
                }
                catch(Exception e){
                    master.tell(new ErrorMessage("Lost Connection to Learner"), getSelf());
                    break;
                }
            }
            System.out.println("Done");
        } else
            unhandled(message);
    }

}
