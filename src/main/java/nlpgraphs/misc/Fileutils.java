package nlpgraphs.misc;

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

import nlpgraphs.classes.POSFile;


public class Fileutils {

	public static void writeToFile(String filename, String[] lines) {
		File f = new File(filename);
		File parent = f.getParentFile();

		if(!parent.exists()) {
			parent.mkdirs();
		}
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

	public static File[] getFiles(Path dir) {
		return dir.toFile().listFiles();
	}


//	public static List<String> readAllLines(String filename) {
//		List<String> out = new ArrayList<String>();
//		try {
//			FileInputStream fstream = new FileInputStream(filename);
//			DataInputStream in = new DataInputStream(fstream);
//			BufferedReader br = new BufferedReader(new InputStreamReader(in));
//			String line = null;
//			while ((line = br.readLine()) != null)   {
//				out.add(line);
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		return out;
//
//	}

	public static List<POSFile> getFileList(Path dir, Path baseDir) {
		List<POSFile> tasks = new ArrayList<POSFile>();
		
		for (File file : getFiles(dir)) {
			if(file.isFile() && file.getName().endsWith(".txt")) {
				tasks.add(new POSFile(file.toPath(), baseDir));
			}else if(file.isDirectory()) {
				tasks.addAll(getFileList(file.toPath(), baseDir));
			}
		}
		return tasks;
	}

	public static POSFile[] getFileList(Path dir) {
		return getFileList(dir, dir).toArray(new POSFile[0]);
	}
	
	
	public static POSFile[] getUnparsedFiles(Path dir, String outDir) {
		POSFile[] files = getFileList(dir);
		List<POSFile> out = new ArrayList<POSFile>();
		for (POSFile file : files) {
			File outFile = new File(outDir+file.getRelPath());
			if(!outFile.exists()) {
				out.add(file);
			}
		}
		
		return out.toArray(new POSFile[0]);
	}

	public static POSFile[][] getChunks(POSFile[] files, int n) {
		List<POSFile[]> chunks = new ArrayList<>();

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

		return chunks.toArray(new POSFile[0][0]);
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
