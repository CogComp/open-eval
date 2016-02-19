package actors;

import actors.Messages.*;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import controllers.Core;
import models.Job;

public class MasterActor extends UntypedActor {

	public static Props props = Props.create(MasterActor.class);
	
	int completed = 0;
	int skipped;
	int total = 1;
	String conf_id;
	String record_id;
	
	@Override
	public void onReceive(Object message) throws Exception {
		// When told to start a job, MasterActor spawns a JobProcessingActor to carry out the job.
		if (message instanceof SetUpJobMessage) {
			SetUpJobMessage jobInfo = (SetUpJobMessage) message;
			this.conf_id = jobInfo.getConf_id();
			this.record_id = jobInfo.getRecord_id();
            ActorRef jobProcessor = this.getContext().actorOf(JobProcessingActor.props);
            jobProcessor.tell(message, getSelf());
        } 
		// When the JobProcessingActor has finished processing another TextAnnotation, it
		// updates the master.
		else if (message instanceof StatusUpdate) {
            System.out.println("Got Status Update");
            StatusUpdate update = (StatusUpdate) message;
            completed = update.getCompleted();
            skipped = update.getSkipped();
            total = update.getTotal();
            System.out.println("Completed: " + completed);
            System.out.println("Total: " + total);
        }	
		// When the progress bar page polls for an updated status, Master returns the
		// number of completed, skipped, and total TextAnnotations.
		else if (message instanceof StatusRequest) {
            System.out.println("Got Status Message");
           	getSender().tell(new StatusUpdate(completed, skipped, total), getSelf());
        } else {
            System.out.println("unhandled message"+message.toString());
            unhandled(message);
        }
	}

}
