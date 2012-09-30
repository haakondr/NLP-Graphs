package no.roek.nlpgraphs.postprocessing;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.algorithm.GraphEditDistance;
import no.roek.nlpgraphs.concurrency.PlagiarismJob;
import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.document.PlagiarismReference;
import no.roek.nlpgraphs.document.TextPair;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.GraphUtils;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

public class PlagiarismWorker extends Thread {

	private BlockingQueue<PlagiarismJob> queue;
	private String resultsDir;
	private double plagiarismThreshold;

	public PlagiarismWorker(BlockingQueue<PlagiarismJob> queue) {
		this.queue = queue;
		this.resultsDir = ConfigService.getResultsDir();
		this.plagiarismThreshold = ConfigService.getPlagiarismThreshold();
	}

	@Override
	public void run() {
		boolean running = true;
		while(running) {
			try {
				PlagiarismJob job = queue.take();
				if(job.isLastInQueue()) {
					running = false;
				}else {
					List<PlagiarismReference> plagReferences = findPlagiarism(job);
					System.out.println("done plagiarism search for file "+job.getFilename());
					for (PlagiarismReference plagiarismReference : plagReferences) {
						System.out.println("Found plagiarism in files "+ plagiarismReference.getSourceReference());
					}
					writeResults(job.getFile().getFileName().toString(), plagReferences);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Exiting app. Calculation done.");
		System.exit(0);
	}

	
	public List<PlagiarismReference> findPlagiarism(PlagiarismJob job) {
		List<PlagiarismReference> plagReferences = new ArrayList<>();
		
		for(TextPair pair : job.getTextPairs()) {
			//TODO: if parsed file does not exist, parse the file and print info to user that file does not exist.
			Graph test = GraphUtils.getGraphFromFile(pair.getTestSentence().getFilename(), pair.getTestSentence().getNumber());
			Graph train = GraphUtils.getGraphFromFile(pair.getTrainSentence().getFilename(), pair.getTrainSentence().getNumber());
			
			GraphEditDistance ged = new GraphEditDistance(test, train);
			double dist = ged.getDistance();
			if(dist < plagiarismThreshold) {
				plagReferences.add(getPlagiarismReference(pair, dist));
			}
		}
		
		return plagReferences;
	}
//	public List<PlagiarismReference> findPlagiarism(PlagiarismJob job) {
//		List<PlagiarismReference> plagReferences = new ArrayList<>();
//
//		for(GraphPair pair : job.getGraphPairs()) {
//			GraphEditDistance ged = new GraphEditDistance(pair.getSuspiciousGraph(), pair.getSourceGraph());
//			if(ged.getDistance() < plagiarismThreshold) {
//				plagReferences.add(getPlagiarismReference(pair));
//			}
//		}
//
//		return plagReferences;
//	}

	public PlagiarismReference getPlagiarismReference(TextPair pair, double similarity) {
		NLPSentence test = pair.getTestSentence();
		NLPSentence train = pair.getTrainSentence();
		String filename = test.getFilename();
		String offset = Integer.toString(test.getStart());
		String length = Integer.toString(test.getLength());
		String sourceReference = train.getFilename();
		String sourceOffset = Integer.toString(train.getStart());
		String sourceLength = Integer.toString(train.getLength());
		return new PlagiarismReference(filename, offset, length, sourceReference, sourceOffset, sourceLength, similarity);
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
			Fileutils.createParentFolders(resultsDir+file);
			FileWriter writer = new FileWriter(resultsDir+file);
			outputter.output(doc, writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
