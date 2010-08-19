package spie;

/**
 * The Class Interval. 
 * 
 */
public class Interval {
	
	private int startCoord;
	private int endCoord;
	private int length;
	private String id;
	/**
	 * @param iStartCoord the start of the Interval (Incusive)
	 * @param iLength the length of the Interval (Inclusive) (meaning that 1 base has a length of 1)
	 */
	public Interval(int iStartCoord, int iLength){
		id = "null";
		startCoord=iStartCoord;
		length=iLength;
		endCoord=iStartCoord+iLength-1;
	}
	
	/**
	 * Instantiates a new interval.
	 *
	 * @param iId the ID of the gene
	 * @param iStartCoord the i start coord
	 * @param iLength the i length
	 */
	public Interval(String iId,int iStartCoord, int iLength){
		id = iId;
		startCoord=iStartCoord;
		length=iLength;
		endCoord=iStartCoord+iLength-1;
	}
	
	/**
	 * Gets the start coord.
	 *
	 * @return the start coord
	 */
	public int getStartCoord(){
		return startCoord;
	}
	
	/**
	 * Gets the end coord.
	 *
	 * @return the end coord
	 */
	public int getEndCoord(){
		return endCoord;
	}
	
	/**
	 * Gets the length of the interval
	 *
	 * @return the length
	 */
	public int getLength(){
		return length;
	}
	
	/**
	 * Tests if the interval contains the coordinate.
	 *
	 * @param coord the coord
	 * @return true, if successful
	 */
	public boolean contains(int coord){
		if(coord>=startCoord && coord<=endCoord){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Gets the id (the gene from which the interval came from)
	 *
	 * @return the id
	 */
	public String getId(){
		return id;
	}
	
	/**
	 * Sets the ID. 
	 *
	 * @param iId the new ID
	 */
	public void setID(String iId){
		id=iId;
	}
}
