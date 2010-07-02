package drawableObjects;

/**
 * The Class ConstitutiveLine_Unweighted.
 * 
 * Constitutive regions in an exon are represented using lines.
 * A line has a x,y coordinate and a height.
 * 
 */
public class ConstitutiveLine_Unweighted {
	
	/** The x coordinate of the line. */
	private float xCoord;
	
	/** The y coordinate of the line. */
	private float yCoord;
	
	/** The height of the line */
	private float height;
	
	/**
	 * Instantiates a new unweighed constitutive line
	 * 
	 * @param iXCoord the input xCoord
	 * @param iYCoord the input yCoord
	 * @param iHeight the height of the line
	 */
	public ConstitutiveLine_Unweighted(float iXCoord, int iYCoord, int iHeight){
		xCoord=iXCoord;
		yCoord=iYCoord;
		height=iHeight;
	}
	
	/**
	 * Gets the height of the line
	 * 
	 * @return the height
	 */
	public float getHeight(){
		return height;
	}
	
	/**
	 * Gets the y coord of the line
	 * 
	 * @return the y coord
	 */
	public float getYCoord(){
		return yCoord;
	}
	
	/**
	 * Gets the x coord of the line
	 * 
	 * @return the x coord
	 */
	public float getXCoord(){
		return xCoord;
	}
	
	/**
	 * Sets the x coord of the line
	 * (used when flipping)
	 * 
	 * @param iXCoord the new x coord
	 */
	public void setXCoord(float iXCoord){
		xCoord=iXCoord;
	}

}
