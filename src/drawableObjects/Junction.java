package drawableObjects;

public class Junction {
	private int leftScaled;
	private int rightScaled;
	private int hit;
	private double weight;
	public Junction(int iLeft,int iRight,int initialHit){
		leftScaled=iLeft;
		rightScaled=iRight;
		hit=initialHit;
	}
	public int getLeftScaled(){
		return leftScaled;
	}
	public int getRightScaled(){
		return rightScaled;
	}
	public void setLeftScaled(int iLeft){
		leftScaled=iLeft;
	}
	public void setRightScaled(int iRight){
		rightScaled=iRight;	
	}
	public void incrementCount(int increment){
		hit+=increment;
	}
	public void setWeight(double iWeight){
		weight = iWeight;
	}
	public double getWeight() {
		return weight;
	}
	public int getHits() {
		return hit;
	}
	

}
