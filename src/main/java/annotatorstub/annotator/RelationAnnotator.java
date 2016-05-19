package annotatorstub.annotator;

import java.io.BufferedReader;
import java.io.FileReader;
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

import static java.lang.Math.log;
import static java.lang.Math.pow;

public class RelationAnnotator implements Sa2WSystem {
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
            return -Double.compare(tuple.prob, t1.prob);
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
        if (stopwords.size() == 0)
            addStopwords();

        try {
            WATRelatednessComputer.setCache("relatedness.cache");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        HashSet<ScoredAnnotation> result = new HashSet<>();

        Comparator<Tuple> comparator = new TupleComparator();
        PriorityQueue<Tuple> queue = new PriorityQueue<>(comparator);
        ArrayList<String> words = new ArrayList<>(Arrays.asList(text.split(" ")));
        HashSet<Integer> toExclude = new HashSet<>();

        BingPiggyBack.setJsonCaching();

        for(int start = 0; start < words.size(); ++start) {
            HashSet<ScoredAnnotation> result_temp = bestCut(words, start, text);
            if (evaluateResult(result) < evaluateResult(result_temp)) {
                result = result_temp;
            }
        }

        lastTime = System.currentTimeMillis() - lastTime;
        return result;
    }

    private double evaluateResult(HashSet<ScoredAnnotation> result){
        double score = 1;
        if (result.size() == 0) return 0;
        for (ScoredAnnotation sa : result) {
            score += sa.getScore();
        }
        return score;
    }

    private HashSet<ScoredAnnotation> bestCut(ArrayList<String> words, int nbrCuts, String text) {
        return bestCutRec(words, nbrCuts, 0, -1, text, 1.);
    }

    static HashSet<String> stopwords = new HashSet();

    private static void addStopwords()
    {
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader("stopwords_en.txt"));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }
        try {
            while(br.ready())
            {
                stopwords.add(br.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String removeStop(String str){
        for (String word : stopwords){
            str  = str.replaceAll(word,"");
        }
        return str;
    }

    private double relatedness = 1.;

    private HashSet<ScoredAnnotation> bestCutRec(ArrayList<String> words, int nbrCuts, int start, int lastId, String text, double modif) {
        HashSet<ScoredAnnotation> result = new HashSet<>();
        if (start == words.size() || nbrCuts < 0)
            return result;
        else if (nbrCuts == 0) {
            HashSet<ScoredAnnotation> result_temp = new HashSet<>();
            String temp = String.join(" ", words.subList(start, words.size()));
            int index = text.indexOf(temp);
            double maxProb = -1;
            int bestId = 0;
            int[] ids;

            BingPiggyBack binger = new BingPiggyBack();
            binger.query(temp);

            // compute the entities from the correct spelling
            String correctedSpelling = binger.getSpellingSuggestion();
            System.out.println(temp + " super corrected to " + correctedSpelling);

            try {
                //ids = WATRelatednessComputer.getLinks(temp.replaceAll("[^a-zA-Z0-9 ]", ""));
                ids = WATRelatednessComputer.getLinks(correctedSpelling);
            } catch (Exception e){
                System.err.println(e.getMessage());
                return result;
            }
            if (lastId == -1) {
                if (ids.length != 0) {
                    for (int id : ids) {
                        double prob = WATRelatednessComputer.getCommonness(correctedSpelling, id) * WATRelatednessComputer.getLp(correctedSpelling);
                        if (prob > maxProb) {
                            maxProb = prob;
                            bestId = id;
                        }
                    }
                }
            } else {
                if (ids.length != 0) {
                    for (int id : ids) {
                        //May be too strong
                        double prob = WATRelatednessComputer.getCommonness(correctedSpelling, id) *
                                (relatedness * WATRelatednessComputer.getJaccardRelatedness(id, lastId) + (1-relatedness) * WATRelatednessComputer.getLp(correctedSpelling)) *
                                modif;
                        if (prob > maxProb) {
                            maxProb = prob;
                            bestId = id;
                        }
                    }
                }
            }
            if (bestId != 0) {
                result_temp.add(new ScoredAnnotation(index, temp.length(), bestId, (float) maxProb));
            }
            if (evaluateResult(result) < evaluateResult(result_temp)) {
                result = result_temp;
            }

        } else {
            for (int i = start + 1; i <= words.size() - nbrCuts; i++){
                HashSet<ScoredAnnotation> result_temp = new HashSet<>();
                HashSet<ScoredAnnotation> result_temp2 = new HashSet<>();
                HashSet<ScoredAnnotation> result_temp3 = new HashSet<>();
                String temp = String.join(" ", words.subList(start, i));
                int index = text.indexOf(temp);
                double maxProb = -1;
                int bestId = 0;
                int[] ids;

                BingPiggyBack binger = new BingPiggyBack();
                binger.query(temp);

                // compute the entities from the correct spelling
                String correctedSpelling = binger.getSpellingSuggestion();
                System.out.println(temp + " corrected to " + correctedSpelling);

                try {
                    //ids = WATRelatednessComputer.getLinks(temp.replaceAll("[^a-zA-Z0-9 ]", ""));
                    ids = WATRelatednessComputer.getLinks(correctedSpelling);
                } catch (Exception e){
                    System.err.println(e.getMessage());
                    continue;
                }
                if (lastId == -1) {
                    if (ids.length != 0) {
                        for (int id : ids) {
                            double prob = WATRelatednessComputer.getCommonness(correctedSpelling, id) * WATRelatednessComputer.getLp(correctedSpelling);
                            if (prob > maxProb) {
                                maxProb = prob;
                                bestId = id;
                            }
                        }
                    }
                } else {
                    if (ids.length != 0) {
                        for (int id : ids) {
                            double prob = WATRelatednessComputer.getCommonness(correctedSpelling, id) *
                                 (relatedness * WATRelatednessComputer.getJaccardRelatedness(id, lastId) + (1-relatedness) * WATRelatednessComputer.getLp(correctedSpelling)) *
                                    modif;
                            if (prob > maxProb) {
                                maxProb = prob;
                                bestId = id;
                            }
                        }
                    }
                }
                if (bestId != 0) {
                    result_temp.add(new ScoredAnnotation(index, temp.length(), bestId, (float) maxProb));
                    result_temp.addAll(bestCutRec(words, nbrCuts - 1, i, bestId, text, modif));
                    result_temp2.addAll(bestCutRec(words, nbrCuts - 1, i, lastId, text, modif));
                    if (lastId != -10) {
                        result_temp3.add(new ScoredAnnotation(index, temp.length(), bestId, (float) maxProb));
                        result_temp3.addAll(bestCutRec(words, nbrCuts - 1, i, lastId, text, modif));
                    }
                    if (evaluateResult(result_temp) < evaluateResult(result_temp2)) {
                        result_temp = result_temp2;
                    }
                    if (evaluateResult(result_temp) < evaluateResult(result_temp3)) {
                        result_temp = result_temp3;
                    }
                } else {
                    result_temp.addAll(bestCutRec(words, nbrCuts - 1, i, lastId, text, modif));
                }
                if (evaluateResult(result) < evaluateResult(result_temp)) {
                    result = result_temp;
                }
            }
        }
        return result;
    }
    /* To modify */
    public String getName() {
        return "relation query annotator";
    }
}

