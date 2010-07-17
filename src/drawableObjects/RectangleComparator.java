package drawableObjects;

import java.util.Comparator;


public class RectangleComparator implements Comparator<Rectangle_Unweighted>{
	public RectangleComparator(){
		
	}
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
