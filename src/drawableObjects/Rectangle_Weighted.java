package drawableObjects;

public class Rectangle_Weighted{

	private float weight;
	private float scaledXCoord;
	private float scaledLength;
	private int absoluteStart;
	private int absoluteEnd;
	private int color;
	private float scaledHeight;
	private String isoformID;
	
	/**
	 * Instantiates a new Rectangle_Weighted
	 * 
	 * @param iScaledXCoord the left value of the X coordinate of the rectangle
	 * @param iScaledLength the scaled length of the rectangle
	 * @param iScaledHeight the height of the rectangle
	 * @param iAbsoluteStart the genomic coordinate of the start of the region defined by the rectangle
	 * @param iAbsoluteEnd the genomic coordinate of the end of the region defined by the rectangle
	 * @param iIsoformID the ID of the MRNA that this is a part of
	 * @param iWeight the weight of the rectangle (should represent the average number of short reads that span its region)
	 * @param iColor the color of the rectangle
	 */
	public Rectangle_Weighted(float iScaledXCoord, float iScaledLength, float iScaledHeight, int iAbsoluteStart,
			int iAbsoluteEnd, String iIsoformID, float iWeight,int iColor) {
		scaledXCoord=iScaledXCoord;
		scaledLength=iScaledLength;
		weight =iWeight;
		absoluteStart=iAbsoluteStart;
		absoluteEnd=iAbsoluteEnd;
		color =iColor;
		scaledHeight=iScaledHeight;
		isoformID=iIsoformID;	
	}
	public void setWeight(float iWeight) {
		weight = iWeight;
	}
	public float getWeight() {
		return weight;
	}
	public void incrementWeight(float increment){
		weight+=increment;
	}
	public int getAbsoluteLength() {
		return (absoluteEnd-absoluteStart+1);
	}
	public int getAbsoluteStart(){
		return absoluteStart;
	}
	public int getAbsoluteEnd(){
		return absoluteEnd;
	}
	public float getScaledLength(){
		return scaledLength;
	}
	public float getScaledXCoord(){
		return scaledXCoord;
	}
	public void setScaledXCoord(float iScaledXCoord){
		scaledXCoord=iScaledXCoord;
	}
	public int getColor(){
		return color;
	}
	public float getScaledHeight(){
		return scaledHeight;
	}
	public String getIsoformID(){
		return isoformID;
	}
}
