package spie;

import gffParser.Exon;

import java.util.ArrayList;
import java.util.HashSet;

import net.sf.samtools.SAMRecord;

public class Read {
	private HashSet<Exon> setOfExons;
	private int absoluteStart;
	private int absoluteEnd;
	private ArrayList<Interval> listOfIntervals;
	private SAMRecord samRecord;
	public Read(SAMRecord iSamRecord, ArrayList<Interval> iArrayOfInterval){
		samRecord=iSamRecord;
		listOfIntervals=iArrayOfInterval;
		absoluteStart=iSamRecord.getAlignmentStart();
		absoluteEnd=iSamRecord.getAlignmentEnd();		
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
	public SAMRecord getSAMRecord(){
		return samRecord;
	}
}
