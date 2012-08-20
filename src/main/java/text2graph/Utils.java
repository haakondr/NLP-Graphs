package text2graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import text2graph.dependencyParser.PlagFile;



public class Utils {

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

	public static File[] getFiles(String directory) {
		return new File(directory).listFiles();
	}


	public static List<String> readAllLines(String filename) {
		List<String> out = new ArrayList<String>();
		try {
			FileInputStream fstream = new FileInputStream(filename);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while ((line = br.readLine()) != null)   {
				out.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return out;

	}

	public static List<PlagFile> getTaskList(String dir, String baseDir) {
		List<PlagFile> tasks = new ArrayList<PlagFile>();

		for (File file : Utils.getFiles(dir)) {
			if(file.isFile() && file.getName().endsWith(".txt")) {
				tasks.add(new PlagFile(file, baseDir));
			}else if(file.isDirectory()) {
				if(tasks.addAll(getTaskList(file.getPath(), baseDir))) {
					return tasks;
				}
			}
		}
		return tasks;
	}

	public static PlagFile[] getTaskList(String dir) {
		return getTaskList(dir, dir).toArray(new PlagFile[0]);
	}
	
	public static PlagFile[] getUnparsedFiles(String dir, String outDir) {
		PlagFile[] files = getTaskList(dir);
		List<PlagFile> out = new ArrayList<>();
		for (PlagFile file : files) {
			File outFile = new File(outDir+file.getRelPath());
			if(!outFile.exists()) {
				out.add(file);
			}
		}
		
		return out.toArray(new PlagFile[0]);
	}

	public static PlagFile[][] getChunks(PlagFile[] files, int n) {
		List<PlagFile[]> chunks = new ArrayList<>();

		int fileCount = files.length;
		int chunksize = fileCount / n;
		int i = 0;
		while(i < fileCount) {
			if(i+chunksize*2 < fileCount) {
				chunks.add(Arrays.copyOfRange(files, i, i+chunksize));
				i += chunksize;
			}else{
				chunks.add(Arrays.copyOfRange(files, i, fileCount));
				i = fileCount;
			}
		}

		return chunks.toArray(new PlagFile[0][0]);
	}

}
