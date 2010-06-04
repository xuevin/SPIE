package genomeBrowser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

public class GFF3Parser {
	private HashMap<String, IDHolder> hashOfIDHolders;
	private HashMap<String, Gene> hashOfGenes;
	
	public GFF3Parser(){
		hashOfIDHolders = new HashMap<String, IDHolder>();
		hashOfGenes = new HashMap<String,Gene>();
	}
	
	/**
	 * Parses the GFF3 file and makes a structure that can be found in hashOfGenes
	 * 
	 * @param in is the buffered reader
	 */
	public void parse(BufferedReader in) {
		String line;
		try {
			while((line=in.readLine()) != null){
				if(line.startsWith("#")){
					//Skip -- This is header
				}else if (line!=null){
					String[] temp = line.split("\t");
					//Parser returns a hash of Genes.
					
					String type = temp[2]; 
					//If type is gene, make a gene
					if(type.equals("gene")){
						Gene newGene = new Gene(temp[0],temp[1],temp[2],temp[3],temp[4],temp[5],
								temp[6],temp[7],temp[8]);
						hashOfIDHolders.put(newGene.getAttributes().get("ID"),newGene);
						hashOfGenes.put(newGene.getAttributes().get("ID"),newGene);
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
						System.err.println("An unknown type was located in the GFF file");
						System.err.println(type);
					}		
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
	}
	public HashMap<String, Gene> getGenes(){
		return hashOfGenes;
	}
	

}
