
package annotatorstub.utils;

import java.util.ArrayList;
import java.util.StringJoiner;

import edu.stanford.nlp.ling.CoreLabel;

public class QueryMention
{
    private int start;
    private int end;

    private int numTokens;

    private ArrayList<CoreLabel> tokens;

    public QueryMention(ArrayList<CoreLabel> tokens)
    {
        this.tokens = tokens;

        this.numTokens = this.tokens.size();

        if(numTokens > 0)
        {
            this.start = this.tokens.get(0).beginPosition();
            this.end = this.tokens.get(this.numTokens - 1).endPosition();
        }
        else 
        {
            this.start = -1;
            this.end = -1;
        }
    }

    public int start()
    {
        return this.start;
    }

    public int end()
    {
        return this.end;
    }

    public String normalizedString()
    {
        StringJoiner joiner = new StringJoiner(" ");

        for(CoreLabel token : this.tokens)
        {
            joiner.add(token.value());
        }

        return joiner.toString();
    }

    @Override
    public String toString()
    {
        return this.normalizedString();
    }
}
