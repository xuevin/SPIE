package drawableObjects;


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
	 * @param iScaledYCoord the top  value of the Y coordinate of the rectangle
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
	public void setScaledLength(int iLength) {
		scaledLength = iLength;
	}
	public int getScaledLength() {
		return scaledLength;
	}
	public void setColor(int iColor) {
		color = iColor;
	}
	public int getColor() {
		return color;
	}
	public void setScaledHeight(int iHeight) {
		scaledHeight = iHeight;
	}
	public int getScaledHeight() {
		return scaledHeight;
	}
	public void setScaledXCoord(int iXCoord) {
		xScaledCoord = iXCoord;
	}
	public int getScaledXCoord() {
		return xScaledCoord;
	}
	public void setScaledYCoord(int iYCoord) {
		yScaledCoord = iYCoord;
	}
	public int getScaledYCoord() {
		return yScaledCoord;
	}
	public void setAbsoluteStart(int iAbsoluteStart) {
		absoluteStart = iAbsoluteStart;
	}
	public int getAbsoluteStart() {
		return absoluteStart;
	}

	public void setAbsoluteEnd(int iAbsoluteEnd) {
		absoluteEnd = iAbsoluteEnd;
	}

	public int getAbsoluteEnd() {
		return absoluteEnd;
	}
	public String getIsoformID(){
		return isoformID;
	}
}
