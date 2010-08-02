package drawableObjects;

/**
 * GraphColumn is class that describes what an graphColumn object should have.
 * 
 * A graph column is a pixel represntation of a span of genomic coordinates.
 * Usually, when viewing a gene, there are not enough pixels to represent each 
 * genomic coordinate. This graph column represents this span and the height represents 
 * an average of the hits for the span.
 * 
 * @author Vincent Xue
 */
public class GraphColumn {
	private int xCoord;
	private final double averageHits;
	private final int absoluteXStart;
	private final int absoluteXEnd;
	private int scaledHeight;
	
	/**
	 * Instantiates a new graph column.
	 *
	 * @param iXCoord the scaled pixel coordinate
	 * @param iAverageHits the average hits for the genomic span
	 * @param iAbsoluteXStart the genomic coordinate of the start of the span
	 * @param iAbsoluteXEnd the genomic coordinate of the end of the span
	 */
	public GraphColumn(int iXCoord, double iAverageHits, int iAbsoluteXStart, int iAbsoluteXEnd) {
		xCoord = iXCoord;
		averageHits = iAverageHits;
		absoluteXStart = iAbsoluteXStart;
		absoluteXEnd = iAbsoluteXEnd;
		scaledHeight=0;
	}
	
	/**
	 * Gets the scaled x pixel coordinate.
	 *
	 * @return the scaled x pixel coordinate
	 */
	public int getScaledX(){
		return xCoord;
	}
	
	/**
	 * Gets the genomic coordinate of the start of the span.
	 *
	 * @return the genomic coordinate of the start of the span.
	 */
	public int getAbsoluteXStart(){
		return absoluteXStart;
		
	}
	
	/**
	 * Gets the genomic coordinate of the end of the span
	 *
	 * @return the genomic coordinate of the end of the span
	 */
	public int getAbsoluteXEnd(){
		return absoluteXEnd;
		
	}
	
	/**
	 * Sets the scaled position of the x pixel
	 * (used for flipping)
	 *
	 * @param iXCoord the new scaled x coordinate
	 */
	public void setScaledX(int iXCoord) {
		xCoord=iXCoord;
	}
	
	/**
	 * Gets the average hits for the span that the pixel covers
	 *
	 * @return the average hits for the span that the pixel covers
	 */
	public double getAverageHits(){
		return averageHits;
	}
	
	/**
	 * Sets the scaled height of the column. (in pixels)
	 * (used for scaling the height of the histogram)
	 *
	 * @param height the new scaled height
	 */
	public void setScaledHeight(int height){
		scaledHeight=height;
	}
	
	/**
	 * Gets the scaled height of the column (in pixels)
	 *
	 * @return the scaled height
	 */
	public int getScaledHeight(){
		return scaledHeight;
		
	}
}
