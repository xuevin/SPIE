package genomeBrowser;

public class Junction {
	float leftScaled;
	float rightScaled;
	int hit;
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
	public void increaseHit(){
		hit++;
	}
	public int getHits() {
		return hit;
	}
	

}
