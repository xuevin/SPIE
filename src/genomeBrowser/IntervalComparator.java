package genomeBrowser;

import java.util.Comparator;

import drawableObjects.Rectangle_Unweighted;


public class IntervalComparator implements Comparator<Interval>{
	public IntervalComparator(){
		
	}
	public int compare(Interval o1, Interval o2) {
		if(o1.getStartCoord()==o2.getStartCoord()){
			return 0;	
		}else if(o1.getStartCoord()<o2.getStartCoord()){
			return -1;
		}else{
			return 1;
		}
		
	}

}
