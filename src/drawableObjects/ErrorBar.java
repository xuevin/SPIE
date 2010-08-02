package drawableObjects;

/**
 * Error Bar is class that describes what an error bar object should have.
 *  
 * @author Vincent Xue
 */
public class ErrorBar {
	
	private double standardDeviation;
	private int scaledXPosition;
	private String isoformID;
	private double weight;
	private Rectangle_Weighted rectangle;
	private int absoluteStart;
	private int absoluteEnd;
	
	/**
	 * Instantiates a new ErrorBar.
	 *
	 * @param iWeight the weight of the error bar
	 * @param iStandardDeviation the standard deviation of the error bar
	 * @param iAbsoluteStart the genomic start of the region that is being considered
	 * @param iAbsoluteEnd the genomic end of the region that is being considered
	 * @param iIsoformID the ID of the MRNA that this error bar is a part of
	 * @param iScaledXPosition the scaled X position of the Error Bar (should be the middle of an exon)
	 */
	public ErrorBar(double iWeight, double iStandardDeviation, int iAbsoluteStart, int iAbsoluteEnd,String iIsoformID,int iScaledXPosition) {
		scaledXPosition=iScaledXPosition;
		weight=iWeight;
		standardDeviation=iStandardDeviation;
		isoformID=iIsoformID;
		absoluteStart = iAbsoluteStart;
		absoluteEnd = iAbsoluteEnd;
	}
	/**
	 * Gets the absolute start.
	 *
	 * @return the absolute start
	 */
	public int getAbsoluteStart(){
		return absoluteStart;
	}
	
	/**
	 * Gets the absolute end.
	 *
	 * @return the absolute end
	 */
	public int getAbsoluteEnd(){
		return absoluteEnd;
	}
	
	/**
	 * Gets the scaled x position.
	 *
	 * @return the scaled x position
	 */
	public int getScaledXPosition(){
		return scaledXPosition;
	}
	
	/**
	 * Gets the standard deviation.
	 *
	 * @return the standard deviation
	 */
	public double getStandardDeviation(){
		return standardDeviation;
	}
	
	/**
	 * Sets the scaled x coord.
	 * (used for flipping)
	 *
	 * @param i the new scaled x coord
	 */
	public void setScaledXCoord(int i) {
		scaledXPosition=i;
	}
	
	/**
	 * Gets the weight.
	 *
	 * @return the weight
	 */
	public double getWeight() {
		return weight;
	}
	
	/**
	 * Gets the isoform id.
	 *
	 * @return the isoform id
	 */
	public String getIsoformID(){
		return isoformID;
	}
	
	/**
	 * Sets the weight.
	 *
	 * @param f the new weight
	 */
	public void setWeight(double f){
		weight = f;
	}
}
