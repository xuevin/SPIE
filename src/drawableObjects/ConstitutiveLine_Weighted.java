package drawableObjects;

public class ConstitutiveLine_Weighted{
	private float weight;
	private float height;
	private float xCoord;
	
	/**
	 * Instantiates a new constitutiveLine_Weighted.
	 * 
	 * @param iXCoord the scaled x position the line
	 * @param iHeight the height of the line
	 * @param iWeight the weight of the line
	 */
	public ConstitutiveLine_Weighted(float iXCoord,float iHeight,float iWeight) {
		//super(iXCoordStart, 0, iXCoordStart, 0);
		height = iHeight;
		weight=iWeight;
		xCoord=iXCoord;
	}
	public ConstitutiveLine_Weighted(float iXCoord,float iHeight,float iWeight,int color) {
		//super(iXCoordStart, 0, iXCoordStart, 0,color);
		height = iHeight;
		weight=iWeight;
	}
	public void setWeight(int iWeight) {
		weight=iWeight;
	}
	public float getWeight() {
		return weight;
	}
	public void incrementWeight(float increment){
		weight+=increment;		
	}
	public float getVerticalLength(){
		return height;
	}
	public float getXCoord(){
		return xCoord;
	}
	public void setXCoord(float iXCoord) {
		xCoord=iXCoord;
		
	}
	public void setWeight(float f) {
		weight=f;
		
	}

	
	

}
