package vito;

import gffParser.CDS;
import gffParser.Exon;
import gffParser.Gene;
import gffParser.MRNA;
import gffParser.StartCodon;
import gffParser.StopCodon;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TempParser {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		HashMap<String, IDHolder> hashOfIDHolders;
//		HashMap<String, IDHolder> hashOfGenes = new HashMap<String, IDHolder>();
//		
//		File file= new File("/Users/Vinny/Data/c2c12_cugbp1_kd/genes.gff");
//		
//		Pattern pattern3 = Pattern.compile("ID[^;]*;");
//		TreeSet<String> setOfIds = new TreeSet<String>();
//		long start = System.currentTimeMillis();
//		try {
//			BufferedReader in = new BufferedReader(new FileReader(file),16384);
//			String line;
//			try {
//				hashOfIDHolders=new HashMap<String, IDHolder>();
//				while((line=in.readLine())!=null){
//					if(line.startsWith("#")){
//						//Skip -- This is header
//					}else if (line!=null){
//						String[] temp = line.split("\t");
//						//Parser returns a hash of Genes.
//						if(temp.length<9){
//							System.err.println("GFF3 File does not have 9 columns");
//						}
//						String type = temp[2]; 
//						//If type is gene, make a gene
//						if(type.equals("gene")){
//							Gene newGene = new Gene(temp[0],temp[1],temp[2],temp[3],temp[4],temp[5],
//									temp[6],temp[7],temp[8]);
//							hashOfIDHolders.put(newGene.getAttributes().get("ID"),newGene);
//							hashOfGenes.put(newGene.getAttributes().get("Name"),newGene);
//						}else if(type.equals("mRNA")){
//							MRNA newMRNA = new MRNA(temp[0],temp[1],temp[2],temp[3],temp[4],temp[5],
//									temp[6],temp[7],temp[8]);
//							hashOfIDHolders.put(newMRNA.getAttributes().get("ID"), newMRNA);
//							hashOfIDHolders.get(newMRNA.getAttributes().get("Parent")).addChild(newMRNA);
//						}else if(type.equals("CDS")){ //CDS do not have IDS 
//							CDS newCDS = new CDS(temp[0],temp[1],temp[2],temp[3],temp[4],temp[5],
//									temp[6],temp[7],temp[8]);
//							hashOfIDHolders.get(newCDS.getAttributes().get("Parent")).addChild(newCDS);
//						}else if(type.equals("start_codon")){
//							StartCodon newStartCodon = new StartCodon(temp[0],temp[1],temp[2],temp[3],
//									temp[4],temp[5],temp[6],temp[7],temp[8]);
//							hashOfIDHolders.get(newStartCodon.getAttributes().get("Parent")).addChild(newStartCodon);
//						}else if(type.equals("stop_codon")){
//							StopCodon newStopCodon = new StopCodon(temp[0],temp[1],temp[2],temp[3],
//									temp[4],temp[5],temp[6],temp[7],temp[8]);
//							hashOfIDHolders.get(newStopCodon.getAttributes().get("Parent")).addChild(newStopCodon);
//						}else if(type.equals("exon")){
//							Exon newExon = new Exon(temp[0],temp[1],temp[2],temp[3],
//									temp[4],temp[5],temp[6],temp[7],temp[8]);
//							hashOfIDHolders.get(newExon.getAttributes().get("Parent")).addChild(newExon);
//						}else{
//							//errors += (type+"\n");
//							//System.err.println(type);
//							//hashOfGenes=null;
//							//return "An unknown type was located in the GFF file\n" + type;
//						}		
//					}	
//				}
//					
//				System.out.println("Done");
//				System.out.println(System.currentTimeMillis()-start);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		File file= new File("/Users/Vinny/Data/c2c12_cugbp1_kd/genes.gff");
		
		args = new String[2];
		args[0]="I="+file.getAbsolutePath();
		args[1]="O=~/Desktop/test_java_bam";
		
	//	CollectAlignmentSummaryMetrics.main(args);
		
		
		ArrayList<String> temp = new ArrayList<String>();
		Pattern pattern3 = Pattern.compile("ID[^;]*;");
		TreeSet<String> setOfIds = new TreeSet<String>();
		long start = System.currentTimeMillis();
		try {
			
			BufferedReader in = new BufferedReader(new FileReader(file),16384);
			String line;
			try {
				while((line=in.readLine()) != null){
					temp.add(line);
//					if(line.contains("\tgene\t")){
//						Matcher id = pattern3.matcher(line);
//						if(id.find()){
//							String stringID = id.group();
//							setOfIds.add(stringID.substring(3, stringID.length()-1));
//						}
//					}
				}
				 FileOutputStream fos = new FileOutputStream("test.ser");
			     BufferedOutputStream bos = new BufferedOutputStream(fos);
			     ObjectOutputStream oos = new ObjectOutputStream(bos);
			     oos.writeObject(temp);
			     oos.close();
				System.out.println("Done");
				System.out.println(System.currentTimeMillis()-start);
				
				
				start = System.currentTimeMillis();
				try {
					ArrayList a1;
					FileInputStream fis = new FileInputStream("test.ser");
					BufferedInputStream bis = new BufferedInputStream(fis);
					ObjectInputStream ois = new ObjectInputStream(bis);
					a1 = (ArrayList)ois.readObject();
					ois.close();
					System.out.println(System.currentTimeMillis()-start);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	
	
	
	

}
