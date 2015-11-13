package controllers.io;

import java.util.ArrayList;
import java.util.List;

import controllers.ToyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.IResetableIterator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

public class DatabaseCommunication {

	private final String[] datasetNames = {"toyDataset"};
	private TextAnnotationDBHandler dbHandler;
	
	/** Constructor. Initiates database communication */
	public DatabaseCommunication() {
		this.dbHandler = new TextAnnotationDBHandler("./datasets", datasetNames);
	}
	
	/** Queries the database for a dataset by name */
	public List<TextAnnotation> retrieveDataset(String datasetName) {
		IResetableIterator<TextAnnotation> data = dbHandler.getDataset(datasetName);
		List<TextAnnotation> dataset = new ArrayList<>();
		while (data.hasNext()) {
			dataset.add(data.next());
		}
		return dataset;
	}
	
	/** Stores the specified text annotations in the database */
	public void storeDataset(String datasetName, List<TextAnnotation> textAnnotations) {
		for (TextAnnotation textAnnotation : textAnnotations) {
			dbHandler.addTextAnnotation(datasetName, textAnnotation);
		}
	}

	
}
