package genomeBrowser;

import gffParser.Exon;
import gffParser.MRNA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
		METHOD0,METHOD1,METHOD2,METHOD3
	}
	
	/** The total number of reads in a BAM file. */
	private static int totalNumberOfReads;

	/** The overhang used for calculating method 2. (Default is 4)*/
	private static int overhang = 4;

	/** The method to use when calculating weights. */
	private static Method method= Method.METHOD0;
	
	/** The short read length.(Default is 35) */ 
	private static int shortReadLength = 35;
	
	/**
	 * Sets the method to use for calculating weights.
	 * 
	 * @param i the new method
	 */
	public static void setMethod(Method i){
		method =i;
	}
	/**
	 * Sets the total number of reads.
	 * 
	 * @param i the new total number of reads
	 */
	public static void setTotalNumberOfReads(int i){
		totalNumberOfReads=i;
	}
	
	/**
	 * Sets the short read length.
	 *
	 * @param i the new short read length
	 */
	public static void setShortReadLength(int i){
		shortReadLength = i;
	}
	/**
	 * Gets the weight of an exon.
	 * This weight is dependent on what method is used.
	 * 
	 * @param compatibleShortReads the ArrayList of compatible ShortReads
	 * @param exon the exon
	 * @param endExon the end exon
	 * 
	 * @return the weight
	 */
	public static float getWeightOfExon(Exon exon, ArrayList<ShortRead> compatibleShortReads,Boolean endExon){
		int absoluteStart = exon.getStart();
		int absoluteEnd = exon.getEnd();
		switch(method){
			//Average Coverage Per Exon	
			case METHOD0: return getAverage_ReadsPerBase(absoluteStart, absoluteEnd, compatibleShortReads);
			case METHOD1: return getBodyReads_Per_ExonLength(absoluteStart, absoluteEnd, compatibleShortReads);
			case METHOD2: return (getAllReads_Per_TotalPossiblePositions(absoluteStart, absoluteEnd, compatibleShortReads,endExon)/totalNumberOfReads)*100000000;
			default: return getAverage_ReadsPerBase(absoluteStart, absoluteEnd, compatibleShortReads); 
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
	private static float getAllReads_Per_TotalPossiblePositions(
			int absoluteStart, int absoluteEnd,
			ArrayList<ShortRead> compatibleShortReads,boolean endExon) {
		int count=0;
		
		for(ShortRead shortRead:compatibleShortReads){
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
			possibleStartPositions=length-2*(overhang-1) + ((shortReadLength-1)-2*(overhang-1));
		}
		return (float)count/possibleStartPositions;
	}
	
	/**
	 * Gets the ratio of the sum of reads per base to the total number of bases.
	 * 
	 * @param absoluteStart the absolute start of the exon
	 * @param absoluteEnd the absolute end of the exon
	 * @param compatibleShortReads the ArrayList of compatible ShortReads
	 * 
	 * @return the sum of reads per base divided by total number of bases
	 */
	public static float getAverage_ReadsPerBase(int absoluteStart, int absoluteEnd, ArrayList<ShortRead> compatibleShortReads){
		ArrayList<SAMRecord> compatibleSAMRecords = convertShortReadsToSamRecords(compatibleShortReads);
		HashMap<Integer,Integer> compatibleDensityMap = getDensityMap(absoluteStart, absoluteEnd, compatibleSAMRecords);
		int sum =0;
		for(int i =absoluteStart;i<=absoluteEnd;i++){
			sum+=(compatibleDensityMap.get(i));
		}
		int length = (absoluteEnd-absoluteStart+1);
		return (float)sum/length;
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
	public static float getBodyReads_Per_ExonLength(int absoluteStart, int absoluteEnd, ArrayList<ShortRead> compatibleShortReads){
		int length = (absoluteEnd-absoluteStart+1);
		int count=0;
		for(ShortRead shortRead:compatibleShortReads){
			if(		shortRead.getStart()>=absoluteStart &&
					shortRead.getEnd()<=absoluteEnd &&
					shortRead.isBodyRead()){
				count++;
			}
			
		}
		return (float)count/(length);
	}
	public static float getStandardDeviation(int absoluteStart, int absoluteEnd, ArrayList<ShortRead> compatibleShortReads){
		switch(method){
			case METHOD0:return getStandardDeviation_ReadsPerBase(absoluteStart, absoluteEnd,compatibleShortReads);
			case METHOD1:return 0;
			case METHOD2:return 0;
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
	public static float getStandardDeviation_ReadsPerBase(int absoluteStart, int absoluteEnd, ArrayList<ShortRead> compatibleShortReads) {
		ArrayList<SAMRecord> compatibleSAMRecords = convertShortReadsToSamRecords(compatibleShortReads);
		HashMap<Integer,Integer> compatibleDensityMap = getDensityMap(absoluteStart, absoluteEnd, compatibleSAMRecords);
		float mean = getAverage_ReadsPerBase(absoluteStart, absoluteEnd, compatibleShortReads);	
		double variance=0;
		for(int i = absoluteStart;i<=absoluteEnd;i++){
			int value = compatibleDensityMap.get(i);
			variance+= Math.pow((value-mean), 2);
		}
		return (float) Math.sqrt(variance/(absoluteEnd-absoluteStart));//Don't need the minus one because this is inclusive
	}
	
	/**
	 * Gets a ArrayList of compatible ShortReads.
	 * 
	 * @param isoform the isoform you want to find compatible reads for
	 * @param iSAMRecords the arrayList of SAMRecords you want to check (Non-Compatible)
	 * 
	 * @return An ArrayList of compatible ShortReads
	 */
	public static ArrayList<ShortRead> getCompatibleShortReads(MRNA isoform, ArrayList<SAMRecord> iSAMRecords){ 
		ArrayList<ShortRead> arrayOfCompatibleShortReads = new ArrayList<ShortRead>();
		
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
			ShortRead temp =getShortReadIfCompatible(samRecord,samRecordIntervals, isoform.getExons().values());  
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
		for(ShortRead shortRead:getCompatibleShortReads(isoform, iSamRecords)){
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
	public static ArrayList<SAMRecord> convertShortReadsToSamRecords(ArrayList<ShortRead> iShortReads){
		ArrayList<SAMRecord> samRecords = new ArrayList<SAMRecord>();
		for(ShortRead shortRead:iShortReads){
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
	private static ShortRead getShortReadIfCompatible(SAMRecord samRecord,ArrayList<Interval> samInterval,Collection<Exon> exonList){
		
		if(samInterval.size()==0){
			System.err.println("samInterval Was Empty");
			return null;
		}
		ShortRead newShortRead = new ShortRead(samRecord,samInterval);
		
		for(int i =0;i<samInterval.size();i++){
			if(i==0){
				boolean found = false;
				for(Exon exon: exonList){
					//Get the first exon whose interval whose start is less than the short read start
					//but whose end is greater than or equal to the short read start
					if(exon.getStart()<=samInterval.get(0).getStartCoord() && exon.getEnd()>=samInterval.get(0).getStartCoord()){
						//check if case of single interval
						if(samInterval.size()==1){
							if(exon.getStart()<=samInterval.get(0).getStartCoord() && exon.getEnd()>=samInterval.get(0).getEndCoord()){
//								System.out.println(" Body Read ");
								newShortRead.addExons(exon);
								return newShortRead;
							}else{
//								System.out.println("Not Found In any Exon");
								return null;
							}
						}
						//If not the case of a single interval, check if the short read interval at least
						//ends with the exon end
						if(exon.getEnd()==samInterval.get(0).getEndCoord()){
//							System.out.print(" 0-I&E ");
							newShortRead.addExons(exon);
							found = true;
							i++;
							break;
						}else{
//							System.out.println(" 0-I*E ");
							return null;
						}
					}
				}
				if(found==false){
//					System.out.println("No Exons found that contain the short read start");
					return null;	
				}
				
			}
			if(i!=samInterval.size()-1){// check if the interval is completely within an exon
				boolean found = false;
				for(Exon exon: exonList){
					if(exon.getStart()==samInterval.get(i).getStartCoord() && exon.getEnd()==samInterval.get(i).getEndCoord()){
//						System.out.print(" " + i +"-I&E");
						newShortRead.addExons(exon);
						i++;
						found=true;
						break;
					}
				}
				if(!found){
//					System.out.println(" " + i +"-I*E");
					return null;
				}
			}
			if(i==samInterval.size()-1){ //Check if the last interval is within the last exon
				for(Exon exon: exonList){
					if(exon.getStart()==samInterval.get(i).getStartCoord() && exon.getEnd()>=samInterval.get(i).getEndCoord()){
//						System.out.println(" Junction Read ");
						newShortRead.addExons(exon);
						return newShortRead;	
					}
				}
//				System.out.println(" " + i +"-No End Match");
				return null;
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
		ArrayList<ShortRead> compatibleShortReads= getCompatibleShortReads(isoform, iSAMRecords);
		int count=0;
		for(ShortRead shortRead:compatibleShortReads){
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
		ArrayList<ShortRead> compatibleShortReads= getCompatibleShortReads(isoform, iSAMRecords);
		int count=0;
		for(ShortRead shortRead:compatibleShortReads){
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

	public static float getWeightOfJunction(Junction junction) {
		switch(method){
			case METHOD0: return junction.getHits();
			case METHOD1: return 0;
			case METHOD2: return (float)junction.getHits()/((shortReadLength-1)-2*(overhang-1))/totalNumberOfReads*100000000; 
			default: return 10;
			//FIXME getting junction weights
		}	
	}
	public static float getRPKM(ArrayList<SAMRecord> allSAMRecord, ArrayList<Interval> constitutiveIntervals,int totalNumberOfReads){
		Statistics.setTotalNumberOfReads(totalNumberOfReads);
		ArrayList<Interval> listOfIntervals = new ArrayList<Interval>();
		
		for(Interval interval:constitutiveIntervals){
			if(interval.getLength()>shortReadLength){
				listOfIntervals.add(interval);
			}
		}
		//System.out.println(listOfIntervals.size());
		float count=0;
		float denominator=0;
		for(Interval interval:listOfIntervals){
			denominator+=(interval.getLength()-shortReadLength+1);
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
		//System.out.println((count/denominator)/((float)totalNumberOfReads/1000000));
		return (count/denominator)/((float)totalNumberOfReads/1000000);
	}

}
