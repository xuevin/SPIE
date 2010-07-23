package drawableObjects;

public class GraphColumn {
	private int xCoord;
	private final double averageHits;
	private final int absoluteXStart;
	private final int absoluteXEnd;
	private int scaledHeight;
	
	public GraphColumn(int iXCoord, double iHits, int iAbsoluteXStart, int iAbsoluteXEnd) {
		xCoord = iXCoord;
		averageHits = iHits;
		absoluteXStart = iAbsoluteXStart;
		absoluteXEnd = iAbsoluteXEnd;
		scaledHeight=0;
	}
	public int getScaledX(){
		return xCoord;
	}
	public int getAbsoluteXStart(){
		return absoluteXStart;
		
	}
	public int getAbsoluteXEnd(){
		return absoluteXEnd;
		
	}
	public void setScaledX(int iXCoord) {
		xCoord=iXCoord;
		
	}
	public double getAverageHits(){
		return averageHits;
	}
	public void setScaledHeight(int height){
		scaledHeight=height;
	}
	public int getScaledHeight(){
		return scaledHeight;
		
	}
}
