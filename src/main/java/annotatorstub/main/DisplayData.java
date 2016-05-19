package annotatorstub.main;

import annotatorstub.annotator.BingPiggyBack;
import annotatorstub.utils.WATRelatednessComputer;
import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.ScoredAnnotation;
import it.unipi.di.acube.batframework.data.ScoredTag;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.datasetPlugins.DatasetBuilder;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

/**
 * Created by julien on 18/04/16.
 */
public class DisplayData {
    public static void main(String[] args) throws Exception {
		WikipediaApiInterface api = WikipediaApiInterface.api();
		try {
			WATRelatednessComputer.setCache("relatedness.cache");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

        A2WDataset ds = DatasetBuilder.getGerdaqDevel();
        List<HashSet<Annotation>> goldS = ds.getA2WGoldStandardList();
        List<String> examples = ds.getTextInstanceList();

		BingPiggyBack.setJsonCaching();

		double minScore = 2;

		for(int i = 0; i < examples.size(); ++i){
		    String text = examples.get(i);
			HashSet<Annotation> tags = goldS.get(i);
			for(Annotation a : tags) {

				String temp = text.substring(
								a.getPosition(),
								a.getPosition()
										+ a.getLength());

				BingPiggyBack binger = new BingPiggyBack();
				binger.query(temp);

				// compute the entities from the correct spelling
				String correctedSpelling = binger.getSpellingSuggestion();
				double score = WATRelatednessComputer.getCommonness(correctedSpelling, a.getConcept());
				if (score > 0.0 && score < minScore) minScore = score;
				System.out.printf(
						"\t%s[%s] (%d, %d) -> %s (%d) (score=%.3f)%n",
						temp,
						correctedSpelling,
						a.getPosition(),
						a.getPosition()
								+ a.getLength(), api.getTitlebyId(a
								.getConcept()), a.getConcept(),
						score);
			}

		}
		System.out.println(minScore);
    }
}
