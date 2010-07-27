package vito;

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
	public Interval(String iId,int iStartCoord, int iLength){
		id = iId;
		startCoord=iStartCoord;
		length=iLength;
		endCoord=iStartCoord+iLength-1;
	}
	public int getStartCoord(){
		return startCoord;
	}
	public int getEndCoord(){
		return endCoord;
	}
	public int getLength(){
		return length;
	}
	public boolean contains(int coord){
		if(coord>=startCoord && coord<=endCoord){
			return true;
		}else{
			return false;
		}
	}
	public String getId(){
		return id;
	}
	public void setID(String iId){
		id=iId;
	}
}
