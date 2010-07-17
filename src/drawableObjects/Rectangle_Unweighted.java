package drawableObjects;


public class Rectangle_Unweighted{
	private float xScaledCoord;
	private float yScaledCoord;
	private float length;
	private int color;
	private float height;
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
	public Rectangle_Unweighted(float iScaledXCoord,float iScaledYCoord,float iScaledLength, 
			float iScaledHeight,int iAbsoluteStart, int iAbsoluteEnd,String iIsoformID,int iColor){
		xScaledCoord=iScaledXCoord;
		yScaledCoord=iScaledYCoord;
		length=iScaledLength;
		height=iScaledHeight;
		color=iColor;
		absoluteStart=iAbsoluteStart;
		absoluteEnd=iAbsoluteEnd;
		isoformID=iIsoformID;
	}
	public void setScaledLength(int iLength) {
		length = iLength;
	}
	public float getScaledLength() {
		return length;
	}
	public void setColor(int iColor) {
		color = iColor;
	}
	public int getColor() {
		return color;
	}
	public void setScaledHeight(int iHeight) {
		height = iHeight;
	}
	public float getScaledHeight() {
		return height;
	}
	public void setScaledXCoord(float iXCoord) {
		xScaledCoord = iXCoord;
	}
	public float getScaledXCoord() {
		return xScaledCoord;
	}
	public void setScaledYCoord(float iYCoord) {
		yScaledCoord = iYCoord;
	}
	public float getScaledYCoord() {
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
