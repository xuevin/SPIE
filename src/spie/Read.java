package spie;

import gffParser.Exon;

import java.util.ArrayList;
import java.util.HashSet;

import net.sf.samtools.SAMRecord;

/**
 * The Class Read adds more functions to a SAMFileRecord 
 * (Contains information whether or not a read is junction or body read)
 */
public class Read {
	private HashSet<Exon> setOfExons;
	private int absoluteStart;
	private int absoluteEnd;
	private ArrayList<Interval> listOfIntervals;
	private SAMRecord samRecord;
	
	/**
	 * Instantiates a new read.
	 *
	 * @param iSamRecord the i sam record
	 * @param iArrayOfInterval an array of intervals (Based off the cigar string)
	 */
	public Read(SAMRecord iSamRecord, ArrayList<Interval> iArrayOfInterval){
		samRecord=iSamRecord;
		listOfIntervals=iArrayOfInterval;
		absoluteStart=iSamRecord.getAlignmentStart();
		absoluteEnd=iSamRecord.getAlignmentEnd();		
		setOfExons = new HashSet<Exon>();
	}
	
	/**
	 * Adds an exon that this read covers
	 *
	 * @param iExon the i exon
	 */
	public void addExons(Exon iExon){
		setOfExons.add(iExon);
	}
	
	/**
	 * Gets the set of exons this read covers
	 *
	 * @return the sets the of exons
	 */
	public HashSet<Exon> getSetOfExons(){
		return setOfExons;
	}
	
	/**
	 * Checks if is junction read.
	 *
	 * @return true, if is junction read
	 */
	public boolean isJunctionRead(){
		if(setOfExons.size()>1){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if is body read.
	 *
	 * @return true, if is body read
	 */
	public boolean isBodyRead(){
		if(setOfExons.size()==1){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Gets the start of the read (inclusive)
	 *
	 * @return the start
	 */
	public int getStart(){
		return absoluteStart;
	}
	
	/**
	 * Gets the end of the read (inclusive)
	 *
	 * @return the end
	 */
	public int getEnd(){
		return absoluteEnd;
	}
	
	/**
	 * Gets the end coordinate of the first exon.
	 *
	 * @return the first exon end
	 */
	public int getFirstExonEnd(){
		int smallestExonEnd=999999999;
		for(Exon exon:setOfExons){
			if(exon.getEnd()<smallestExonEnd){
				smallestExonEnd=exon.getEnd();
			}
		}
		return smallestExonEnd;
	}
	
	/**
	 * Gets the beginning of the last exon
	 *
	 * @return the last exon beginning
	 */
	public int getLastExonBeginning(){
		int largestExonStart=0;
		for(Exon exon:setOfExons){
			if(exon.getStart()>largestExonStart){
				largestExonStart=exon.getStart();
			}
		}
		return largestExonStart;		
	}
	
	/**
	 * Gets the SAM record. 
	 *
	 * @return the SAM record
	 */
	public SAMRecord getSAMRecord(){
		return samRecord;
	}
}
