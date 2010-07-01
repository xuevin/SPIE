package drawableObjects;

public class ConstitutiveLine_Unweighted {
	private float xCoord;
	private float yCoord;
	private float height;
	public ConstitutiveLine_Unweighted(float iXCoord, int iYCoord, int iHeight){
		xCoord=iXCoord;
		yCoord=iYCoord;
		height=iHeight;
	}
	public float getHeight(){
		return height;
	}
	public float getYCoord(){
		return yCoord;
	}
	public float getXCoord(){
		return xCoord;
	}
	public void setXCoord(float iXCoord){
		xCoord=iXCoord;
	}

}
