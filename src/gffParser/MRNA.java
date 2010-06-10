package gffParser;

import genomeBrowser.IDHolder;

import java.util.TreeMap;


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
	public String getId() {
		return ID;
	}
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
	public StartCodon getStartCodon(){
		return startCodon;
	}
	public StopCodon getStopCodon(){
		return stopCodon;
	}
	public TreeMap<Integer,CDS> getCDS(){
		return treeCDS;
	}
	public TreeMap<Integer,Exon> getExons(){
		return treeExon;
		
	}
}
