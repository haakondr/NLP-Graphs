package text2graph.misc;

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

	public static List<POSFile> getTaskList(String dir, String baseDir) {
		List<POSFile> tasks = new ArrayList<POSFile>();
		
		for (File file : getFiles(dir)) {
			if(file.isFile() && file.getName().endsWith(".txt")) {
				tasks.add(new POSFile(file, baseDir));
			}else if(file.isDirectory()) {
				tasks.addAll(getTaskList(file.getPath(), baseDir));
			}
		}
		return tasks;
	}

	public static POSFile[] getTaskList(String dir) {
		return getTaskList(dir, dir).toArray(new POSFile[0]);
	}
	
	public static POSFile[] getUnparsedFiles(String dir, String outDir) {
		POSFile[] files = getTaskList(dir);
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

}
