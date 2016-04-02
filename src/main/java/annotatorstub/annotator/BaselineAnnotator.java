package annotatorstub.annotator;

import java.io.IOException;
import java.util.HashSet;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.ScoredAnnotation;
import it.unipi.di.acube.batframework.data.ScoredTag;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.Sa2WSystem;
import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.ProblemReduction;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

import annotatorstub.utils.WATRelatednessComputer;

public class BaselineAnnotator implements Sa2WSystem {
	private static long lastTime = -1;
	private static float threshold = -1f;
	
	public long getLastAnnotationTime() {
		return lastTime;
	}

	public HashSet<Tag> solveC2W(String text) throws AnnotationException {
		return ProblemReduction.A2WToC2W(solveA2W(text));
	}

	public HashSet<Annotation> solveD2W(String text, HashSet<Mention> mentions) throws AnnotationException {
		return ProblemReduction.Sa2WToD2W(solveSa2W(text), mentions, threshold);
	}

	public HashSet<Annotation> solveA2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToA2W(solveSa2W(text), threshold);
	}

	public HashSet<ScoredTag> solveSc2W(String text) throws AnnotationException {
	    return ProblemReduction.Sa2WToSc2W(solveSa2W(text));
    }

	public HashSet<ScoredAnnotation> solveSa2W(String text) throws AnnotationException {
		lastTime = System.currentTimeMillis();

		HashSet<ScoredAnnotation> result = new HashSet<>();

        for(int start = 0; start < text.length(); ++start)
        {
            if(!Character.isLetterOrDigit(text.charAt(start))) {
                continue;
            }
            for(int end = text.length(); end > start; --end)
            {
                String query = text.substring(start, end);

                int best = -1;
                double bestScore = -1.0f;
                for(int id : WATRelatednessComputer.getLinks(query))
                {
                    double score = WATRelatednessComputer.getCommonness(query, id);
                    if(score > bestScore){
                        bestScore = score;
                        best = id;
                    }
                }

                if(best != -1)
                {
                    result.add(new ScoredAnnotation(start, end - start, best, (float)bestScore));
                    start = end;
                    break;
                }
            }
        }

		lastTime = System.currentTimeMillis() - lastTime;
		return result;
    }
	
	public String getName() {
		return "Baseline Annotator";
	}
}
