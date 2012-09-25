package no.roek.nlpgraphs.postprocessing;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import no.roek.nlpgraphs.algorithm.GraphEditDistance;
import no.roek.nlpgraphs.concurrency.Job;
import no.roek.nlpgraphs.document.GraphPair;
import no.roek.nlpgraphs.document.PlagiarismReference;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

public class PlagiarismWorker extends Thread {
	
	private BlockingQueue<Job> queue;
	private String resultsDir;
	private double plagiarismThreshold;

	public PlagiarismWorker(BlockingQueue<Job> queue) {
		this.queue = queue;
		this.resultsDir = ConfigService.getResultsDir();
		this.plagiarismThreshold = ConfigService.getPlagiarismThreshold();
	}

	@Override
	public void run() {
		boolean running = true;
		while(running) {
			Job job = queue.poll();
			if(job.isLastInQueue()) {
				running = false;
				break;
			}
			List<PlagiarismReference> plagReferences = findPlagiarism(job);
			System.out.println("done plagiarism search for file "+job.getFilename());
			for (PlagiarismReference plagiarismReference : plagReferences) {
				System.out.println("Found plagiarism in files "+ plagiarismReference.getSourceReference());
			}
			writeResults(job.getFile().getFileName().toString(), plagReferences);
		}
		
		System.out.println("Exiting app. Calculation done.");
		System.exit(0);
	}

	public List<PlagiarismReference> findPlagiarism(Job job) {
		List<PlagiarismReference> plagReferences = new ArrayList<>();
		
		for(GraphPair pair : job.getGraphPairs()) {
			GraphEditDistance ged = new GraphEditDistance(pair.getSuspiciousGraph(), pair.getSourceGraph());
			if(ged.getDistance() < plagiarismThreshold) {
				plagReferences.add(getPlagiarismReference(pair));
			}
		}
		
		return plagReferences;
	}

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

	public PlagiarismReference getPlagiarismReference(GraphPair pair) {
		Graph test = pair.getSuspiciousGraph();
		Graph train = pair.getSourceGraph();
		String offset = Integer.toString(test.getOffset());
		String length = Integer.toString(test.getLength());
		String sourceReference = train.getFilename();
		String sourceOffset = Integer.toString(train.getOffset());
		String sourceLength = Integer.toString(train.getLength());
		return new PlagiarismReference(offset, length, sourceReference, sourceOffset, sourceLength, pair.getSimilarity());
	}

	public void writeResults(String file, List<PlagiarismReference> plagiarisms) {
		Element root = new Element("document");
		root.setAttribute("reference", file);
		for (PlagiarismReference plagiarismReference : plagiarisms) {
			Element reference = new Element("feature");
			reference.setAttribute("name", "detected-plagiarism");
			reference.setAttribute("this_offset", plagiarismReference.getOffset());
			reference.setAttribute("this_length", plagiarismReference.getLength());
			reference.setAttribute("obfuscation", Double.toString(plagiarismReference.getSimilarity()));
			reference.setAttribute("source_reference", plagiarismReference.getSourceReference());
			reference.setAttribute("source_offset", plagiarismReference.getSourceOffset());
			reference.setAttribute("source_length", plagiarismReference.getSourceLength());
			root.addContent(reference);
		}

		Document doc = new Document();
		doc.setContent(root);

		XMLOutputter outputter = new XMLOutputter();
		try {
			Fileutils.createFileIfNotExist(resultsDir+file);
			FileWriter writer = new FileWriter(resultsDir+file);
			outputter.output(doc, writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
