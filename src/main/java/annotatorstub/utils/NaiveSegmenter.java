
package annotatorstub.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

public class NaiveSegmenter
{
    public enum SegmentationToken
    {
        BEGIN,
        INNER,
        OMIT
    }

    private HashMap<Integer, HashSet<ArrayList<SegmentationToken> > > cache;

    public NaiveSegmenter()
    {
        this.cache = new HashMap<Integer, HashSet<ArrayList<SegmentationToken> > >();

        HashSet<ArrayList<SegmentationToken> > zeroLength = new HashSet<ArrayList<SegmentationToken> >();
        zeroLength.add(new ArrayList<SegmentationToken>());
        this.cache.put(0, zeroLength);

        HashSet<ArrayList<SegmentationToken> > oneLength = new HashSet<ArrayList<SegmentationToken> >();
        ArrayList<SegmentationToken> onlyB = new ArrayList<SegmentationToken>();
        onlyB.add(SegmentationToken.BEGIN);
        oneLength.add(onlyB);
        ArrayList<SegmentationToken> onlyO = new ArrayList<SegmentationToken>();
        onlyO.add(SegmentationToken.OMIT);
        oneLength.add(onlyO);
        this.cache.put(1, oneLength);
    }

    public HashSet<ArrayList<SegmentationToken> > candidateSegmentations(int length)
    {
        updateCache(length);

        return this.cache.get(length);
    }

    private void updateCache(int length)
    {
        if(!this.cache.containsKey(length))
        {
            if(!this.cache.containsKey(length - 1))
            {
                this.updateCache(length - 1);
            }

            HashSet<ArrayList<SegmentationToken> > prev = this.cache.get(length - 1);

            HashSet<ArrayList<SegmentationToken> > result = new HashSet<ArrayList<SegmentationToken> >();

            for(ArrayList<SegmentationToken> list : prev)
            {
                ArrayList<SegmentationToken> addO = new ArrayList<SegmentationToken>(list);
                addO.add(SegmentationToken.OMIT);
                result.add(addO);

                ArrayList<SegmentationToken> addB = new ArrayList<SegmentationToken>(list);
                addB.add(SegmentationToken.BEGIN);
                result.add(addB);

                if(list.indexOf(SegmentationToken.BEGIN) != -1)
                {
                    ArrayList<SegmentationToken> addI = new ArrayList<SegmentationToken>(list);
                    addI.add(SegmentationToken.INNER);
                    result.add(addI);
                }
            }

            this.cache.put(length, result);
        }
    }
}
