package annotatorstub.annotator;

import java.io.IOException;
import java.util.*;

import annotatorstub.utils.WATRelatednessComputer;
import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.ScoredAnnotation;
import it.unipi.di.acube.batframework.data.ScoredTag;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.Sa2WSystem;
import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.ProblemReduction;

public class Baseline1Annotator implements Sa2WSystem {
	private static long lastTime = -1;
	private static float threshold = -1f;

    private class SubArray {
        public ArrayList<String> arr;
        public int x;
        public int y;

        SubArray(int x, int y, ArrayList<String> arr){
            this.x = x;
            this.y = y;
            this.arr = arr;
        }
    }

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


	/* To modify */
	public HashSet<ScoredAnnotation> solveSa2W(String text) throws AnnotationException {
		lastTime = System.currentTimeMillis();

		HashSet<ScoredAnnotation> result = new HashSet<>();

        ArrayList<String> words = new ArrayList<>(Arrays.asList(text.split(" ")));
        Queue<SubArray> ranges = new LinkedList<>();
        ranges.add(new SubArray(0, words.size(), words));

        for(int sizeWord = words.size(); sizeWord > 0; sizeWord--){
            Queue<SubArray> nextQueue = new LinkedList<>();
            while(!ranges.isEmpty()){
                SubArray sa = ranges.poll();
                if (sa.y - sa.x < sizeWord){
                    nextQueue.add(sa);
                    continue;
                }
                boolean broke = false;
                for(int i = sa.x; i < sa.y - sizeWord + 1; i++) {
                    String temp = String.join(" ", sa.arr.subList(i, i+sizeWord));
                    int[] ids;
                    try {
                        ids = WATRelatednessComputer.getLinks(temp);
                    } catch (Exception e){
                        System.err.println(e.getMessage());
                        continue;
                    }
                    if (ids.length != 0){
                        double maxProb = -1;
                        int bestId = 0;
                        for (int id : ids){
                            double prob = WATRelatednessComputer.getCommonness(temp, id);
                            if (prob > maxProb) {
                                maxProb = prob;
                                bestId = id;
                            }
                        }
                        /* Add to the set */
                        int index = text.indexOf(temp);
                        result.add(new ScoredAnnotation(index, temp.length(), bestId, (float) maxProb));
                        /* Cut the range and andd the new one to queues */
                        if (i != sa.x) {
                            nextQueue.add(new SubArray(sa.x, i, sa.arr));
                        }
                        if (i != sa.y - sizeWord){
                            ranges.add(new SubArray(i + sizeWord, sa.y, sa.arr));
                        }
                        /* Stop exploring this subArray */
                        broke = true;
                        break;
                    }
                }
                if (!broke) {
                    nextQueue.add(sa);
                }
            }
            /* Update the queue for the next round */
            ranges = nextQueue;
        }

		lastTime = System.currentTimeMillis() - lastTime;
		return result;
    }

	/* To modify */
	public String getName() {
		return "baseline-1 query annotator";
	}
}
