package drawableObjects;

public class GraphColumn {
	private float xCoord;
	private float averageHits;
	private int absoluteXStart;
	private int absoluteXEnd;
	private float scaledHeight;
	
	public GraphColumn(float iXCoord, int iHits, int iAbsoluteXStart, int iAbsoluteXEnd) {
		xCoord = iXCoord;
		averageHits = iHits;
		absoluteXStart = iAbsoluteXStart;
		absoluteXEnd = iAbsoluteXEnd;
		scaledHeight=0;
	}
	public float getScaledX(){
		return xCoord;
	}
	public int getAbsoluteXStart(){
		return absoluteXStart;
		
	}
	public int getAbsoluteXEnd(){
		return absoluteXEnd;
		
	}
	public void setScaledX(float iXCoord) {
		xCoord=iXCoord;
		
	}
	public float getAverageHits(){
		return averageHits;
	}
	public void setScaledHeight(float f){
		scaledHeight=f;
	}
	public float getScaledHeight(){
		return scaledHeight;
		
	}
	public void incrementHeight(float value){
		averageHits+=value;
	}
	public void setAbsoluteXCoords(int iAbsoluteXStart, int iAbsoluteXEnd) {
		absoluteXStart=iAbsoluteXStart;
		absoluteXEnd=iAbsoluteXEnd;
		
	}

}
