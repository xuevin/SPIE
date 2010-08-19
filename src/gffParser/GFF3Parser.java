package gffParser;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * A class that handels parsing GFF3 Files
 * @author Vinny
 *
 */
public class GFF3Parser {
	private HashMap<String, IDHolder> hashOfIDHolders;
	private HashMap<String, Gene> hashOfGenes;
	public GFF3Parser(){
		
	}
	
	/**
	 * Parses the GFF3 file and returns a string that describes if any problems occured
	 *
	 * @param file the input GFF3 file
	 * @return a string that describes any problems encountered during parsing
	 */
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
						if(newGene.getAttributes().get("Name")!=null){
							hashOfGenes.put(newGene.getAttributes().get("Name"),newGene);	
						}else{
							hashOfGenes.put(newGene.getId(),newGene);
						}
						
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
	
	/**
	 * Gets the hash of genes (Key is the id, Value is the Gene)
	 *
	 * @return the hash of genes
	 */
	public HashMap<String, Gene> getHashOfGenes() {
		if(hashOfGenes.size()==0){
			System.err.println("Are You Sure You Parsed First?");
		}
		return hashOfGenes;
	}
	

}
