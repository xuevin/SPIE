package gffParser;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GFF3Parser {
	private HashMap<String, IDHolder> hashOfIDHolders;
	private TreeSet<String> setOfIds;
	private File inFile;
	private HashMap<String, Gene> hashOfGenes;
	public GFF3Parser(){
	}
	
//	/**
//	 * This parser is faster. It takes in a file and only gets the gene ID
//	 * 
//	 * @param file the file
//	 * 
//	 * @return the string
//	 */
//	@Deprecated
//	public String parse2(File file){
//		inFile = file;
//		
//		String line;
//		Pattern pattern3 = Pattern.compile("ID[^;]*;");
//		setOfIds = new TreeSet<String>();
//		long start = System.currentTimeMillis();
//		try {
//			BufferedReader in = new BufferedReader(new FileReader(file),16384);;
//			while((line=in.readLine()) != null){
//				if(line.contains("\tgene\t")){
//					Matcher id = pattern3.matcher(line);
//					if(id.find()){
//						String stringID = id.group();
//						setOfIds.add(stringID.substring(3, stringID.length()-1));
//					}
//				}
//			}
//			return ("Completed in " + (System.currentTimeMillis()-start) + " Milliseconds");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return "Incomplete";
//	}
//	@Deprecated
//	public Collection<String> getCollectionOfStringIDs(){
//		if(setOfIds!=null){
//			return setOfIds;	
//		}else{
//			System.err.println("Please Parse the file before you attempt to get the string");
//			return null;
//		}
//	}
//
//	/**
//	 * This function parses the file by creating the structure as it is made.
//	 * 
//	 * @param file the file
//	 * 
//	 * @return the string
//	 */
	public String parse(File file) {
		
		hashOfIDHolders = new HashMap<String, IDHolder>();
		hashOfGenes = new HashMap<String,Gene>();
		
		String line;
		float start = System.currentTimeMillis();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file),16384);
			
			while((line=in.readLine()) != null){
				if(line.startsWith("#")){
					//Skip -- This is header
				}else if (line!=null){
					String[] temp = line.split("\t");
					//Parser returns a hash of Genes.
					if(temp.length<9){
						System.err.println("GFF3 File does not have 9 columns");
						hashOfGenes=null;
						return "The current file is not in the correct format.\n Please check it again.";
					}
					String type = temp[2]; 
					//If type is gene, make a gene
					if(type.equals("gene")){
						Gene newGene = new Gene(temp[0],temp[1],temp[2],temp[3],temp[4],temp[5],
								temp[6],temp[7],temp[8]);
						hashOfIDHolders.put(newGene.getAttributes().get("ID"),newGene);
						hashOfGenes.put(newGene.getAttributes().get("Name"),newGene);
					}else if(type.equals("mRNA")){
						MRNA newMRNA = new MRNA(temp[0],temp[1],temp[2],temp[3],temp[4],temp[5],
								temp[6],temp[7],temp[8]);
						hashOfIDHolders.put(newMRNA.getAttributes().get("ID"), newMRNA);
						hashOfIDHolders.get(newMRNA.getAttributes().get("Parent")).addChild(newMRNA);
					}else if(type.equals("CDS")){ //CDS do not have IDS 
						CDS newCDS = new CDS(temp[0],temp[1],temp[2],temp[3],temp[4],temp[5],
								temp[6],temp[7],temp[8]);
						hashOfIDHolders.get(newCDS.getAttributes().get("Parent")).addChild(newCDS);
					}else if(type.equals("start_codon")){
						StartCodon newStartCodon = new StartCodon(temp[0],temp[1],temp[2],temp[3],
								temp[4],temp[5],temp[6],temp[7],temp[8]);
						hashOfIDHolders.get(newStartCodon.getAttributes().get("Parent")).addChild(newStartCodon);
					}else if(type.equals("stop_codon")){
						StopCodon newStopCodon = new StopCodon(temp[0],temp[1],temp[2],temp[3],
								temp[4],temp[5],temp[6],temp[7],temp[8]);
						hashOfIDHolders.get(newStopCodon.getAttributes().get("Parent")).addChild(newStopCodon);
					}else if(type.equals("exon")){
						Exon newExon = new Exon(temp[0],temp[1],temp[2],temp[3],
								temp[4],temp[5],temp[6],temp[7],temp[8]);
						hashOfIDHolders.get(newExon.getAttributes().get("Parent")).addChild(newExon);
					}else{
						//errors += (type+"\n");
						//System.err.println(type);
						//hashOfGenes=null;
						//return "An unknown type was located in the GFF file\n" + type;
					}		
				}
			}
			//System.out.println ("Completed in " + (System.currentTimeMillis()-start) + " Milliseconds");
			in.close();
			hashOfIDHolders=null;
		} catch (IOException e) {
			System.err.println("There was a a problem parsing the GFF3");
			e.printStackTrace();
		}
		return "Completed in " + (System.currentTimeMillis()-start) + " Milliseconds";
	}
//	@Deprecated
//	public Gene get(String geneID){
//		if(inFile!=null){
//			String line;
//			long start = System.currentTimeMillis();
//			try {
//				BufferedReader buffer = new BufferedReader(new FileReader(inFile),16384);
//				Gene newGene = null;
//				hashOfIDHolders=new HashMap<String, IDHolder>();
//				while((line=buffer.readLine())!=null){
//					if(line.contains(geneID) || hashContains(line)){
//						if(line.startsWith("#")){
//							//Skip -- This is header
//						}else if (line!=null){
//							String[] temp = line.split("\t");
//							//Parser returns a hash of Genes.
//							if(temp.length<9){
//								System.err.println("GFF3 File does not have 9 columns");
//								return null;
//							}
//							String type = temp[2]; 
//							//If type is gene, make a gene
//							if(type.equals("gene")){
//								newGene = new Gene(temp[0],temp[1],temp[2],temp[3],temp[4],temp[5],
//										temp[6],temp[7],temp[8]);
//								hashOfIDHolders.put(newGene.getAttributes().get("ID"),newGene);
//							}else if(type.equals("mRNA")){
//								MRNA newMRNA = new MRNA(temp[0],temp[1],temp[2],temp[3],temp[4],temp[5],
//										temp[6],temp[7],temp[8]);
//								hashOfIDHolders.put(newMRNA.getAttributes().get("ID"), newMRNA);
//								hashOfIDHolders.get(newMRNA.getAttributes().get("Parent")).addChild(newMRNA);
//							}else if(type.equals("CDS")){ //CDS do not have IDS 
//								CDS newCDS = new CDS(temp[0],temp[1],temp[2],temp[3],temp[4],temp[5],
//										temp[6],temp[7],temp[8]);
//								hashOfIDHolders.get(newCDS.getAttributes().get("Parent")).addChild(newCDS);
//							}else if(type.equals("start_codon")){
//								StartCodon newStartCodon = new StartCodon(temp[0],temp[1],temp[2],temp[3],
//										temp[4],temp[5],temp[6],temp[7],temp[8]);
//								hashOfIDHolders.get(newStartCodon.getAttributes().get("Parent")).addChild(newStartCodon);
//							}else if(type.equals("stop_codon")){
//								StopCodon newStopCodon = new StopCodon(temp[0],temp[1],temp[2],temp[3],
//										temp[4],temp[5],temp[6],temp[7],temp[8]);
//								hashOfIDHolders.get(newStopCodon.getAttributes().get("Parent")).addChild(newStopCodon);
//							}else if(type.equals("exon")){
//								Exon newExon = new Exon(temp[0],temp[1],temp[2],temp[3],
//										temp[4],temp[5],temp[6],temp[7],temp[8]);
//								hashOfIDHolders.get(newExon.getAttributes().get("Parent")).addChild(newExon);
//							}else{
//								//errors += (type+"\n");
//								//System.err.println(type);
//								//hashOfGenes=null;
//								//return "An unknown type was located in the GFF file\n" + type;
//							}		
//						}	
//					}
//					
//				}
//				System.out.println ("Completed in " + (System.currentTimeMillis()-start) + " Milliseconds");
//				return newGene;
//			} catch (IOException e) {
//				System.err.println("There was a a problem parsing the GFF3");
//				e.printStackTrace();
//			} 
//		}
//		return null;
//	}
//	private boolean hashContains(String line) {
//		for(String string :hashOfIDHolders.keySet()){
//			if(line.contains(string)){
//				return true;
//			}
//		}
//		return false;
//	}
	public HashMap<String, Gene> getGenes() {
		if(hashOfGenes.size()==0){
			System.err.println("Are You Sure You Parsed First?");
		}
		return hashOfGenes;
	}
	

}
