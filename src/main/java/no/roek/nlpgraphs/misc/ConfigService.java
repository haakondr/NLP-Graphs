package no.roek.nlpgraphs.misc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;


public class ConfigService {

	private Properties configFile;
	private InputStream is;

	public ConfigService() {
		configFile = new Properties();
		try{
			is = new FileInputStream("app.properties");
			configFile.load(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public String getParsedFilesDir() {
		return configFile.getProperty("PARSED_DIR");
	}

	public String getDataDir() {
		return configFile.getProperty("DATA_DIR");
	}

	public String getTestDir() {
		return configFile.getProperty("TEST_DIR");
	}

	public String getTrainDir() {
		return configFile.getProperty("TRAIN_DIR");
	}

	public String getAnnotationsDir() {
		return configFile.getProperty("ANNOTATIONS_DIR");
	}

	public String getResultsDir() {
		return configFile.getProperty("RESULTS_DIR");
	}

	public int getDocumentRecall() {
		return Integer.parseInt(configFile.getProperty("DOCUMENT_RECALL"));
	}

	public int getPlagiarismThreshold() {
		return Integer.parseInt(configFile.getProperty("PLAGIARISM_THRESHOLD"));
	}

	public String getMaltParams() {
		return configFile.getProperty("MALT_PARAMS");
	}

	public String getPOSTaggerParams() {
		return configFile.getProperty("POSTAGGER_PARAMS");
	}

	public int getPOSTaggerThreadCount() {
		return Integer.parseInt(configFile.getProperty("POSTAGGER_THREADS"));
	}

	public int getMaltParserThreadCount() {
		return Integer.parseInt(configFile.getProperty("MALTPARSER_THREADS"));
	}

	public int getPlagiarismThreads() {
		return Integer.parseInt(configFile.getProperty("PLAGIARISM_SEARCHER_THREADS"));
	}

	public int getSentenceRetrievalThreads() {
		return Integer.parseInt(configFile.getProperty("SENTENCE_RETRIEVAL_THREADS"));
	}
	
	public int getIndexBuilderThreads() {
		return Integer.parseInt(configFile.getProperty("INDEX_BUILDER_THREADS"));
	}
	
	public String getWordNetDir() {
		return configFile.getProperty("WORDNET_DIR");
	}
}
