package drawableObjects;

public class Junction {
	float leftScaled;
	float rightScaled;
	int hit;
	float weight;
	public Junction(float iLeft,float iRight,int initialHit){
		leftScaled=iLeft;
		rightScaled=iRight;
		hit=initialHit;
	}
	public float getLeftScaled(){
		return leftScaled;
	}
	public float getRightScaled(){
		return rightScaled;
	}
	public void setLeftScaled(float iLeft){
		leftScaled=iLeft;
	}
	public void setRightScaled(float iRight){
		rightScaled=iRight;	
	}
	public void incrementCount(int increment){
		hit+=increment;
	}
	public void setWeight(float iWeight){
		weight = iWeight;
	}
	public float getWeight() {
		return weight;
	}
	public float getHits() {
		return hit;
	}
	

}
