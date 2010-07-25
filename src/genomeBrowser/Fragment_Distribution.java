package genomeBrowser;

import gffParser.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.util.CloseableIterator;

public class Fragment_Distribution {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<Integer> arrayOfLengths = new ArrayList<Integer>();
		
		long start = System.currentTimeMillis();
		File file= new File("/Users/Vinny/Data/c2c12_cugbp1_kd/genes.gff");
//		File file= new File("/Users/Vinny/Data/c2c12_cugbp1_kd/cugbp1.gff");
		
		System.out.println("Parsing GFF3");
		GFF3Parser parser = new GFF3Parser();
		parser.parse(file);
		HashMap<String,Gene> hashOfGenes= parser.getGenes();
		System.out.println("Parsing Finished");
		parser=null; //Will this clear some memory? 
		
		File bamFile= new File("/Users/Vinny/Data/c2c12_cugbp1_kd/control_0d/accepted_hits_fixed.bam");
		File bamFileIndex= new File("/Users/Vinny/Data/c2c12_cugbp1_kd/control_0d/accepted_hits_fixed.bam.bai");
		
		SAMFileReader samReader = new SAMFileReader(bamFile,bamFileIndex);
		samReader.setValidationStringency(SAMFileReader.ValidationStringency.SILENT);
		
		System.out.println("Iterating through " +hashOfGenes.size()+" Genes");
		
		//For every gene get the constitutite bases
		
		Iterator<Gene> geneItr = hashOfGenes.values().iterator();
		int count=0;
		while(geneItr.hasNext()){
			count++;
			System.out.println("Currently at : "+ count + "/" + hashOfGenes.size());
			Gene gene = geneItr.next();
			
//			System.out.println("Fetching Constitutive Bases for " + gene.getId());
			//Get the bases
			ArrayList<Integer> cBases = Statistics.getConstituitiveBases(gene.getMRNA().values());
													
			//Then get the Intervals
//			System.out.println("Fetching Constitutive Intervals for " + gene.getId());
			ArrayList<Interval> intervals = Statistics.getConstitutiveIntervals(cBases);
			
			//For every interval get the shortreads
//			System.out.println("Iterating through "+ intervals.size() + " intervals");
			for(Interval interval:intervals){
				if(interval.getLength()>1000){
//					System.out.println("Found an interval of " + interval.getLength());
					//For every interval greater than 1000 bp, get the short reads
					CloseableIterator<SAMRecord> match = samReader.queryContained(
							gene.getSeqID(),interval.getStartCoord(),interval.getEndCoord());
					
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
								arrayOfLengths.add(Math.abs(samRecord.getAlignmentStart()-samRecord.getMateAlignmentStart()));
							}	
						}			
					}
					match.close();
				}
			}

		}
		
		System.out.println("Iteration Complete");
		System.out.println("Writing Files: "+ arrayOfLengths.size());
		FileOutputStream fos;
		try {
			fos = new FileOutputStream("AL_Integer.VITO");
			BufferedOutputStream bos = new BufferedOutputStream(fos);
		    ObjectOutputStream oos = new ObjectOutputStream(bos);
		    oos.writeObject(arrayOfLengths);
		    oos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done");
		System.out.println(System.currentTimeMillis()-start);
		return;
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