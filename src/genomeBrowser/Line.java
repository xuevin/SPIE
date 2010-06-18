package genomeBrowser;

public class Line{
	private float xCoordStart;
	private float yCoordStart;
	private float xCoordEnd;
	private float yCoordEnd;
	private int color;
	
	public Line(float iXCoordStart,float iYCoordStart,float iXCoodEnd,float iYCoordEnd,int iColor){
		xCoordStart = iXCoordStart;
		xCoordEnd = iXCoodEnd;
		yCoordStart = iYCoordStart;
		yCoordEnd=iYCoordEnd;
		color=iColor;
	}

	public Line(float iXCoordStart,float iYCoordStart,float iXCoodEnd,float iYCoordEnd) {
		xCoordStart = iXCoordStart;
		xCoordEnd = iXCoodEnd;
		yCoordStart = iYCoordStart;
		yCoordEnd=iYCoordEnd;
		color=255;
	}

	public void setXCoordStart(float iXCoordStart) {
		xCoordStart = iXCoordStart;
	}

	public float getXCoordStart() {
		return xCoordStart;
	}

	public void setYCoordStart(float iYCoordStart) {
		yCoordStart = iYCoordStart;
	}

	public float getYCoordStart() {
		return yCoordStart;
	}

	public void setXCoordEnd(float iXCoordEnd) {
		xCoordEnd = iXCoordEnd;
	}

	public float getXCoordEnd() {
		return xCoordEnd;
	}

	public void setYCoordEnd(float iYCoordEnd) {
		yCoordEnd = iYCoordEnd;
	}

	public float getYCoordEnd() {
		return yCoordEnd;
	}

	public void setColor(int iColor) {
		color = iColor;
	}
	public int getColor() {
		return color;
	}
	/**
	 * @param Decreases the Y Coord End By specified amount
	 */
	public void incrementHeight(int value){
		yCoordEnd-=value;
	}	
}
