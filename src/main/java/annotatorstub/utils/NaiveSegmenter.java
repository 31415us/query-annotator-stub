
package annotatorstub.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.io.StringReader;

import edu.stanford.nlp.process.*;
import edu.stanford.nlp.ling.*;

public class NaiveSegmenter
{
    private NaiveSegmentation segmentation;
    private CoreLabelTokenFactory labelFac;
    private String tokenizerOptions;

    public NaiveSegmenter()
    {
        this.segmentation = new NaiveSegmentation();
        this.labelFac = new CoreLabelTokenFactory();
        this.tokenizerOptions =
            "invertible,normalizeParentheses=false,normalizeOtherBrackets=false";
    }

    public HashSet<ArrayList<QueryMention> > segmentions(String query)
    {
        HashSet<ArrayList<QueryMention> > result =
            new HashSet<ArrayList<QueryMention> >();

        ArrayList<CoreLabel> tokens = this.tokenize(query);

        int numTokens = tokens.size();

        HashSet<ArrayList<NaiveSegmentation.SegmentationToken> > segs =
            this.segmentation.candidateSegmentations(numTokens);

        for(ArrayList<NaiveSegmentation.SegmentationToken> seg : segs)
        {
            result.add(this.mentionsInSeg(seg, tokens));
        }

        return result;
    }

    private ArrayList<QueryMention> mentionsInSeg(
            ArrayList<NaiveSegmentation.SegmentationToken> segmentation,
            ArrayList<CoreLabel> tokens)
    {
        ArrayList<QueryMention> result = new ArrayList<QueryMention>();

        int length = segmentation.size();

        ArrayList<CoreLabel> tokensInCurrentMention = null;
        for(int i = 0; i < length; ++i)
        {
            CoreLabel token = tokens.get(i);
            NaiveSegmentation.SegmentationToken segmentType = segmentation.get(i);

            switch(segmentType)
            {
                case BEGIN:
                    if(tokensInCurrentMention != null)
                    {
                        result.add(new QueryMention(tokensInCurrentMention));
                    }
                    tokensInCurrentMention = new ArrayList<CoreLabel>();
                    tokensInCurrentMention.add(token);
                    break;
                case INNER:
                    tokensInCurrentMention.add(token);
                    break;
                case OMIT:
                    break;
                default:
                    break;
            }
        }

        if(tokensInCurrentMention != null)
        {
            result.add(new QueryMention(tokensInCurrentMention));
        }

        return result;
    }

    private ArrayList<CoreLabel> tokenize(String query)
    {
        ArrayList<CoreLabel> result = new ArrayList<CoreLabel>();

        StringReader r = new StringReader(query);

        PTBTokenizer<CoreLabel> tokenizer =
            new PTBTokenizer<CoreLabel>(r, this.labelFac, this.tokenizerOptions);

        while(tokenizer.hasNext())
        {
            result.add(tokenizer.next());
        }

        return result;
    }
}
