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

public class BestProbaFirstAnnotator implements Sa2WSystem {
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

    private class Tuple {
        public SubArray sa;
        public double prob;
        public int id;

        Tuple(SubArray sa, double prob, int id){
            this.sa = sa;
            this.prob = prob;
            this.id = id;
        }
    }

    private class TupleComparator implements Comparator<Tuple> {

        @Override
        public int compare(Tuple tuple, Tuple t1) {
            if (tuple.prob > t1.prob) return 1;
            return 0;
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

        Comparator<Tuple> comparator = new TupleComparator();
        PriorityQueue<Tuple> queue = new PriorityQueue<>(comparator);
        ArrayList<String> words = new ArrayList<>(Arrays.asList(text.split(" ")));
        HashSet<Integer> toExclude = new HashSet<>();

        for(int start = 0; start < words.size(); ++start) {
            for (int end = words.size(); end > start; --end) {
                String temp = String.join(" ", words.subList(start, end));
                double maxProb = -1;
                int bestId = 0;
                int[] ids;
                try {
                    ids = WATRelatednessComputer.getLinks(temp);
                } catch (Exception e){
                    System.err.println(e.getMessage());
                    continue;
                }
                if (ids.length != 0) {
                    for (int id : ids) {
                        double prob = WATRelatednessComputer.getCommonness(temp, id);
                        if (prob > maxProb) {
                            maxProb = prob;
                            bestId = id;
                        }
                    }
                }
                queue.add(new Tuple(new SubArray(start, end, words), maxProb, bestId));
            }
        }

        while (!queue.isEmpty()){
            Tuple current = queue.poll();
            SubArray sa = current.sa;
            boolean broke = false;
            for (int i = sa.x; i < sa.y; i++){
                if(toExclude.contains(i)) {
                    broke = true;
                    break;
                }
            }
            if (broke) continue;
            if (current.id == 0) continue;
            for (int i = sa.x; i < sa.y; i++){
                toExclude.add(i);
            }
            String temp = String.join(" ", sa.arr.subList(sa.x, sa.y));
            int index = text.indexOf(temp);
            result.add(new ScoredAnnotation(index, temp.length(), current.id, (float) current.prob));
        }

        lastTime = System.currentTimeMillis() - lastTime;
        return result;
    }

    /* To modify */
    public String getName() {
        return "baseline-1 query annotator";
    }
}

