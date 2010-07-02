package drawableObjects;

public class ErrorBar {
	private double standardDeviation;
	private float scaledXPosition;
	private String isoformID;
	private float weight;
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
	public ErrorBar(float iWeight, float iStandardDeviation, Rectangle_Weighted iRectangle,String iIsoformID,float iScaledXPosition) {
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
	public float getScaledXPosition(){
		return scaledXPosition;
	}
	public double getStandardDeviation(){
		return standardDeviation;
	}
	public void setScaledXCoord(float i) {
		scaledXPosition=i;
	}
	public float getWeight() {
		return weight;
	}
	public String getIsoformID(){
		return isoformID;
	}
	public void setWeight(float f){
		weight = f;
	}
	
}
