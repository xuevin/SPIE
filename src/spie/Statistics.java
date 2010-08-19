package spie;

import gffParser.Exon;
import gffParser.MRNA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import drawableObjects.Junction;

import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
import net.sf.samtools.SAMRecord;

/**
 * The Class Statistics.
 * 
 * This class performs a host of statistical analysis.
 * @author Vincent Xue
 */
public class Statistics {
	
	public enum Method{
		COVERAGEPEREXON,RPK,RPKM,METHOD3
	}
	/** The total number of reads in a BAM file. */
	private static int totalNumberOfReads;

	/** The overhang used for calculating. (Default is 4)*/
	private static int overhang = 4;

	/** The default method to use when calculating weights. */
	private static Method method= Method.COVERAGEPEREXON;
	
	/** The short read length.(Default is 35) */ 
	private static int readLength = 35;
	
	protected Statistics(){
		//This is not ment to be instantiated
	}
	/**
	 * Sets the method to use for calculating weights.
	 * 
	 * @param i the new method
	 */
	public static void setMethod(Method i){
		System.out.println("Request Made to Change Method");
		method =i;
	}
	/**
	 * Sets the total number of reads.
	 * 
	 * @param i the new total number of reads
	 */
	public static void setTotalNumberOfReads(int i){
		System.out.println("Total Number Of Reads Updated: " + i);
		totalNumberOfReads=i;
	}
	/**
	 * Sets the short read length.
	 *
	 * @param i the new short read length
	 */
	public static void setReadLength(int i){
		readLength = i;
	}
	/**
	 * Gets the weight of an exon.
	 * This weight is dependent on what method is used.
	 * 
	 * @param compatibleMRNAReads the ArrayList of compatible reads for the MRNA
	 * @param exon the exon
	 * @param isEndExon the end exon
	 * 
	 * @return the weight
	 */
	public static double getWeightOfExon(Exon exon, ArrayList<Read> compatibleMRNAReads,Boolean isEndExon){
		int absoluteStart = exon.getStart();
		int absoluteEnd = exon.getEnd();
		switch(method){
			//Average Coverage Per Exon	
			case COVERAGEPEREXON: return getAverage_ReadsPerBase_PerExon(absoluteStart, absoluteEnd, compatibleMRNAReads);
			case RPK: return getBodyReads_per_KExon(absoluteStart, absoluteEnd, compatibleMRNAReads);
			case RPKM: return ((double)getAllReads_Per_KTotalPossiblePositions(absoluteStart, absoluteEnd, compatibleMRNAReads,isEndExon)/((double)totalNumberOfReads/1000000));
			default: return getAverage_ReadsPerBase_PerExon(absoluteStart, absoluteEnd, compatibleMRNAReads); 
		}
	}
	
	/**
	 * Gets the ratio of all reads (junction and body) to the total number of possible start
	 * positions for a short read.
	 * 
	 * The total number of positions can be calculated using the equation
	 * [L-2(v-1)]+[(r-1)-2(v-1)] (for inner exons)
	 * [L-2(v-1)] (for end exons)
	 * 
	 * where
	 * L is the length of the exon
	 * r is the length of the short read
	 * v is the length of the overlap
	 * 
	 * @param absoluteStart the absolute start of the exon
	 * @param absoluteEnd the absolute end of the exon
	 * @param compatibleShortReads the ArrayList of compatible ShortReads
	 * @param endExon the boolean that specifies whether the current exon is an end exon
	 * 
	 * @return the ratio of all reads : total number of start positions
	 */
	private static double getAllReads_Per_KTotalPossiblePositions(
			int absoluteStart, int absoluteEnd,
			ArrayList<Read> compatibleShortReads,boolean endExon) {
		int count=0;
		
		for(Read shortRead:compatibleShortReads){
			for(Exon exon :shortRead.getSetOfExons()){
				if(exon.getStart()==absoluteStart && exon.getEnd()==absoluteEnd){
					count++;
				}
			}
		}
		int length = (absoluteEnd-absoluteStart+1);
		int possibleStartPositions=0;
		if(endExon)
		{
			possibleStartPositions=length-2*(overhang-1);
			
		}else{
			possibleStartPositions=length-2*(overhang-1) + ((readLength-1)-2*(overhang-1));
		}
		return (double)count/((double)possibleStartPositions/1000);
	}
	
	/**
	 * Gets the ratio of the sum of reads per base to the total number of bases.
	 * 
	 * @param absoluteStart the absolute start of the exon
	 * @param absoluteEnd the absolute end of the exon
	 * @param compatibleShortReads the ArrayList of compatible ShortReads
	 * 
	 * @return the sum of reads body per base divided by total number of bases
	 */
	public static double getAverage_ReadsPerBase_PerExon(int absoluteStart, int absoluteEnd, ArrayList<Read> compatibleShortReads){
		//Sort out the body reads
		ArrayList<Read> bodyReads = new ArrayList<Read>();
		for(Read read :compatibleShortReads){
			if(read.isBodyRead()){
				bodyReads.add(read);	
			}
		}
		
		ArrayList<SAMRecord> compatibleSAMRecords = convertShortReadsToSamRecords(bodyReads);
		HashMap<Integer,Integer> compatibleDensityMap = getDensityMap(absoluteStart, absoluteEnd, compatibleSAMRecords);
		int sum =0;
		for(int i =absoluteStart;i<=absoluteEnd;i++){
			sum+=(compatibleDensityMap.get(i));
		}
		int length = (absoluteEnd-absoluteStart+1);
		return (double)sum/length;
	}
	
	/**
	 * Gets the number of body short reads divided by the exon length.
	 * 
	 * @param absoluteStart the absolute start of an exon
	 * @param absoluteEnd the absolute end of an exon
	 * @param compatibleShortReads the compatible short reads
	 * 
	 * @return the number of reads for the exon divided by the length of the exon
	 */
	public static double getBodyReads_per_KExon(int absoluteStart, int absoluteEnd, ArrayList<Read> compatibleShortReads){
		int length = (absoluteEnd-absoluteStart+1);
		int count=0;
		for(Read shortRead:compatibleShortReads){
			if(		shortRead.getStart()>=absoluteStart &&
					shortRead.getEnd()<=absoluteEnd &&
					shortRead.isBodyRead()){
				count++;
			}
			
		}
		return (double)count/((double)length/1000);
	}
	public static double getStandardDeviation(int absoluteStart, int absoluteEnd, ArrayList<Read> compatibleShortReads){
		switch(method){
			case COVERAGEPEREXON:return getStandardDeviation_ReadsPerBase(absoluteStart, absoluteEnd,compatibleShortReads);
			case RPK:return 0;
			case RPKM:return 0;
			case METHOD3:return 0;
			default:return 0;
		}
		
	}
	
	/**
	 * Gets the standard deviation_ reads per base.
	 * 
	 * @param absoluteStart the absolute start
	 * @param absoluteEnd the absolute end
	 * @param compatibleShortReads the compatible short reads
	 * 
	 * @return the standard deviation_ reads per base
	 */
	public static double getStandardDeviation_ReadsPerBase(int absoluteStart, int absoluteEnd, ArrayList<Read> compatibleShortReads) {
		ArrayList<SAMRecord> compatibleSAMRecords = convertShortReadsToSamRecords(compatibleShortReads);
		HashMap<Integer,Integer> compatibleDensityMap = getDensityMap(absoluteStart, absoluteEnd, compatibleSAMRecords);
		double mean = getAverage_ReadsPerBase_PerExon(absoluteStart, absoluteEnd, compatibleShortReads);	
		double variance=0;
		for(int i = absoluteStart;i<=absoluteEnd;i++){
			int value = compatibleDensityMap.get(i);
			variance+= Math.pow((value-mean), 2);
		}
		return (double) Math.sqrt(variance/(absoluteEnd-absoluteStart));//Don't need the minus one because this is inclusive
	}
	
	/**
	 * Gets a ArrayList of compatible ShortReads.
	 * 
	 * @param isoform the isoform you want to find compatible reads for
	 * @param iSAMRecords the arrayList of SAMRecords you want to check (Non-Compatible)
	 * 
	 * @return An ArrayList of compatible ShortReads
	 */
	public static ArrayList<Read> getCompatibleReads(MRNA isoform, ArrayList<SAMRecord> iSAMRecords){ 
		ArrayList<Read> arrayOfCompatibleShortReads = new ArrayList<Read>();
		
		for(SAMRecord samRecord:iSAMRecords){
			//** CIGAR PARSING
			List<CigarElement> cigarElements = samRecord.getCigar().getCigarElements();
			int start = samRecord.getAlignmentStart();
			ArrayList<Interval> samRecordIntervals= new ArrayList<Interval>();
			
			for(CigarElement cigarElement:cigarElements){
				if(cigarElement.getOperator().equals(CigarOperator.M)){
					samRecordIntervals.add(new Interval(start,cigarElement.getLength()));
					start=start+cigarElement.getLength();
					//System.out.println("M"+cigarElement.getLength());
				}else if(cigarElement.getOperator().equals(CigarOperator.N)){
					start=start+cigarElement.getLength();
					//System.out.println("N"+cigarElement.getLength());
				}else if(cigarElement.getOperator().equals(CigarOperator.D)){
					start=start+cigarElement.getLength();
					System.out.println("D"+cigarElement.getLength());
				}	
			}
//			System.out.print(count++ +" "+ samRecord.getCigarString()+" ");
			Read temp =getShortReadIfCompatible(samRecord,samRecordIntervals, isoform.getExons().values());  
			if(temp!=null){
				arrayOfCompatibleShortReads.add(temp);
			}else{
				//skip
			}
		}		
		//System.out.println(arrayOfCompatibleShortReads.size());
		return arrayOfCompatibleShortReads;
	}
	
	/**
	 * Gets a compatible ArrayList of SAMRecords
	 * (it calls getCompatibleShortReadsFirst).
	 * 
	 * @param isoform the input isoform
	 * @param iSamRecords the input array of SAMRecords (NonCompatible ShortReads)
	 * 
	 * @return the ArrayList of compatible SAMRecords
	 */
	public static ArrayList<SAMRecord> getCompatibleSamRecords(MRNA isoform, ArrayList<SAMRecord> iSamRecords){
		ArrayList<SAMRecord> compatibleSAMRecords = new ArrayList<SAMRecord>();
		for(Read shortRead:getCompatibleReads(isoform, iSamRecords)){
			compatibleSAMRecords.add(shortRead.getSAMRecord());	
		}
		return compatibleSAMRecords;
	}
	
	/**
	 * Convert short reads to sam records. Does not check for compatibility
	 * 
	 * @param iShortReads the input array of ShortReads
	 * 
	 * @return the ArrayList of SamRecords
	 */
	public static ArrayList<SAMRecord> convertShortReadsToSamRecords(ArrayList<Read> iShortReads){
		ArrayList<SAMRecord> samRecords = new ArrayList<SAMRecord>();
		for(Read shortRead:iShortReads){
			samRecords.add(shortRead.getSAMRecord());	
		}
		return samRecords;
	}
	
	/**
	 * Gets a new ShortRead if the SAMRecord is compatible
	 * 
	 * 1) First find the exon where the short read begins
	 * 2) Catch the case of a single interval --See if the short read is completely within the exon
	 * 3) If more than one interval, check each interval with the next ones. Make sure short read
	 * interval has 'start of exon'  and end of short read has 'end of exon' (except the last)
	 * 4) Check that the last interval matches
	 * 
	 * @param samRecord the SAMRecord that you want to find out is compatible or not
	 * @param samInterval the ArrayList of Intervals that contain the genomic coordinates
	 * for which a short read spans
	 * @param exonList the Collection of exons that the SAMRecord might be found in or cross
	 * 
	 * @return a new ShortRead is returned if it is compatible. If it is not, null is returned.
	 */
	private static Read getShortReadIfCompatible(SAMRecord samRecord,ArrayList<Interval> samInterval,Collection<Exon> exonList){
		
		if(samInterval.size()==0){
			System.err.println("samInterval Was Empty");
			return null;
		}
		Read newShortRead = new Read(samRecord,samInterval);
		
		Iterator<Exon> itr = exonList.iterator();
		
		
		for(int i =0;i<samInterval.size();i++){
			if(i==0){
				boolean found = false;
				while(itr.hasNext()){
					Exon exon = itr.next();
					//Get the first exon whose interval whose start is less than the short read start
					//but whose end is greater than or equal to the short read start
					if(exon.getStart()<=samInterval.get(0).getStartCoord() && exon.getEnd()>=samInterval.get(0).getStartCoord()){
						//check if case of single interval
						if(samInterval.size()==1){
							if(exon.getStart()<=samInterval.get(0).getStartCoord() && exon.getEnd()>=samInterval.get(0).getEndCoord()){
								//System.out.println(" Body Read ");
								newShortRead.addExons(exon);
								return newShortRead;
							}else{
								//System.out.println("Not Found In any Exon");
								return null;
							}
						}
						//If not the case of a single interval, check if the short read interval at least
						//ends with the exon end
						if(exon.getEnd()==samInterval.get(0).getEndCoord()){
							//System.out.print(" 0-I&E ");
							newShortRead.addExons(exon);
							found = true;
							i++;
							break;
						}else{
							//System.out.println(" 0-I*E ");
							return null;
						}
					}
				}
				if(found==false){
					//System.out.println("No Exons found that contain the short read start");
					return null;	
				}
				
			}
			if(i!=samInterval.size()-1){// check if the interval is completely within an exon
				Exon exon = itr.next();
				if(exon.getStart()==samInterval.get(i).getStartCoord() && exon.getEnd()==samInterval.get(i).getEndCoord()){
						//System.out.print(" " + i +"-I&E");
					newShortRead.addExons(exon);
					i++;
				}else{
					//System.out.println(" " + i +"-I*E");
					return null;
				}
			}
			if(i==samInterval.size()-1){ //Check if the last interval is within the last exon
				Exon exon = itr.next();
				if(exon.getStart()==samInterval.get(i).getStartCoord() && exon.getEnd()>=samInterval.get(i).getEndCoord()){
					//System.out.println(" Junction Read ");
					newShortRead.addExons(exon);
					return newShortRead;	
				}else{
					//System.out.println(" " + i +"-No End Match");
					return null;
				}
			}
		}
		System.err.println(" This is wrong ");
		return null;
	}
	
	/**
	 * Gets the isoform density map.A density map is a hash map that has all the genomic coordinates as
	 * keys and the number of short reads that cross it as values
	 * 
	 * @param absoluteStart the start of the gene
	 * @param absoluteEnd the end of the gene
	 * @param isoform the isoform for which you want to get a density map of
	 * @param iSamRecords the ArrayList of SAMRecords from which a density map will be generated from
	 * 
	 * @return The HashMap of <Integer,Integer> where the key is the genomic coordinate and the value
	 * is the number of short reads that cross it. This hash map is generate only using compatible reads
	 */
	public static HashMap<Integer,Integer> getIsoformDensityMap(int absoluteStart, int absoluteEnd,MRNA isoform, ArrayList<SAMRecord> iSamRecords){
		//Get the compatible SAMRecords
		//Then pass it to the densitymap function
		return getDensityMap(absoluteStart,absoluteEnd, getCompatibleSamRecords(isoform, iSamRecords));	
	}
	
	/**
	 * Gets the density map. A density map is a hash map that has all the specified coordinates as
	 * keys and the count of short reads that cross it as values
	 * 
	 * @param absoluteStart the start of the gene
	 * @param absoluteEnd the end of the gene
	 * @param iSAMRecords the ArrayList of SAMRecords from which a density map will be generated from
	 * 
	 * @return The HashMap of <Integer,Integer> where the key is the genomic coordinate and the value
	 * is the number of short reads that cross it
	 */
	public static HashMap<Integer, Integer> getDensityMap(int absoluteStart, int absoluteEnd,ArrayList<SAMRecord> iSAMRecords){
		//First load an absolute density plot hash
		HashMap<Integer, Integer> densityMap = new HashMap<Integer, Integer>();
		
		for(int i = absoluteStart;i<=absoluteEnd;i++){
			densityMap.put(i,0);	
		}
		
		for(SAMRecord samRecord:iSAMRecords){
			//Only retrieve the density map for the specified coordinates
				
			if( //Body Reads
				(samRecord.getAlignmentStart()>=absoluteStart && samRecord.getAlignmentEnd()<=absoluteEnd)||
				//Tail of short read is in region
				(samRecord.getAlignmentStart()<absoluteStart&&samRecord.getAlignmentEnd()>absoluteStart)||
				//Head of short read is in region
				(samRecord.getAlignmentStart()<absoluteEnd && samRecord.getAlignmentEnd()>absoluteEnd)){
			
				//** CIGAR PARSING
				List<CigarElement> cigarElements = samRecord.getCigar().getCigarElements();
				int start = samRecord.getAlignmentStart();
				for(CigarElement cigarElement:cigarElements){
					if(cigarElement.getOperator().equals(CigarOperator.M)){
						for(int i = 0;i<cigarElement.getLength();i++){
							//Ensure a density map of only the specified region is put in
							if(start>absoluteStart && start<absoluteEnd){
								int prev = densityMap.get(start);
								prev++;
								densityMap.put(start, prev);
								start++;	
							}
						}
						//System.out.println("M"+cigarElement.getLength());
					}else if(cigarElement.getOperator().equals(CigarOperator.N)){
						for(int i = 0;i<cigarElement.getLength();i++){
							start++;
						}
						//System.out.println("N"+cigarElement.getLength());
					}else if(cigarElement.getOperator().equals(CigarOperator.D)){
						for(int i = 0;i<cigarElement.getLength();i++){
							start++;
						}
						System.out.println("D"+cigarElement.getLength());
					}
						
				}
			}else{
				
			}		
		}
		return densityMap;
	}
	
	/**
	 * Gets the number of body reads as specified by the coordinates.
	 * 
	 * @param isoform the isoform from which you want the compatible reads
	 * @param absoluteStart the absolute start of the exon
	 * @param absoluteEnd the absolute end of the exon
	 * @param iSAMRecords the Array of SAMRecords (Non-Compatible)
	 * 
	 * @return the number of body reads in the specified region
	 */
	public static int getNumberOfBodyReads(MRNA isoform,int absoluteStart, int absoluteEnd,
			ArrayList<SAMRecord> iSAMRecords){
		ArrayList<Read> compatibleShortReads= getCompatibleReads(isoform, iSAMRecords);
		int count=0;
		for(Read shortRead:compatibleShortReads){
			if(shortRead.isBodyRead()){
				if(shortRead.getSetOfExons().size()!=1){
					System.err.println("FATAL ERROR IN CALCULATIONS");
				}
				for(Exon exon :shortRead.getSetOfExons()){	
					if(exon.getStart()==absoluteStart && exon.getEnd()==absoluteEnd){
						count++;
					}
				}
			}
		}
		return count;
	}
	
	/**
	 * Gets the number of junction reads as specified by the coordinates.
	 * 
	 * @param isoform the isoform from which you want the compatible reads
	 * @param absoluteStart the absolute start of the exon
	 * @param absoluteEnd the absolute end of the exon
	 * @param iSAMRecords the Array of SAMRecords (Non-Compatible)
	 * 
	 * @return the number of junction reads in the specified region
	 */
	public static int getNumberOfJunctionReads(MRNA isoform,int absoluteStart, int absoluteEnd,
			ArrayList<SAMRecord> iSAMRecords){
		ArrayList<Read> compatibleShortReads= getCompatibleReads(isoform, iSAMRecords);
		int count=0;
		for(Read shortRead:compatibleShortReads){
			if(shortRead.isJunctionRead()){
				for(Exon exon :shortRead.getSetOfExons()){
					if(exon.getStart()==absoluteStart && exon.getEnd()==absoluteEnd){
						count++;
					}
				}
					
			}
		}
		return count;
		
	}

	/**
	 * Sets the overhang.
	 * 
	 * @param iOverhang the new overhang
	 */
	public static void setOverhang(int iOverhang) {
		overhang = iOverhang;
		
	}
	
	/**
	 * Gets the weight of junction.
	 *
	 * @param junction the junction
	 * @return the weight of junction
	 */
	public static double getWeightOfJunction(Junction junction) {
		switch(method){
			case COVERAGEPEREXON: return junction.getHits();
			case RPK: return 0;
			case RPKM: return (double)junction.getHits()/((double)((readLength-1)-2*(overhang-1))/1000)/(totalNumberOfReads/1000000); 
			default: return 0;
		}	
	}
	
	/**
	 * Gets the rPKM.
	 *
	 * @param allSAMRecord the all sam record
	 * @param constitutiveIntervals the constitutive intervals
	 * @param totalNumberOfReads the total number of reads
	 * @return the rPKM
	 */
	public static double getRPKM(ArrayList<SAMRecord> allSAMRecord, ArrayList<Interval> constitutiveIntervals,int totalNumberOfReads){
		Statistics.setTotalNumberOfReads(totalNumberOfReads);
		Statistics.setReadLength(allSAMRecord.get(0).getCigar().getReadLength());
		
		ArrayList<Interval> listOfIntervals = new ArrayList<Interval>();
		
		for(Interval interval:constitutiveIntervals){
			if(interval.getLength()>readLength){
				listOfIntervals.add(interval);
			}
		}
		//System.out.println(listOfIntervals.size());
		double count=0;
		double denominator=0;
		for(Interval interval:listOfIntervals){
			denominator+=(interval.getLength()-readLength+1);
			for(SAMRecord samRecord:allSAMRecord){
				//Ignore all junction reads (body reads are the ones with cigar lengths of 35
				if(samRecord.getCigar().getCigarElements().size()==1){
					if(	interval.getStartCoord()<=samRecord.getAlignmentStart() &&
						interval.getEndCoord()>=samRecord.getAlignmentEnd()){
						count++;
					}
				}
			}
		}
		//Divide by a thousand
		denominator=denominator/1000;
		//System.out.println((count/denominator)/((double)totalNumberOfReads/1000000));
		return (count/denominator)/((double)totalNumberOfReads/1000000);
	}
	
	/**
	 * Gets the constitutive intervals.
	 *
	 * @param inputArrayOfConstitutivePositions the input array of constitutive positions
	 * @return the constitutive intervals
	 */
	public static ArrayList<Interval> getConstitutiveIntervals(ArrayList<Integer> inputArrayOfConstitutivePositions){
		
		ArrayList<Interval> listOfIntervals = new ArrayList<Interval>();
		int start = -1;
		int currentLength=-1;
		//Sort First
		
		//Iterate through all the constitutive unscaled positions and make intervals out of them
		//if they are consecutive
		for(Integer coord:inputArrayOfConstitutivePositions){
			if(start== -1){
				start = coord;
				currentLength=1;
			}else{
				//Make sure it is consecutive
				if(coord!=start+currentLength){
					listOfIntervals.add(new Interval(start, currentLength));
					start = coord;
					currentLength=1;
				}else{
					currentLength++;
					
				}
			}
		}
		listOfIntervals.add(new Interval(start, currentLength));
		return listOfIntervals;
		
	}
	/**
	 * Returns an ArrayList of genomic coordinates which are constitutive.
	 * 
	 * This method works by plotting all the coordinates for each MRNA. It then iteratively
	 * goes through each coordinate and determines if the number of "hits" for the coordinate
	 * equal the number of MRNAs in the list
	 * 
	 * @param mrnaList is a Collection of MRNA.
	 * 
	 * @return An ArrayList of constitutive genomic coordinates are returned.
	 */
	public static ArrayList<Integer> getConstituitiveBases(Collection<MRNA> mrnaList){
		
		
		
		HashMap<Integer,Integer> basesWithHits = new HashMap<Integer,Integer>();
//		for(int i = absoluteStartOfGene;i<=(absoluteStartOfGene+absoluteLengthOfGene-1);i++){
//			basesWithHits.put(i,0);
//		}
		//For all MRNA
		for(MRNA mrna:mrnaList){
			//For All Exons
			for(Exon exon:mrna.getExons().values()){
				//For all positions in Exon
				for(int y=exon.getStart();y<=exon.getEnd();y++){
					//Take a tally of how many exons exist for a certain base
					if(basesWithHits.get(y)==null){
						basesWithHits.put(y, 1);
					}else{
						int prev = basesWithHits.get(y);
						prev++;
						basesWithHits.put(y,prev);	
					}
				}
			}
		}
		ArrayList<Integer> newConstitutiveUnscaledPositions = new ArrayList<Integer>();
		if(mrnaList.size()>1){
			//maintains order
//			for(int i =absoluteStartOfGene;i<=absoluteEndOfGene;i++){
//				if(basesWithHits.get(i)==mrnaList.size()){
//					newConstitutiveUnscaledPositions.add(i);	
//				}
//				
//			}
			for(Integer genomicPosition:basesWithHits.keySet()){
				//Those sites with a tally equal to the size of the MRNA list are constitutive
				if(basesWithHits.get(genomicPosition)==mrnaList.size()){
					newConstitutiveUnscaledPositions.add(genomicPosition);
				}
			}
			Collections.sort(newConstitutiveUnscaledPositions);
		}
		return newConstitutiveUnscaledPositions;
	}

}
