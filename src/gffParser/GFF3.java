package gffParser;

import java.util.HashMap;


public class GFF3 {
	private String seqID; 	//Col 1
	private String source;	//Col 2
	private String type; 	//Col 3
	private int start;		//Col 4
	private int end;		//Col 5
	private double score;	//Col 6 
	private boolean strand; //Col 7
	private int phase;		//Col 8
	private HashMap <String, String> attributes; //Col 9
	
	GFF3(String col1, String col2, String col3, String col4,String col5, 
			String col6, String col7, String col8, String col9){
		seqID = col1;
		source = col2;
		type = col3;
		start = Integer.parseInt(col4);
		end = Integer.parseInt(col5);
		try{
			score = Double.parseDouble(col6);
		}catch(Exception e){
			if(col8=="."){
				score = 0;
			}
		}	
		if(col7=="+"){
			strand = true;
		}else{
			strand=false;
		}
		try{
			phase = Integer.parseInt(col8);
		}catch(Exception e){
			if(col8=="."){
				phase = 0;
			}
		}
		
		attributes = new HashMap<String, String>();
		for (String subAttribute : col9.split(";")){
			String[] temp = subAttribute.split("=");
			attributes.put(temp[0],temp[1]);
		}
	}
	public String getSeqID(){
		return seqID;
	}
	public String getSource(){
		return source;
	}
	public String getType(){
		return type;
	}
	public int getStart(){
		return start;
	}
	public int getEnd(){
		return end;
	}
	public double getScore(){
		return score;
	}
	public boolean getStrand(){
		return strand;
	}
	public int getPhase(){
		return phase; 
	}
	public HashMap<String, String> getAttributes(){
		return attributes;
	}
	public int getLength(){
		return (end-start)+1;
	}
}
