package genomeBrowser;

import gffParser.Exon;

import java.util.ArrayList;
import java.util.HashSet;

public class ShortRead {
	private HashSet<Exon> setOfExons;
	private int absoluteStart;
	private int absoluteEnd;
	private ArrayList<Interval> listOfIntervals;
	public ShortRead(ArrayList<Interval> iArrayOfInterval){
		listOfIntervals=iArrayOfInterval;
		absoluteStart=-1; 
		absoluteEnd=-1;
		for(Interval interval:listOfIntervals){
			if(absoluteStart==-1 | absoluteEnd==-1){
				absoluteStart=interval.getStartCoord();
				absoluteEnd=interval.getEndCoord();
			}
			if(interval.getStartCoord()<absoluteStart){
				absoluteStart=interval.getStartCoord();
			}
			if(interval.getEndCoord()>absoluteEnd){
				absoluteEnd=interval.getEndCoord();
			}
		}		
		setOfExons = new HashSet<Exon>();
	}
	public void addExons(Exon iExon){
		setOfExons.add(iExon);
	}
	public HashSet<Exon> getSetOfExons(){
		return setOfExons;
	}
	public boolean isJunctionRead(){
		if(setOfExons.size()>1){
			return true;
		}else{
			return false;
		}
	}
	public boolean isBodyRead(){
		if(setOfExons.size()==1){
			return true;
		}else{
			return false;
		}
	}
	public int getStart(){
		return absoluteStart;
	}
	public int getEnd(){
		return absoluteEnd;
	}
	public int getFirstExonEnd(){
		int smallestExonEnd=999999999;
		for(Exon exon:setOfExons){
			if(exon.getEnd()<smallestExonEnd){
				smallestExonEnd=exon.getEnd();
			}
		}
		return smallestExonEnd;
	}
	public int getLastExonBeginning(){
		int largestExonStart=0;
		for(Exon exon:setOfExons){
			if(exon.getStart()>largestExonStart){
				largestExonStart=exon.getStart();
			}
		}
		return largestExonStart;		
	}
	public float getLastExonEnd() {
		int largestExonEnd=0;
		for(Exon exon:setOfExons){
			if(exon.getEnd()>largestExonEnd){
				largestExonEnd=exon.getEnd();
			}
		}
		return largestExonEnd;
	}
	public float getFirstExonBeginning() {
		int smallestExonStart=999999999;
		for(Exon exon:setOfExons){
			if(exon.getStart()<smallestExonStart){
				smallestExonStart=exon.getStart();
			}
		}
		return smallestExonStart;
	}

}
