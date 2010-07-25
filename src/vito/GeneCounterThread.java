package vito;

import gffParser.Gene;

import java.util.ArrayList;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.util.CloseableIterator;

class GeneCounterThread implements Runnable{
	private Gene gene;
	private SAMFileReader samReader;
	private ArrayList<Integer> arrayOfLengths;
	
	public GeneCounterThread(Gene iGene,SAMFileReader iSamReader,ArrayList<Integer>iArrayOfLengths) {
		samReader=iSamReader;
		gene =iGene;
		arrayOfLengths=iArrayOfLengths;
	}
	
	public void run() {
		
		System.out.println("Fetching Constitutive Bases for " + gene.getId());
		//Get the bases
		ArrayList<Integer> cBases = Statistics.getConstituitiveBases(gene.getMRNA().values());
												
		//Then get the Intervals
		System.out.println("Fetching Constitutive Intervals for " + gene.getId());
		ArrayList<Interval> intervals = Statistics.getConstitutiveIntervals(cBases);
		
		//For every interval get the shortreads
		System.out.println("Iterating through "+ intervals.size() + " intervals");
		for(Interval interval:intervals){
			if(interval.getLength()>1000){
				System.out.println("Found an interval of " + interval.getLength());
				//For every interval greater than 1000 bp, get the short reads
				CloseableIterator<SAMRecord> match = samReader.queryContained(
						gene.getSeqID(),interval.getStartCoord(),interval.getEndCoord());
				
				System.out.println("Fetching reads from a region of  "+ interval.getLength() + " bases long");
				while(match.hasNext()){
					//For every short read check the start coord and its mate to make sure it is in the interval
					SAMRecord samRecord = match.next();
					if(samRecord.getMateUnmappedFlag()){
						//If mate is unmapped
						//skip
					}else{
						//NOTE: not all mates are reverse ????
						//what should I do //TOOD
//							if(samRecord.getMateNegativeStrandFlag()){
//								System.out.println("Reverese");
//							}
						if(	interval.contains(samRecord.getAlignmentStart()) &&
							interval.contains(samRecord.getMateAlignmentStart())){
							arrayOfLengths.add(Math.abs(samRecord.getAlignmentStart()-samRecord.getMateAlignmentStart()));
						}	
					}			
				}
				match.close();
			}
		}
		samReader.close();
	}
}