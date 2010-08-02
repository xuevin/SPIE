package drawableObjects;

/**
 * Rectangle_Weighted is class that describes what a
 * Rectangle_Weighted object should have.
 *  
 * @author Vincent Xue
 *
 */
public class Rectangle_Weighted{

	private double weight;
	private int scaledXCoord;
	private int scaledLength;
	private int absoluteStart;
	private int absoluteEnd;
	private int color;
	private int scaledHeight;
	private String isoformID;
	
	/**
	 * Instantiates a new Rectangle_Weighted.
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
	public Rectangle_Weighted(int iScaledXCoord, int iScaledLength, int iScaledHeight, int iAbsoluteStart,
			int iAbsoluteEnd, String iIsoformID, double iWeight,int iColor) {
		scaledXCoord=iScaledXCoord;
		scaledLength=iScaledLength;
		weight =iWeight;
		absoluteStart=iAbsoluteStart;
		absoluteEnd=iAbsoluteEnd;
		color =iColor;
		scaledHeight=iScaledHeight;
		isoformID=iIsoformID;	
	}
	
	/**
	 * Sets the weight of the rectangle
	 *
	 * @param iWeight the new weight
	 */
	public void setWeight(double iWeight) {
		weight = iWeight;
	}
	
	/**
	 * Gets the weight of the rectangle
	 *
	 * @return the weight
	 */
	public double getWeight() {
		return weight;
	}
	
	/**
	 * Gets the genomic length represented by the rectangle
	 *
	 * @return the genomic length of the rectangle
	 */
	public int getAbsoluteLength() {
		return (absoluteEnd-absoluteStart+1);
	}
	
	/**
	 * Gets the start of the genomic coordinate for region represented 
	 *
	 * @return the start of the genomic coordinate for region represented
	 */
	public int getAbsoluteStart(){
		return absoluteStart;
	}
	
	/**
	 * Gets the end of the genomic coordinate for region represented
	 *
	 * @return the start of the genomic coordinate for region represented
	 */
	public int getAbsoluteEnd(){
		return absoluteEnd;
	}
	public int getScaledLength(){
		return scaledLength;
	}
	public int getScaledXCoord(){
		return scaledXCoord;
	}
	public void setScaledXCoord(int iScaledXCoord){
		scaledXCoord=iScaledXCoord;
	}
	public int getColor(){
		return color;
	}
	public int getScaledHeight(){
		return scaledHeight;
	}
	public String getIsoformID(){
		return isoformID;
	}
}
