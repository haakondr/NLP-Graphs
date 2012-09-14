package no.roek.nlpgraphs.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.roek.nlpgraphs.document.DocumentFile;
import no.roek.nlpgraphs.graph.Graph;


public class Fileutils {

	public static void writeToFile(String filename, String[] lines) {
		createFileIfNotExist(filename);

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

			for (String line : lines) {
				writer.write(line);
				writer.newLine();
			}
			writer.close();
		}catch ( IOException ioe ) {
			ioe.printStackTrace();
		}
	}

	public static void writeToFile(String filename, String text) {
		createFileIfNotExist(filename);
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writer.write(text);
			writer.close();
		}catch ( IOException ioe ) {
			ioe.printStackTrace();
		}
	}

	public static void createFileIfNotExist(String filename) {
		File f = new File(filename);
		File parent = f.getParentFile();

		if(!parent.exists()) {
			parent.mkdirs();
		}
	}

	public static File[] getFiles(Path dir) {
		return dir.toFile().listFiles();
	}

	public static List<DocumentFile> getFileList(Path dir, Path baseDir) {
		List<DocumentFile> tasks = new ArrayList<DocumentFile>();

		for (File file : getFiles(dir)) {
			if(file.isFile() && file.getName().endsWith(".txt")) {
				tasks.add(new DocumentFile(file.toPath(), baseDir));
			}else if(file.isDirectory()) {
				tasks.addAll(getFileList(file.toPath(), baseDir));
			}
		}
		return tasks;
	}

	public static DocumentFile[] getFileList(Path dir) {
		return getFileList(dir, dir).toArray(new DocumentFile[0]);
	}


	public static DocumentFile[] getUnparsedFiles(Path dir, String outDir) {
		DocumentFile[] files = getFileList(dir);
		List<DocumentFile> out = new ArrayList<DocumentFile>();
		for (DocumentFile file : files) {
			File outFile = new File(outDir+file.getRelPath());
			if(!outFile.exists()) {
				out.add(file);
			}
		}

		return out.toArray(new DocumentFile[0]);
	}

	public static DocumentFile[][] getChunks(DocumentFile[] files, int n) {
		List<DocumentFile[]> chunks = new ArrayList<>();

		int fileCount = files.length;
		int chunksize = fileCount / n;
		int i = 0;
		while(i < fileCount) {
			if(i+chunksize*2 <= fileCount) {
				chunks.add(Arrays.copyOfRange(files, i, i+chunksize));
				i += chunksize;
			}else{
				chunks.add(Arrays.copyOfRange(files, i, fileCount));
				i = fileCount;
			}
		}

		return chunks.toArray(new DocumentFile[0][0]);
	}

	//TODO: rewrite with generics
	public static Path[][] getChunks(Path[] files, int n) {
		List<Path[]> chunks = new ArrayList<>();

		int fileCount = files.length;
		int chunksize = fileCount / n;
		int i = 0;
		while(i < fileCount) {
			if(i+chunksize*2 <= fileCount) {
				chunks.add(Arrays.copyOfRange(files, i, i+chunksize));
				i += chunksize;
			}else{
				chunks.add(Arrays.copyOfRange(files, i, fileCount));
				i = fileCount;
			}
		}

		return chunks.toArray(new Path[0][0]);
	}

	public static String getText(Path path) {

		List<String> lines;
		try {
			lines = Files.readAllLines(path, StandardCharsets.UTF_8);
			StringBuffer sb = new StringBuffer();
			for (String line : lines) {
				sb.append(line);
			}

			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
