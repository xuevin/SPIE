package genomeBrowser;

public class Interval {
	
	private int startCoord;
	private int endCoord;
	private int length;
	public Interval(int iStartCoord, int iLength){
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
}
