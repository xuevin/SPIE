package genomeBrowser;

public class GraphColumn {
	int xCoord;
	int yCoord;
	int height;
	int absoluteXStart;
	int absoluteXEnd;
	
	public GraphColumn(int iXCoord,int iYCoord, int iHeight, int iAbsoluteXStart, int iAbsoluteXEnd) {
		xCoord = iXCoord;
		yCoord = iYCoord;
		height = iHeight;
		absoluteXStart = iAbsoluteXStart;
		absoluteXEnd = iAbsoluteXEnd;
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
	public void setScaledX(int i) {
		xCoord=i;
		
	}
	public int getScaledYStart() {
		return yCoord;
	}
	public int getScaledYEnd() {
		return yCoord-height;
	}
	public int getHeight(){
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
