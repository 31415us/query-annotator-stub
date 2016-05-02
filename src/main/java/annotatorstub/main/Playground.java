
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

        ///*
        NaiveSegmenter seg = new NaiveSegmenter();

        for(ArrayList<NaiveSegmenter.SegmentationToken> segmentation : seg.candidateSegmentations(4))
        {
            for(NaiveSegmenter.SegmentationToken t : segmentation)
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
            while(tokenizer.hasNext()) {
                CoreLabel token = tokenizer.next();
                System.out.print("\t");
                System.out.print(token.originalText());
                System.out.print(" " + token.beginPosition() + " " + token.endPosition());
                System.out.print("\n");
            }
            System.out.println();
        }
        //*/
    }
}
