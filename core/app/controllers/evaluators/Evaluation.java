package controllers.evaluators;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.experiments.EvaluationRecord;

import java.util.*;

public class Evaluation {

	// evaluation over all of the instances
	private EvaluationRecord macroEvaluationRecord;

	// evaluation over single instances
	private List<EvaluationRecord> microEvaluationRecords;

	public Evaluation(List<EvaluationRecord> microEvaluationRecords) {
		this.microEvaluationRecords = microEvaluationRecords;
	}

	public Evaluation(EvaluationRecord macroEvaluationRecord) {
		this.macroEvaluationRecord = macroEvaluationRecord;
	}

	public Evaluation(EvaluationRecord macroEvaluationRecord, List<EvaluationRecord> microEvaluationRecords) {
		this.macroEvaluationRecord = macroEvaluationRecord;
		this.microEvaluationRecords = microEvaluationRecords;
	}
    
    public EvaluationRecord getMacroEvaluationRecord() {
        return macroEvaluationRecord;
    }
}