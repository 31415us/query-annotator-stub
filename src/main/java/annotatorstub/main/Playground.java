
package annotatorstub.main;

import java.io.StringReader;
import java.util.*;

import it.unipi.di.acube.batframework.data.*;
import it.unipi.di.acube.batframework.datasetPlugins.*;

import edu.stanford.nlp.process.*;
import edu.stanford.nlp.ling.*;

import annotatorstub.utils.*;

public class Playground
{
	public static void main(String[] args) throws Exception {

        /*
        GERDAQDataset trainA = DatasetBuilder.getGerdaqTrainA();
        GERDAQDataset trainB = DatasetBuilder.getGerdaqTrainB();
        GERDAQDataset devel = DatasetBuilder.getGerdaqDevel();
        GERDAQDataset test = DatasetBuilder.getGerdaqTest();
        //*/

        /*
        NaiveSegmentation seg = new NaiveSegmentation();

        for(ArrayList<NaiveSegmentation.SegmentationToken> segmentation : seg.candidateSegmentations(4))
        {
            for(NaiveSegmentation.SegmentationToken t : segmentation)
            {
                switch(t)
                {
                    case BEGIN:
                        System.out.print("B");
                        break;
                    case INNER:
                        System.out.print("I");
                        break;
                    case OMIT:
                        System.out.print("O");
                        break;
                    default:
                        break;
                }
                System.out.print(" ");
            }
            System.out.println();
        }
        //*/

        /*
        for(String query : trainB.getTextInstanceList()) {
            CoreLabelTokenFactory tokenFac = new CoreLabelTokenFactory();
            StringReader r = new StringReader(query);
            String options = "invertible,normalizeParentheses=false,normalizeOtherBrackets=false";

            PTBTokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(r, tokenFac, options);

            System.out.println(query + ":");
            ArrayList<CoreLabel> l = new ArrayList<CoreLabel>();
            while(tokenizer.hasNext()) {
                CoreLabel token = tokenizer.next();
                l.add(token);
            }

            QueryMention m = new QueryMention(l);

            System.out.print("\t");
            System.out.print(m);
            System.out.print(" ");
            System.out.print(m.start());
            System.out.print(" ");
            System.out.print(m.end());
            System.out.println();
        }
        //*/
        
        NaiveSegmenter segmenter = new NaiveSegmenter();
        
        String dummyQuery = "token1 token2 token3";
        HashSet<ArrayList<QueryMention> > mentions = segmenter.segmentions(dummyQuery);

        System.out.println(dummyQuery + ":");
        for(ArrayList<QueryMention> ms : mentions)
        {
            for(QueryMention m : ms)
            {
                System.out.print(m);
                System.out.print("\t\t");
            }

            System.out.println();
        }
    }
}
