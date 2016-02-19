package actors;

import java.util.List;

import actors.Messages.*;
import akka.actor.Props;
import akka.actor.UntypedActor;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import models.Job;
import play.libs.ws.WSResponse;

public class JobProcessingActor extends UntypedActor {

	public static Props props = Props.create(JobProcessingActor.class);
	
	private int completed;
	private int total;
	private int skipped;	

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof StartJobMessage) {

			StartJobMessage jobInfo = (StartJobMessage) message;			
			Job job = jobInfo.getJob();
			List<TextAnnotation> unprocessedInstances = job.getUnprocessedInstances();
			completed = 0;
			skipped = 0;
			total = unprocessedInstances.size();

			System.out.println("Created Job Processor Worker");
            System.out.println("Sending and recieving annotations:");
            try {
        		for (TextAnnotation ta : unprocessedInstances) {
        			job.sendAndReceiveRequestFromSolver(ta);
        			completed++;
        			getSender().tell(new StatusUpdate(completed, skipped, total), getSelf());
        		}
        		getSender().tell(new FinishedMessage(job), getSelf());
            } catch (Exception ex) {
            	System.out.println("Error sending and receiving text annotations");
            	getSender().tell(new FinishedMessage(job), getSelf());
            }
            System.out.println("Done");
        } else
            unhandled(message);
	}

}
