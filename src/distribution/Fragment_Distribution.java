package distribution;

import gffParser.GFF3Parser;
import gffParser.Gene;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.util.CloseableIterator;
import vito.Interval;
import vito.Statistics;

public class Fragment_Distribution {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File gffFile;//= new File("/Users/Vinny/Data/c2c12_cugbp1_kd/genes.gff");
//		File file= new File("/Users/Vinny/Data/c2c12_cugbp1_kd/cugbp1.gff")
		File bamFile;//= new File("/Users/Vinny/Data/c2c12_cugbp1_kd/control_0d/accepted_hits_fixed.bam");
		File bamFileIndex;//= new File("/Users/Vinny/Data/c2c12_cugbp1_kd/control_0d/accepted_hits_fixed.bam.bai");
		
		
		if(args.length==3){
			gffFile = new File(args[0]);
			bamFile = new File(args[1]);
			bamFileIndex = new File(args[2]);
		}else if(args.length==1){
			gffFile = new File(args[0]);
			createConstitutiveIntervalIndex(gffFile);
			return;
		}else{
			System.out.println("Not Enough Arguements");
			return;
		}
		
		ArrayList<Integer> arrayOfLengths = new ArrayList<Integer>();
		long start = System.currentTimeMillis();		
		
		;
		
		ArrayList<Interval> intervals = createConstitutiveIntervalIndex(gffFile);
		
		
		
		SAMFileReader samReader = new SAMFileReader(bamFile,bamFileIndex);
		samReader.setValidationStringency(SAMFileReader.ValidationStringency.SILENT);
 
		//For every interval get the shortreads
		System.out.println("Iterating through "+ intervals.size() + " intervals");
		
		for(Interval interval:intervals){
			if(interval.getLength()>1000){
//				System.out.println("Found an interval of " + interval.getLength());
				//For every interval greater than 1000 bp, get the short reads
				CloseableIterator<SAMRecord> match = samReader.queryContained(
						interval.getId(),interval.getStartCoord(),interval.getEndCoord());
				
//					System.out.println("Fetching reads from a region of  "+ interval.getLength() + " bases long");
				while(match.hasNext()){
					//For every short read check the start coord and its mate to make sure it is in the interval
					SAMRecord samRecord = match.next();
					if(samRecord.getMateUnmappedFlag()){
						//If mate is unmapped
						//skip
					}else{
						//NOTE: not all mates are reverse ????
						//what should I do //TOOD
//								if(samRecord.getMateNegativeStrandFlag()){
//									System.out.println("Reverese");
//								}
						if(	interval.contains(samRecord.getAlignmentStart()) &&
							interval.contains(samRecord.getMateAlignmentStart())){		
							
							arrayOfLengths.add(Math.abs(samRecord.getAlignmentStart()-samRecord.getMateAlignmentStart())
									+samRecord.getCigar().getReadLength());
						}	
					}			
				}
				match.close();
			}
		}

		
		System.out.println("Sorting");
		Collections.sort(arrayOfLengths);
		
		System.out.println("Writing Files: "+ arrayOfLengths.size());
		try {
			File writeFile = new File(bamFile.getName()+".length.dat");
			PrintWriter pWriter = new PrintWriter(new FileWriter(writeFile));
		
			for(Integer integer : arrayOfLengths){
				
			    pWriter.println(integer.intValue());
			    
			}
			pWriter.close();	
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Completed in " + (System.currentTimeMillis()-start) + " Milliseconds");
		return;
	}

	private static ArrayList<Interval> createConstitutiveIntervalIndex(File file) {
		ArrayList<Interval> listOfIntervals = new ArrayList<Interval>();
		
		System.out.println("Parsing GFF3");
		GFF3Parser parser = new GFF3Parser();
		parser.parse(file);
		HashMap<String,Gene> hashOfGenes= parser.getGenes();
		System.out.println("Parsing Finished");
		parser=null; //Will this clear some memory?
		
		System.out.println("Iterating through " +hashOfGenes.size()+" Genes");
		
		PrintWriter genomeConstWriter;
		try {
			File writeFile = new File(file.getName()+".const.dat");
			genomeConstWriter = new PrintWriter(new FileWriter(writeFile));
			    	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		//For every gene get the constitutite bases
		Iterator<Gene> geneItr = hashOfGenes.values().iterator();
		int count=0;
		//For Every Gene
		while(geneItr.hasNext()){
			count++;
			if(count%1000==0){
				System.out.println("Currently at : "+ count + "/" + hashOfGenes.size());
			}
			Gene gene = geneItr.next();
			
//			System.out.println("Fetching Constitutive Bases for " + gene.getId());
			
			//Get the constitutive bases
			ArrayList<Integer> cBases = Statistics.getConstituitiveBases(gene.getMRNA().values());
													
			//Then get the constitutive Intervals
//			System.out.println("Fetching Constitutive Intervals for " + gene.getId());
			ArrayList<Interval> intervals = Statistics.getConstitutiveIntervals(cBases);
			
			//Print out the intervals to txt
			for(Interval interval:intervals){
				if(interval.getLength()>1000){
					interval.setID(gene.getSeqID());
					listOfIntervals.add(interval);
					
					//Write to file
					String strand;
					if(gene.getStrand()){
						strand="+";
					}else{
						strand="-";
					}
					genomeConstWriter.println(	"chr"+gene.getSeqID()+":"
												+interval.getStartCoord()+"-"
												+interval.getEndCoord()+":"
												+strand+"\t"
												+interval.getLength());
				}
			}
		}
		System.out.println("Iteration Complete");
		hashOfGenes=null;
		return listOfIntervals;
	}
}

//For a Threading Implementation
//while(Thread.activeCount()!=0){
//System.out.println("Waiting for last few threads to finish:" + Thread.activeCount());
//try {
//	Thread.sleep(10000);
//} catch (InterruptedException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}
//}