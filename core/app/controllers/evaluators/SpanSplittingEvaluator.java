package controllers.evaluators;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.experiments.EvaluationRecord;

import java.util.*;

public class SpanSplittingEvaluator extends Evaluator {

	private String spanViewName;
	
	public SpanSplittingEvaluator(String spanViewName) {
		super();
		this.spanViewName = spanViewName;
	}

    public void setSpanViewName(String spanViewName) {
        this.spanViewName = spanViewName;
    }

	public Evaluation evaluate(List<TextAnnotation> correctAnnotations, List<TextAnnotation> solverAnnotations) {
        assert( correctAnnotations.size() == solverAnnotations.size() );

        EvaluationRecord macroEvaluationRecord = new EvaluationRecord();
        List<EvaluationRecord> microEvaluationRecords = new ArrayList<>();

        for( int i = 0; i < correctAnnotations.size(); i++) {
            View correctVu = correctAnnotations.get(i).getView(spanViewName);
            View solverVu = solverAnnotations.get(i).getView(spanViewName);

            Set<IntPair> correctSpans = new HashSet<>();
            for(Constituent cons : correctVu.getConstituents() ) {
                correctSpans.add(cons.getSpan());
            }

            Set<IntPair> solverSpans = new HashSet<>();
            for(Constituent cons : solverVu.getConstituents() ) {
                solverSpans.add(cons.getSpan());
            }

            Set<IntPair> spanIntersection = new HashSet<>(correctSpans);
            spanIntersection.retainAll(solverSpans);

            EvaluationRecord microEvaluationRecord = new EvaluationRecord();
            microEvaluationRecord.incrementCorrect(spanIntersection.size());
            microEvaluationRecord.incrementGold(correctSpans.size());
            microEvaluationRecord.incrementPredicted(solverSpans.size());
            microEvaluationRecords.add(microEvaluationRecord);

            macroEvaluationRecord.incrementCorrect(spanIntersection.size());
            macroEvaluationRecord.incrementGold(correctSpans.size());
            macroEvaluationRecord.incrementPredicted(solverSpans.size());
        }

        return new Evaluation(macroEvaluationRecord, microEvaluationRecords);
	}
}