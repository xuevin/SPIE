package gffParser;


import java.util.HashMap;


public class Gene extends GFF3 implements IDHolder {
	String ID;
	HashMap<String,MRNA> hashMRNA;
	public Gene(String col1, String col2, String col3, String col4,String col5, 
			String col6, String col7, String col8, String col9){
		super(col1,col2,col3,col4,col5,col6,col7,col8,col9);
		ID = getAttributes().get("ID");
		hashMRNA = new HashMap<String, MRNA>();
	}
	public String getId() {
		return ID;
	}
	public void addChild(GFF3 child) {
		if(child instanceof MRNA){
			hashMRNA.put(((MRNA) child).getId(), (MRNA) child);
		}
	}
	public HashMap<String,MRNA> getMRNA(){
		return hashMRNA;
	}
	
}
