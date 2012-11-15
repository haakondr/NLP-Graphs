package no.roek.nlpgraphs.ged;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.App;
import no.roek.nlpgraphs.document.SentencePair;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.GraphUtils;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

public class PlagiarismWorker extends Thread {

	private BlockingQueue<PlagiarismJob> queue;
	private String resultsDir, parsedDir, testDir, trainDir;
	private double plagiarismThreshold;
	private App concurrencyService;
	private boolean running;

	public PlagiarismWorker(BlockingQueue<PlagiarismJob> queue, App concurrencyService) {
		this.queue = queue;
		ConfigService cs = new ConfigService();
		parsedDir = cs.getParsedFilesDir();
		testDir =cs.getTestDir();
		trainDir = cs.getTrainDir();
		this.resultsDir = cs.getResultsDir();
		this.plagiarismThreshold = cs.getPlagiarismThreshold();
		this.concurrencyService = concurrencyService;
	}

	@Override
	public void run() {
		running = true;
		while(running) {
			try {
				PlagiarismJob job = queue.take();
				if(job.isLastInQueue()) {
					running = false;
					break;
				}
				List<PlagiarismReference> plagReferences = findPlagiarism(job);
//				List<PlagiarismReference> plagReferences = listCandidateReferences(job);
				writeResults(job.getFile().getFileName().toString(), plagReferences);
				concurrencyService.plagJobDone(this, "queue: "+queue.size());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void kill() {
		try {
			PlagiarismJob job = new PlagiarismJob("kill");
			job.setLastInQueue(true);
			queue.put(job);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public List<PlagiarismReference> listCandidateReferences(PlagiarismJob job) {
		List<PlagiarismReference> plagReferences = new ArrayList<>();
		for (SentencePair pair : job.getTextPairs()) {
			plagReferences.add(getPlagiarismReference(pair, pair.getSimilarity(), false));
		}
		
		return plagReferences;
	}
	
	public List<PlagiarismReference> findPlagiarism(PlagiarismJob job) {
		List<PlagiarismReference> plagReferences = new ArrayList<>();

		for(SentencePair pair : job.getTextPairs()) {
			Graph train = GraphUtils.getGraphFromFile(parsedDir+trainDir+pair.getTrainFile(), pair.getTrainSentence());
			Graph test = GraphUtils.getGraphFromFile(parsedDir+testDir+pair.getTestFile(), pair.getTestSentence());

			GraphEditDistance ged = new GraphEditDistance(test, train);
			double dist = ged.getDistance();
			plagReferences.add(getPlagiarismReference(pair, dist, (dist < plagiarismThreshold)));
		}

		return plagReferences;
	}

	public PlagiarismReference getPlagiarismReference(SentencePair pair, double similarity, boolean detectedPlagiarism) {
		String filename = pair.getTestFile();
		String offset = Integer.toString(pair.getTestGraph().getOffset());
		String length = Integer.toString(pair.getTestGraph().getLength());
		String sourceReference = pair.getTrainFile();
		String sourceOffset = Integer.toString(pair.getTrainGraph().getOffset());
		String sourceLength = Integer.toString(pair.getTrainGraph().getLength());
		String name = detectedPlagiarism ? "detected-plagiarism" : "candidate-passage";
//		String name = "detected-plagiarism";
		return new PlagiarismReference(filename, name, offset, length, sourceReference, sourceOffset, sourceLength, similarity);
	}

	public void writeResults(String file, List<PlagiarismReference> plagiarisms) {
		Element root = new Element("document");
		root.setAttribute("reference", file);
		for (PlagiarismReference plagiarismReference : plagiarisms) {
			Element reference = new Element("feature");
			reference.setAttribute("name", plagiarismReference.getName());
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

		FileWriter writer = null;
		try {
			Fileutils.createParentFolders(resultsDir+file);
			//TODO: .xml file ending instead of .txt, so permeasures.py detects it
			writer = new FileWriter(resultsDir+file);
			outputter.output(doc, writer);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}
}
