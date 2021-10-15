package gr.csd.uoc.hy463.themis.indexer;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;

import gr.csd.uoc.hy463.themis.config.Config;

class MergeFiles implements Runnable{
	Config config; 
	Queue<Integer> queue;
	MergeFiles(Config config, Queue<Integer> queue) {
		this.config = config; 
		this.queue = queue; 
	}


	public boolean compare(String line1, String line2) {

		if(line1.contentEquals(line2)) return true; 

		return false; 
	}

	public void toWrite(String[] split1, String[] split2, BufferedWriter writer) throws IOException {
		LinkedList<String> positions = new LinkedList<>();

		double newtf = Double.parseDouble(split1[1])+Double.parseDouble(split2[1]); 
		double newdf = Double.parseDouble(split1[2])+Double.parseDouble(split2[2]);

		String[] pos1 = split1[3].replace("[", "").replace("]", "").split(","); 
		String[] pos2 = split2[3].replace("[", "").replace("]", "").split(",");

		positions.addAll(Arrays.asList(pos1));
		positions.addAll(Arrays.asList(pos2));
		
		writer.write(split2[0]+"#"+newtf+"#"+newdf+"#"+positions);
		writer.newLine();
	
	}
	
	public void merge(String file1, String file2, String file3) {
		try {
			System.out.println(file1 + " Started Merging " + file2 );
			FileReader fileReader1 = new FileReader(file1);
			FileReader fileReader2 = new FileReader(file2);
			BufferedWriter writer = new BufferedWriter(new FileWriter(file3));
			BufferedReader bufferedReader1 = new BufferedReader(fileReader1);
			BufferedReader bufferedReader2 = new BufferedReader(fileReader2);
			String line1 = bufferedReader1.readLine();
			String line2 = bufferedReader2.readLine();

			while (line1 != null || line2 != null) {
				try {
					ArrayList<String> temp = new ArrayList<>();
					line1 = bufferedReader1.readLine();
					line2 = bufferedReader2.readLine();

					while(true){

						if((line1 != null) && (line2 != null)){

							String[] split1 = line1.split("#");
							String[] split2 = line2.split("#");

							if(split1[0].equalsIgnoreCase(split2[0])){
								toWrite(split1, split2, writer); 


								line1 = bufferedReader1.readLine();
								line2 = bufferedReader2.readLine();
							}
							else if(split1[0].compareToIgnoreCase(split2[0]) > 0){

								temp.add(line2);
								line2 = bufferedReader2.readLine();

							}
							else if(split1[0].compareToIgnoreCase(split2[0]) < 0){

								temp.add(line1);
								line1 = bufferedReader1.readLine();

							}

						}
						else if(line1 == null && line2 != null){

							temp.add(line2);

							while((line2 = bufferedReader2.readLine()) != null){

								temp.add(line2);

							}

							break;

						}
						else if(line2 == null & line1 != null){

							temp.add(line1);

							while((line1 = bufferedReader1.readLine()) != null){

								temp.add(line1);

							}
							break;

						}
						else break; 

						if(temp.size() >= config.getPartialIndexSize()*3) {
							
							Collections.sort(temp);

							for(String line : temp){
								writer.write(line);
								writer.newLine();

							} 
							System.out.println(temp.size()); 
							temp.clear();
						}


					}

					Collections.sort(temp);
					for(String line : temp){

						writer.write(line);
						writer.newLine();

					}
					
					System.out.println(file1 + " Done Merging " + file2 );
					bufferedReader1.close();
					bufferedReader2.close();
					Files.delete(Paths.get(file1));
					Files.delete(Paths.get(file2)); 
					writer.close();
				} catch (IOException ex) {

					java.util.logging.Logger.getLogger(MergeFiles.class.getName()).log(Level.SEVERE, null, ex);

				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}


	
}