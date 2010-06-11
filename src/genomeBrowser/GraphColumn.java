package genomeBrowser;

public class GraphColumn {
	float xCoord;
	float yCoord;
	int height;
	int absoluteXStart;
	int absoluteXEnd;
	
	public GraphColumn(float iXCoord,float iYCoord, int iHeight, int iAbsoluteXStart, int iAbsoluteXEnd) {
		xCoord = iXCoord;
		yCoord = iYCoord;
		height = iHeight;
		absoluteXStart = iAbsoluteXStart;
		absoluteXEnd = iAbsoluteXEnd;
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
	public float getScaledYStart() {
		return yCoord;
	}
	public float getScaledYEnd() {
		return yCoord-height;
	}
	public int getUnscaledHeight(){
		return height;
	}
	public void incrementHeight(int value){
		height+=value;
	}
	public void setAbsoluteXCoords(int iAbsoluteXStart, int iAbsoluteXEnd) {
		absoluteXStart=iAbsoluteXStart;
		absoluteXEnd=iAbsoluteXEnd;
		
	}

}
