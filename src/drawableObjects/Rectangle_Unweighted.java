package drawableObjects;


/**
 * The Rectangle_Unweighted is the class that describes what properties 
 * a Rectangle_Unweighted object should have
 * 
 * @author Vincent Xue
 */
public class Rectangle_Unweighted{
	private int xScaledCoord;
	private int yScaledCoord;
	private int scaledLength;
	private int color;
	private int scaledHeight;
	private int absoluteStart;
	private int absoluteEnd;
	private String isoformID;
	
	/**
	 * Instantiates a new Rectangle.
	 * 
	 * @param iScaledXCoord the left value of the X coordinate of the rectangle
	 * @param iScaledYCoord the top value of the Y coordinate of the rectangle
	 * @param iScaledLength the scaled length of the rectangle 
	 * @param iScaledHeight the scaled height of the rectangle
	 * @param iAbsoluteStart the genomic start coordinate of the region defined by the rectangle (inclusive)
	 * @param iAbsoluteEnd the genomic end coordinate of the region defined by the rectangle (inclusive)
	 * @param iIsoformID the ID of the MRNA that this rectangle is a part of
	 * @param iColor the color of the rectangle
	 */
	public Rectangle_Unweighted(int iScaledXCoord,int iScaledYCoord,int iScaledLength, 
			int iScaledHeight,int iAbsoluteStart, int iAbsoluteEnd,String iIsoformID,int iColor){
		xScaledCoord=iScaledXCoord;
		yScaledCoord=iScaledYCoord;
		scaledLength=iScaledLength;
		scaledHeight=iScaledHeight;
		color=iColor;
		absoluteStart=iAbsoluteStart;
		absoluteEnd=iAbsoluteEnd;
		isoformID=iIsoformID;
	}
	
	/**
	 * Gets the scaled length of the rectangle
	 *
	 * @return the scaled length
	 */
	public int getScaledLength() {
		return scaledLength;
	}
	
	/**
	 * Sets the color of the rectangle
	 *
	 * @param iColor the new color
	 */
	public void setColor(int iColor) {
		color = iColor;
	}
	
	/**
	 * Gets the color of the rectangle
	 *
	 * @return the color
	 */
	public int getColor() {
		return color;
	}
	
	/**
	 * Sets the scaled height of the rectangle
	 *
	 * @param iHeight the new scaled height
	 */
	public void setScaledHeight(int iHeight) {
		scaledHeight = iHeight;
	}
	
	/**
	 * Gets the scaled height of the rectangle.
	 *
	 * @return the scaled height
	 */
	public int getScaledHeight() {
		return scaledHeight;
	}
	
	/**
	 * Sets the scaled x coordinate.
	 *
	 * @param iXCoord the new scaled x coord
	 */
	public void setScaledXCoord(int iXCoord) {
		xScaledCoord = iXCoord;
	}
	
	/**
	 * Gets the scaled x coordinate.
	 *
	 * @return the scaled x coord
	 */
	public int getScaledXCoord() {
		return xScaledCoord;
	}
	
	/**
	 * Sets the scaled y coordinate.
	 *
	 * @param iYCoord the new scaled y coord
	 */
	public void setScaledYCoord(int iYCoord) {
		yScaledCoord = iYCoord;
	}
	
	/**
	 * Gets the scaled y coordinate.
	 *
	 * @return the scaled y coord
	 */
	public int getScaledYCoord() {
		return yScaledCoord;
	}
	/**
	 * Gets the genomic start coordinate
	 *
	 * @return the absolute start
	 */
	public int getAbsoluteStart() {
		return absoluteStart;
	}	

	/**
	 * Gets the genomic end coordinate
	 *
	 * @return the absolute end
	 */
	public int getAbsoluteEnd() {
		return absoluteEnd;
	}
	
	/**
	 * Gets the isoform id for this rectangle.
	 *
	 * @return the isoform id
	 */
	public String getIsoformID(){
		return isoformID;
	}
}
