package no.roek.nlpgraphs.misc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
		}
	}

	public static HashMap<String, String> getProperties() {
		Properties config = new Properties();
		try(InputStream is = new FileInputStream("app.properties")) {
			config.load(is);
			is.close();
			return new HashMap<String, String>((Map)config);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
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
}
