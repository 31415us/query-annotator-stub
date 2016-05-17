package annotatorstub.main;

import annotatorstub.annotator.Baseline1Annotator;
import annotatorstub.annotator.BestProbaFirstAnnotator;
import annotatorstub.annotator.WorstProbaFirstAnnotator;
import annotatorstub.utils.WATRelatednessComputer;
import it.unipi.di.acube.batframework.cache.BenchmarkCache;
import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.datasetPlugins.DatasetBuilder;
import it.unipi.di.acube.batframework.metrics.Metrics;
import it.unipi.di.acube.batframework.metrics.MetricsResultSet;
import it.unipi.di.acube.batframework.metrics.StrongAnnotationMatch;
import it.unipi.di.acube.batframework.metrics.StrongTagMatch;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.utils.DumpData;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import annotatorstub.annotator.BaselineAnnotator;
import annotatorstub.utils.Utils;

public class BenchmarkMain {
	public static void main(String[] args) throws Exception {
		WikipediaApiInterface wikiApi = WikipediaApiInterface.api();
		try {
			WATRelatednessComputer.setCache("relatedness.cache");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		A2WDataset ds = DatasetBuilder.getGerdaqDevel();
		//Baseline1Annotator ann = new Baseline1Annotator();
		BestProbaFirstAnnotator ann = new BestProbaFirstAnnotator();
		//WorstProbaFirstAnnotator ann = new WorstProbaFirstAnnotator();

		List<HashSet<Tag>> resTag = BenchmarkCache.doC2WTags(ann, ds);
		List<HashSet<Annotation>> resAnn = BenchmarkCache.doA2WAnnotations(ann, ds);
		DumpData.dumpCompareList(ds.getTextInstanceList(), ds.getA2WGoldStandardList(), resAnn, wikiApi);

		Metrics<Tag> metricsTag = new Metrics<>();
		MetricsResultSet C2WRes = metricsTag.getResult(resTag, ds.getC2WGoldStandardList(), new StrongTagMatch(wikiApi));
		Utils.printMetricsResultSet("C2W", C2WRes, ann.getName());

		Metrics<Annotation> metricsAnn = new Metrics<>();
		MetricsResultSet rsA2W = metricsAnn.getResult(resAnn, ds.getA2WGoldStandardList(), new StrongAnnotationMatch(wikiApi));
		Utils.printMetricsResultSet("A2W-SAM", rsA2W, ann.getName());
		
		Utils.serializeResult(ann, ds, new File("annotations.bin"));
		wikiApi.flush();
	}

}
