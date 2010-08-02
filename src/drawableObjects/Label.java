package drawableObjects;


/**
 * Label is the class that describes what an Label object should have.
 * 
 * @author Vincent Xue
 *
 */
public class Label {
	
	private int xScaled;
	private int yScaled;
	private String text;
	
	/**
	 * Instantiates a new label.
	 *
	 * @param iText the text of the label
	 * @param iXScaled the xCoordinate of the label
	 * @param iYScaled the yCoordinate of the label
	 */
	public Label(String iText, int iXScaled, int iYScaled){
		text =iText;
		xScaled=iXScaled;
		yScaled=iYScaled;
	}
	
	/**
	 * Gets the text.
	 *
	 * @return the text
	 */
	public String getText(){
		return text;
	}
	
	/**
	 * Gets the x scaled coordinate
	 *
	 * @return the x scaled coordinate
	 */
	public int getXScaled(){
		return xScaled;
	}
	
	/**
	 * Gets the y scaled coordinate
	 *
	 * @return the y scaled coordinate
	 */
	public int getYScaled(){
		return yScaled;
	}
	

}
