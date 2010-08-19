package drawableObjects;

import java.util.Comparator;


/**
 * The Class RectangleComparator.
 * 
 * @author Vincent Xue
 */
public class RectangleComparator implements Comparator<Rectangle_Unweighted>{
	
	/**
	 * Instantiates a new rectangle comparator.
	 * Rectangles are sorted by their scaled x coordinates
	 */
	public RectangleComparator(){
		
	}
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Rectangle_Unweighted o1, Rectangle_Unweighted o2) {
		if(o1.getScaledXCoord()==o2.getScaledXCoord()){
			return 0;	
		}else if(o1.getScaledXCoord()<o2.getScaledXCoord()){
			return -1;
		}else{
			return 1;
		}
		
	}

}
