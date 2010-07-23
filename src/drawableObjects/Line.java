package drawableObjects;

public class Line{
	private int xCoordStart;
	private int yCoordStart;
	private int xCoordEnd;
	private int yCoordEnd;
	private int color;
	
	public Line(int iXCoordStart,int iYCoordStart,int iXCoodEnd,int iYCoordEnd,int iColor){
		xCoordStart = iXCoordStart;
		xCoordEnd = iXCoodEnd;
		yCoordStart = iYCoordStart;
		yCoordEnd=iYCoordEnd;
		color=iColor;
	}

	public Line(int iXCoordStart,int iYCoordStart,int iXCoodEnd,int iYCoordEnd) {
		xCoordStart = iXCoordStart;
		xCoordEnd = iXCoodEnd;
		yCoordStart = iYCoordStart;
		yCoordEnd=iYCoordEnd;
		color=255;
	}

	public void setXCoordStart(int iXCoordStart) {
		xCoordStart = iXCoordStart;
	}

	public int getXCoordStart() {
		return xCoordStart;
	}

	public void setYCoordStart(int iYCoordStart) {
		yCoordStart = iYCoordStart;
	}

	public int getYCoordStart() {
		return yCoordStart;
	}

	public void setXCoordEnd(int iXCoordEnd) {
		xCoordEnd = iXCoordEnd;
	}

	public int getXCoordEnd() {
		return xCoordEnd;
	}

	public void setYCoordEnd(int iYCoordEnd) {
		yCoordEnd = iYCoordEnd;
	}

	public int getYCoordEnd() {
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
