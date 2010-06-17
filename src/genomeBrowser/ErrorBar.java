package genomeBrowser;

import java.util.ArrayList;

public class ErrorBar {
	private double standardDeviation;
	private float scaledXPosition;
	private float scaledYPosition;
	private double mean;
	private String isoformID;
	public ErrorBar(ArrayList<Integer> arrayOfHeights,String iIsoformID,float iScaledXPosition, float iScaledYPosition) {
		scaledYPosition=iScaledYPosition;
		scaledXPosition=iScaledXPosition;
		standardDeviation=calculateSD(arrayOfHeights);
		isoformID=iIsoformID;
	}
	public float getScaledXPosition(){
		return scaledXPosition;
	}
	
	public double getStandardDeviation(){
		return standardDeviation;
	}
	private double calculateSD(ArrayList<Integer> values){
		
		int sum = 0;
		for(Integer value:values){
			sum+=value;
		}
		mean = (double)sum/values.size();
		
		double variance=0;
		for(Integer value:values){
			variance+= Math.pow((value-mean), 2);
		}
		return Math.sqrt(variance/(values.size()-1));
		
	}
	public float getScaledYPosition() {
		return scaledYPosition;
	}
	public void setScaledXCoord(float i) {
		scaledXPosition=i;
		
	}
	public double getAbsoluteHeight() {
		return mean;
	}
	public String getIsoformID(){
		return isoformID;
	}
	
}
