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
	 * @param iAbsoluteStart the absolute start of the junction
	 * @param iAbsoluteEnd the absolute end of the junction  
	 * @param iLeft the left scaled coordinate of a junction
	 * @param iRight the right scled coordinate of a junction
	 * @param iIsoformID the isoform this junction is a part of
	 * @param inputColor the input color
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
	
	/**
	 * Gets the absolute start of the junction
	 *
	 * @return the absolute start
	 */
	public int getAbsoluteStart(){
		return absoluteStart;
	}
	
	/**
	 * Gets the absolute end of the junction
	 *
	 * @return the absolute end
	 */
	public int getAbsoluteEnd(){
		return absoluteEnd;
	}
	
	/**
	 * Gets the isoform id that this junction is from
	 *
	 * @return the isoform id
	 */
	public String getIsoformID(){
		return isoformID;
	}
	
	/**
	 * Gets the scaled middle of the junction
	 *
	 * @return the scaled middle
	 */
	public int getScaledMiddle(){
		return (rightScaled-leftScaled)/2+leftScaled;
	}

	/**
	 * Adds a read to the array 
	 *
	 * @param read the read that spans the junction
	 */
	public void addRead(Read read) {
		listOfReads.add(read);
		hit+=1;
	}
	
	/**
	 * Gets the reads that span the junction.
	 *
	 * @return an arraylist of Reads that span the junction
	 */
	public ArrayList<Read> getSpanningReads() {
		return listOfReads;
	}
	
	/**
	 * Gets the color of the junction line
	 *
	 * @return the color
	 */
	public int getColor(){
		return color;
	}
	

}
