package drawableObjects;

public class GraphColumn {
	private float xCoord;
	private int unscaledHeight;
	private int absoluteXStart;
	private int absoluteXEnd;
	private int scaledHeight;
	
	public GraphColumn(float iXCoord, int iHeight, int iAbsoluteXStart, int iAbsoluteXEnd) {
		xCoord = iXCoord;
		unscaledHeight = iHeight;
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
	public int getUnscaledHeight(){
		return unscaledHeight;
	}
	public void setScaledHeight(int iScaledHeight){
		scaledHeight=iScaledHeight;
	}
	public int getScaledHeight(){
		return scaledHeight;
		
	}
	public void incrementHeight(int value){
		unscaledHeight+=value;
	}
	public void setAbsoluteXCoords(int iAbsoluteXStart, int iAbsoluteXEnd) {
		absoluteXStart=iAbsoluteXStart;
		absoluteXEnd=iAbsoluteXEnd;
		
	}

}
