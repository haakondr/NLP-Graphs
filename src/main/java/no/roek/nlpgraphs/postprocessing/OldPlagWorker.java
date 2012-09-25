//package no.roek.nlpgraphs.postprocessing;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.TimeUnit;
//
//import no.roek.nlpgraphs.algorithm.GraphEditDistance;
//import no.roek.nlpgraphs.document.GraphPair;
//import no.roek.nlpgraphs.document.PlagiarismReference;
//import no.roek.nlpgraphs.graph.Graph;
//import no.roek.nlpgraphs.jobs.PlagJob;
//import no.roek.nlpgraphs.misc.ConfigService;
//import no.roek.nlpgraphs.misc.Fileutils;
//import no.roek.nlpgraphs.misc.GraphUtils;
//
//import org.jdom2.Document;
//import org.jdom2.Element;
//import org.jdom2.output.XMLOutputter;
//
//public class PlagiarismWorker extends Thread {
//
//	private String parsedData, trainDir, testDir, originalDir, resultsDir;
//	private int plagiarismThreshold;
//	private BlockingQueue<PlagJob> queue;
//
//	public PlagiarismWorker(BlockingQueue<PlagJob> queue) {
//		this.queue = queue;
//		this.parsedData = ConfigService.getParsedFilesDir();
//		this.testDir = ConfigService.getTestDir();
//		this.trainDir = ConfigService.getTrainDir();
//		this.resultsDir = ConfigService.getResultsDir();
//		this.plagiarismThreshold = ConfigService.getPlagiarismThreshold();
//	}
//
//	@Override
//	public void run() {
//		boolean run = true;
//		while(run) {
//			try {
//				PlagJob job = queue.poll(2000, TimeUnit.SECONDS);
//				List<PlagiarismReference> plagReferences = findPlagiarism(job.getFile(), job.getSimilarDocuments());
//				writeResults(job.getFile(), plagReferences);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
//
//	public List<PlagiarismReference> findPlagiarism(String file, String[] simDocs) {
//		System.out.println(Thread.currentThread().getName()+": finding plagiarism cases for file "+file);
//		List<PlagiarismReference> references = new ArrayList<>();
//
//		for (String simDoc : simDocs) {
//			for (GraphPair graphPair : findPlagiarisedSentences(parsedData+testDir+file, parsedData+trainDir+simDoc)) {
//				references.add(getPlagiarismReference(graphPair));
//			}
//		}
//
//		return references;
//	}
//
//	public List<GraphPair> findPlagiarisedSentences(String testFile, String trainFile) {
//		List<GraphPair> similarSentences = new ArrayList<>();
//		List<Graph> testSentences = GraphUtils.getGraphs(testFile);
//		List<Graph> trainSentences = GraphUtils.getGraphs(trainFile);
//
//		for (Graph testSentence : testSentences) {
//			for (Graph trainSentence : trainSentences) {
//				GraphEditDistance ged = new GraphEditDistance(testSentence, trainSentence);
//				double dist = ged.getDistance();
//
//				if(dist < plagiarismThreshold) {
//					similarSentences.add(new GraphPair(testSentence, trainSentence, dist));
//				}
//			}
//		}
//
//		return similarSentences;
//	}
//
//	public PlagiarismReference getPlagiarismReference(GraphPair pair) {
//		Graph test = pair.getSuspiciousGraph();
//		Graph train = pair.getSourceGraph();
//		String offset = Integer.toString(test.getOffset());
//		String length = Integer.toString(test.getLength());
//		String sourceReference = train.getFilename();
//		String sourceOffset = Integer.toString(train.getOffset());
//		String sourceLength = Integer.toString(train.getLength());
//		return new PlagiarismReference(offset, length, sourceReference, sourceOffset, sourceLength, pair.getSimilarity());
//	}
//
//	public void writeResults(String file, List<PlagiarismReference> plagiarisms) {
//		Element root = new Element("document");
//		root.setAttribute("reference", file);
//		for (PlagiarismReference plagiarismReference : plagiarisms) {
//			Element reference = new Element("feature");
//			reference.setAttribute("name", "detected-plagiarism");
//			reference.setAttribute("this_offset", plagiarismReference.getOffset());
//			reference.setAttribute("this_length", plagiarismReference.getLength());
//			reference.setAttribute("obfuscation", Double.toString(plagiarismReference.getSimilarity()));
//			reference.setAttribute("source_reference", plagiarismReference.getSourceReference());
//			reference.setAttribute("source_offset", plagiarismReference.getSourceOffset());
//			reference.setAttribute("source_length", plagiarismReference.getSourceLength());
//			root.addContent(reference);
//		}
//
//		Document doc = new Document();
//		doc.setContent(root);
//
//		XMLOutputter outputter = new XMLOutputter();
//		try {
//			Fileutils.createFileIfNotExist(resultsDir+file);
//			FileWriter writer = new FileWriter(resultsDir+file);
//			outputter.output(doc, writer);
//			writer.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//}
