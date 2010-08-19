package gffParser;


import java.util.TreeMap;


/**
 * The MRNA class decorates a GFF3
 * It adds methods to retrieve the unique ID and to establish child relationships with other 
 * GFF3 objects
 * 
 * (MRNA-StartCodon,MRNA-StopCodon,MRNA-CDS,MRNA-Exon)
 * 
 * @author Vinny
 */
public class MRNA extends GFF3 implements IDHolder{
	private StartCodon startCodon;
	private StopCodon stopCodon;
	private TreeMap<Integer, CDS> treeCDS;
	private TreeMap<Integer, Exon> treeExon;
	private String ID;

	public MRNA(String col1, String col2, String col3, String col4,
			String col5, String col6, String col7, String col8, String col9) {
		super(col1, col2, col3, col4, col5, col6, col7, col8, col9);
		ID = getAttributes().get("ID");
		startCodon = null;
		stopCodon = null;
		treeExon= new TreeMap<Integer, Exon>();
		treeCDS = new TreeMap<Integer, CDS>();
	}
	
	/* (non-Javadoc)
	 * @see gffParser.IDHolder#getId()
	 */
	public String getId() {
		return ID;
	}
	
	/* (non-Javadoc)
	 * @see gffParser.IDHolder#addChild(gffParser.GFF3)
	 */
	public void addChild(GFF3 child) {
		if(child instanceof StartCodon){
			startCodon = (StartCodon) child;
		}else if(child instanceof StopCodon){
			stopCodon = (StopCodon) child;
		}else if(child instanceof CDS){
			treeCDS.put(child.getStart(), (CDS) child);
		}else if(child instanceof Exon){
			treeExon.put(child.getStart(), (Exon) child);
		}else{
			System.err.println("Some Critical Error Occured. Please Look at MRNA.java");
		}
	}
	
	/**
	 * Gets the start codon.
	 *
	 * @return the start codon
	 */
	public StartCodon getStartCodon(){
		return startCodon;
	}
	
	/**
	 * Gets the stop codon.
	 *
	 * @return the stop codon
	 */
	public StopCodon getStopCodon(){
		return stopCodon;
	}
	/**
	 * Gets the CDS. (the start position of the CDS is the key)
	 *
	 * @return the cDS
	 */
	public TreeMap<Integer,CDS> getCDS(){
		return treeCDS;
	}
	/**
	 * Gets the TreeMap of Exons (the start position of the exon is the key).
	 *
	 * @return the exons
	 */
	public TreeMap<Integer,Exon> getExons(){
		return treeExon;
		
	}
}
