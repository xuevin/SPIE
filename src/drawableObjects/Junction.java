package drawableObjects;

import java.util.ArrayList;

import spie.Read;


/**
 * Junction is class that describes what an junction object should have.
 * 
 * @author Vincent Xue
 *
 */
public class Junction {
	private int leftScaled;
	private int rightScaled;
	private int absoluteStart;
	private int absoluteEnd;
	private int hit;
	private double weight;
	private String isoformID;
	private ArrayList<Read> listOfReads;
	private int color;
	
	/**
	 * Instantiates a new junction.
	 *
	 * @param iLeft the left scaled coordinate of a junction
	 * @param iRight the right scled coordinate of a junction
	 * @param initialHit the initial number of short reads that cross the junction
	 */
	public Junction(int iAbsoluteStart, int iAbsoluteEnd,int iLeft,int iRight,String iIsoformID,int inputColor){
		absoluteStart=iAbsoluteStart;
		absoluteEnd=iAbsoluteEnd;
		leftScaled=iLeft;
		rightScaled=iRight;
		hit=0;
		isoformID=iIsoformID;
		listOfReads=new ArrayList<Read>();
		color = inputColor;
	}
	
	/**
	 * Gets the left scaled coordinate of the junction
	 *
	 * @return the left scaled coordinate of the junction
	 */
	public int getLeftScaled(){
		return leftScaled;
	}
	
	/**
	 * Gets the right scaled coordinate of the junction
	 *
	 * @return the right scaled coordinate of the junction
	 */
	public int getRightScaled(){
		return rightScaled;
	}
	
	/**
	 * Sets the left scaled coordinate of the junction
	 * (used for flipping)
	 *
	 * @param iLeft the new left scaled coordinate
	 */
	public void setLeftScaled(int iLeft){
		leftScaled=iLeft;
	}
	
	/**
	 * Sets the right scaled coordinate of the junction
	 * (Used for flipping)
	 * 
	 * @param iRight the new right scaled coordinate
	 */
	public void setRightScaled(int iRight){
		rightScaled=iRight;	
	}
	/**
	 * Sets the weight of the junction.
	 *
	 * @param iWeight the new weight
	 */
	public void setWeight(double iWeight){
		weight = iWeight;
	}
	
	/**
	 * Gets the weight of the junction
	 *
	 * @return the weight of the junction
	 */
	public double getWeight() {
		return weight;
	}
	
	/**
	 * Gets the raw number of hits for the junction
	 *
	 * @return the hits
	 */
	public int getHits() {
		return hit;
	}
	public int getAbsoluteStart(){
		return absoluteStart;
	}
	public int getAbsoluteEnd(){
		return absoluteEnd;
	}
	public String getIsoformID(){
		return isoformID;
	}
	public int getScaledMiddle(){
		return (rightScaled-leftScaled)/2+leftScaled;
	}

	public void addRead(Read read) {
		listOfReads.add(read);
		hit+=1;
	}
	public ArrayList<Read> getCompatibleReads() {
		return listOfReads;
	}
	public int getColor(){
		return color;
	}
	

}
