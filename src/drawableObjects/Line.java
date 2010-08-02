package drawableObjects;

/**
 * Line is the class that describes what an line object should have.
 * (Lines are what we see as hairlines that connect exons)
 * 
 * @author Vincent Xue
 *
 */
public class Line{
	private int xCoordStart;
	private int yCoordStart;
	private int xCoordEnd;
	private int yCoordEnd;
	private int color;
	
	/**
	 * Instantiates a new line.
	 *
	 * @param iXCoordStart the x coordinate of the start of the line
	 * @param iYCoordStart the y coordinate of the start of the line
	 * @param iXCoodEnd the x coodinate of the end of the line
	 * @param iYCoordEnd the y coordinate of the end of the line
	 * @param iColor the color of the line
	 */
	public Line(int iXCoordStart,int iYCoordStart,int iXCoodEnd,int iYCoordEnd,int iColor){
		xCoordStart = iXCoordStart;
		xCoordEnd = iXCoodEnd;
		yCoordStart = iYCoordStart;
		yCoordEnd=iYCoordEnd;
		color=iColor;
	}

	/**
	 * Instantiates a new line.
	 *
	 * @param iXCoordStart the x coordinate of the start of the line
	 * @param iYCoordStart the y coordinate of the start of the line
	 * @param iXCoodEnd the x coodinate of the end of the line
	 * @param iYCoordEnd the y coordinate of the end of the line
	 */
	public Line(int iXCoordStart,int iYCoordStart,int iXCoodEnd,int iYCoordEnd) {
		xCoordStart = iXCoordStart;
		xCoordEnd = iXCoodEnd;
		yCoordStart = iYCoordStart;
		yCoordEnd=iYCoordEnd;
		color=255;
	}

	/**
	 * Sets the x coordinate of the start of the line.
	 * (Used for flipping)
	 * @param iXCoordStart the new x coord start
	 */
	public void setXCoordStart(int iXCoordStart) {
		xCoordStart = iXCoordStart;
	}

	/**
	 * Gets the x coordinate of the start of the line.
	 *
	 * @return the x coord start
	 */
	public int getXCoordStart() {
		return xCoordStart;
	}

	/**
	 * Sets the y coordinate of the start of the line.
	 * (Used for flipping)
	 * 
	 * @param iYCoordStart the new y coord start
	 */
	public void setYCoordStart(int iYCoordStart) {
		yCoordStart = iYCoordStart;
	}

	/**
	 * Gets the y coordinate of the start of the line.
	 *
	 * @return the y coord start
	 */
	public int getYCoordStart() {
		return yCoordStart;
	}

	/**
	 * Sets the x coordinate of the end of the line.
	 * (Used for flipping)
	 *
	 * @param iXCoordEnd the new x coord end
	 */
	public void setXCoordEnd(int iXCoordEnd) {
		xCoordEnd = iXCoordEnd;
	}

	/**
	 * Gets the x coordinate of the end of the line.
	 *
	 * @return the x coord end
	 */
	public int getXCoordEnd() {
		return xCoordEnd;
	}

	/**
	 * Sets the y coordinate of the end of the line
	 * (used for flipping)
	 * 
	 * @param iYCoordEnd the new y coord end
	 */
	public void setYCoordEnd(int iYCoordEnd) {
		yCoordEnd = iYCoordEnd;
	}

	/**
	 * Gets the y coordinate of the end of the line.
	 *
	 * @return the y coord end
	 */
	public int getYCoordEnd() {
		return yCoordEnd;
	}

	/**
	 * Sets the color of the line.
	 *
	 * @param iColor the new color
	 */
	public void setColor(int iColor) {
		color = iColor;
	}
	
	/**
	 * Gets the color of the line.
	 *
	 * @return the color
	 */
	public int getColor() {
		return color;
	}
}
