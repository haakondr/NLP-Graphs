package text2graph.dependencyParser;

import java.io.File;

public class PlagFile {
	private String relPath;
	private File file;
	
	public PlagFile(File file, String baseDir) {
		this.file = file;
		this.relPath = new File(baseDir).toURI().relativize(file.toURI()).getPath();
	}

	public String getRelPath() {
		return relPath;
	}

	public File getFile() {
		return file;
	}
	
	public String getFilename() {
		return file.getName();
	}
	
	public String getPath() {
		return file.getPath();
	}

}