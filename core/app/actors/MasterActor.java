package actors;

import java.util.HashMap;
import java.util.Map;

import actors.Messages.*;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import controllers.Core;
import models.Job;
import models.RunStatus;

public class MasterActor extends UntypedActor {

    public static Props props = Props.create(MasterActor.class);
    
    /**
     * Maps record_id to the current run status.
     */
    private Map<String, RunStatus> runStatuses = new HashMap();
    String conf_id;
    String record_id;

    @Override
    public void onReceive(Object message) throws Exception {
        // When told to start a job, MasterActor spawns a JobProcessingActor to
        // carry out the job.
        if (message instanceof SetUpJobMessage) {
            RunStatus runStatus = new RunStatus(0, 0, 0, "", null);
            SetUpJobMessage jobInfo = (SetUpJobMessage) message;
            this.conf_id = jobInfo.getConf_id();
            this.record_id = jobInfo.getRecord_id();
            runStatuses.put(jobInfo.getRecord_id(), runStatus);
            ActorRef jobProcessor = this.getContext().actorOf(JobProcessingActor.props);
            jobProcessor.tell(message, getSelf());
        }
        // When the JobProcessingActor has finished processing another
        // TextAnnotation, it
        // updates the master.
        else if (message instanceof StatusUpdate) {
            StatusUpdate update = (StatusUpdate) message;
            int completed = update.getCompleted();
            int skipped = update.getSkipped();
            int total = update.getTotal();
            runStatuses.put(update.getRecord_id(), new RunStatus(completed, skipped, total, update.getError(), update.getEvaluation()));
        }
        // When the progress bar page polls for an updated status, Master
        // returns the
        // number of completed, skipped, and total TextAnnotations.
        else if (message instanceof StatusRequest) {
            String id = ((StatusRequest) message).getRecord_id();
            RunStatus status = runStatuses.get(id);
            getSender().tell(new StatusUpdate(status.getCompleted(), status.getSkipped(), status.getTotal(), id, status.getError(), status.getEvaluation()), getSelf());
        } else {
            System.out.println("unhandled message" + message.toString());
            unhandled(message);
        }
    }

}
