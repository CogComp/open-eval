package actors;

import java.util.List;

import actors.Messages.*;
import akka.actor.Props;
import akka.actor.UntypedActor;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import models.Job;
import controllers.Core;
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
			SetUpJobMessage jobInfo = (SetUpJobMessage) message;
			this.conf_id = jobInfo.getConf_id();
			this.record_id = jobInfo.getRecord_id();
			this.url = jobInfo.getUrl();
			Job job = Core.setUpJob(conf_id, url, record_id);

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
			} catch (Exception ex) {
				System.out.println("Error sending and receiving text annotations");
			}
			Core.storeResultsOfRunInDatabase(job, record_id, conf_id);
			System.out.println("Done");
		} else
			unhandled(message);
	}

}
