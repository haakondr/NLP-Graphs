package no.roek.nlpgraphs.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.roek.nlpgraphs.document.DocumentFile;


public class Fileutils {

	public static void writeToFile(String filename, String[] lines) {
		createParentFolderIfNotExist(filename);

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
		createParentFolderIfNotExist(filename);

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writer.write(text);
			writer.close();
		}catch ( IOException ioe ) {
			ioe.printStackTrace();
		}
	}

	public static void createParentFolderIfNotExist(String filename) {
		//TODO: make tests
		File f = new File(filename);
		File parent = f.getParentFile();

		if(!(parent == null)) {
			if(!parent.exists()) {
				parent.mkdirs();
			}
		}
		
		if(parent.getParentFile() == null) {
			createParentFolderIfNotExist(parent.toString());
		}
	}

	
	public static File[] getFiles(Path dir) {
		return dir.toFile().listFiles();
	}
	
	public static File[] getFiles(String dir) {
		return Paths.get(dir).toFile().listFiles();
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

	public static DocumentFile[] getFileList(String dir) {
		return getFileList(Paths.get(dir));
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

	public static File[][] getChunks(File[] files, int n) {
		List<File[]> chunks = new ArrayList<>();

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

		return chunks.toArray(new File[0][0]);
	}

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

//	public static <T> List<T[]> getChunks(T[] files, int n) {
//		List<T[]> chunks = new ArrayList<>();
//
//		int fileCount = files.length;
//		int chunksize = fileCount / n;
//		int i = 0;
//
//		while(i < fileCount) {
//			if(i+chunksize*2 <= fileCount) {
//				chunks.add(Arrays.copyOfRange(files, i, i+chunksize));
//				i += chunksize;
//			}else{
//				chunks.add(Arrays.copyOfRange(files, i, fileCount));
//				i = fileCount;
//			}
//		}
//
//		return chunks;
//	}

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
