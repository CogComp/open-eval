package actors;

import actors.Messages.*;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import controllers.Core;
import models.Job;

public class MasterActor extends UntypedActor {

	public static Props props = Props.create(MasterActor.class);
	
	int completed;
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
            Job job = Core.setUpJob(conf_id, jobInfo.getUrl(), record_id);
            ActorRef jobProcessor = this.getContext().actorOf(JobProcessingActor.props);
            jobProcessor.tell(new StartJobMessage(job), getSelf());
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
        }
		// When the JobProcessingActor is finished, it sends a message to the master
		// to let it know to start storing the results in the database.
		else if (message instanceof FinishedMessage) {
        	FinishedMessage finishedMessage = (FinishedMessage) message;
        	Job finishedJob = finishedMessage.getJob();
        	Core.storeResultsOfRunInDatabase(finishedJob, record_id, conf_id);
        } else {
            System.out.println("unhandled message"+message.toString());
            unhandled(message);
        }
	}

}
