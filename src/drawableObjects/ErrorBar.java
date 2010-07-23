package drawableObjects;

public class ErrorBar {
	private double standardDeviation;
	private int scaledXPosition;
	private String isoformID;
	private double weight;
	private Rectangle_Weighted rectangle;
	
	/**
	 * Instantiates a new ErrorBar.
	 * 
	 * @param iIsoformID the ID of the MRNA that this error bar is a part of
	 * @param iScaledXPosition the scaled X position of the Error Bar (should be the middle of an exon)
	 * @param iRectangle the Rectangle that the error bar covers
	 * @param iWeight the weight of the error bar
	 * @param iStandardDeviation the standard deviation of the error bar
	 */
	public ErrorBar(double iWeight, double iStandardDeviation, Rectangle_Weighted iRectangle,String iIsoformID,int iScaledXPosition) {
		scaledXPosition=iScaledXPosition;
		weight=iWeight;
		standardDeviation=iStandardDeviation;
		isoformID=iIsoformID;
		rectangle = iRectangle;
	}
	public int getAbsoluteStart(){
		return rectangle.getAbsoluteStart();
	}
	public int getAbsoluteEnd(){
		return rectangle.getAbsoluteEnd();
	}
	public int getScaledXPosition(){
		return scaledXPosition;
	}
	public double getStandardDeviation(){
		return standardDeviation;
	}
	public void setScaledXCoord(int i) {
		scaledXPosition=i;
	}
	public double getWeight() {
		return weight;
	}
	public String getIsoformID(){
		return isoformID;
	}
	public void setWeight(double f){
		weight = f;
	}
	
}
