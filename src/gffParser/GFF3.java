package gffParser;

import java.util.HashMap;

/**
 * Each GFF3 object is a row in the GFF3 file. A GFF3 Object can be decorated, 
 * as see in CDS, Gene, Exon, StartCodon, and StopCodon
 * 
 * @author Vincent Xue
 *
 */
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
	
	/**
	 * Instantiates a new gFF3 object.
	 *
	 * @param col1 the seqID
	 * @param col2 the source
	 * @param col3 the type
	 * @param col4 the start
	 * @param col5 the end
	 * @param col6 the score
	 * @param col7 the strand
	 * @param col8 the phase
	 * @param col9 the string of atributes
	 */
	public GFF3(String col1, String col2, String col3, String col4,String col5, 
			String col6, String col7, String col8, String col9){
		seqID = col1;
		source = col2;
		type = col3;
		start = Integer.parseInt(col4);
		end = Integer.parseInt(col5);
		try{
			score = Double.parseDouble(col6);
		}catch(Exception e){
			if(col8.equals(".")){
				score = 0;
			}
		}
		if(col7.equals("+")){
			strand = true;
		}else{
			strand=false;
		}
		try{
			phase = Integer.parseInt(col8);
		}catch(Exception e){
			if(col8.equals(".")){
				phase = 0;
			}
		}
		
		attributes = new HashMap<String, String>();
		for (String subAttribute : col9.split(";")){
			String[] temp = subAttribute.split("=");
			attributes.put(temp[0],temp[1]);
		}
	}
	
	/**
	 * Gets the seq id.
	 *
	 * @return the seq id
	 */
	public String getSeqID(){
		return seqID;
	}
	
	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	public String getSource(){
		return source;
	}
	public String getType(){
		return type;
	}
	
	/**
	 * Gets the start. (Inclusive)
	 * 
	 * @return the start
	 */
	public int getStart(){
		return start;
	}
	
	/**
	 * Gets the end. (Inclusive)
	 * 
	 * @return the end
	 */
	public int getEnd(){
		return end;
	}
	
	/**
	 * Gets the score.
	 *
	 * @return the score
	 */
	public double getScore(){
		return score;
	}
	
	/**
	 * Gets the strand. (true is coding +, false is noncoding -)
	 *
	 * @return the strand
	 */
	public boolean getStrand(){
		return strand;
	}
	
	/**
	 * Gets the phase.
	 *
	 * @return the phase
	 */
	public int getPhase(){
		return phase; 
	}
	
	/**
	 * Gets the attributes.The key is the name of the identifier in the atributes. (case sensitive)
	 *
	 * @return the attributes
	 */
	public HashMap<String, String> getAttributes(){
		return attributes;
	}
	
	/**
	 * Gets the length of the sequence
	 * 
	 * @return the length
	 */
	public int getLength(){
		return (end-start)+1;
	}
}
