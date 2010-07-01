package genomeBrowser;

import gffParser.Exon;
import gffParser.MRNA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
import net.sf.samtools.SAMRecord;

public class Statistics {
	static int method;
	static int totalNumberOfReads;
	
	public static void setMethod(int i){
		method =i;
	}
	public static void setTotalNumberOfReads(int i){
		totalNumberOfReads=i;
	}
	
	/**
	 * Gets the weight of an exon.
	 * 
	 * @param compatibleShortReads the ArrayList of compatible ShortReads
	 * @param exon the exon
	 * 
	 * @return the weight
	 */
	public static float getWeight(Exon exon, ArrayList<ShortRead> compatibleShortReads,Boolean endExon){
		int absoluteStart = exon.getStart();
		int absoluteEnd = exon.getEnd();
		if(method==0){
			return getAverage_ReadsPerBase(absoluteStart, absoluteEnd, compatibleShortReads);	
		}else if (method==1){
			return getBodyReads_Per_ExonLength(absoluteStart, absoluteEnd, compatibleShortReads);
		}else if(method==2){
			return getAllReads_Per_TotalPossiblePositions(absoluteStart, absoluteEnd, compatibleShortReads,endExon); 
		}else if(method==3){
			float value = getAverage_ReadsPerExonLength_OverTotalNumberOfReads(absoluteStart, absoluteEnd, compatibleShortReads);
			return value*10000000;	
		}
		return getAverage_ReadsPerBase(absoluteStart, absoluteEnd, compatibleShortReads);	
	}
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
		
		//Take the length of the first element of the first cigar of the first compatible sam record
		int shortReadLength = compatibleShortReads.get(0).getSAMRecord().getCigar().getCigarElement(0).getLength();
		int length = (absoluteEnd-absoluteStart+1);
		int overhang = 4;
		int possibleStartPositions=0;
		if(endExon)
		{
			System.out.println("Short One");
			possibleStartPositions=length-2*(overhang-1);
			
		}else{
			System.out.println("Long One");
			possibleStartPositions=length-2*(overhang-1) + ((shortReadLength-1)-2*(overhang-1));
		}
		System.out.println(count + " " + possibleStartPositions );
		return (float)count/possibleStartPositions * 100;
		
//			(samRecord.getAlignmentStart()>=absoluteStart && samRecord.getAlignmentEnd()<=absoluteEnd)||
//			//Tail of short read is in region
//			(samRecord.getAlignmentStart()<absoluteStart&&samRecord.getAlignmentEnd()>absoluteStart)||
//			//Head of short read is in region
//			(samRecord.getAlignmentStart()<absoluteEnd && samRecord.getAlignmentEnd()>absoluteEnd)){
		// TODO Auto-generated method stub
	}
	/**
	 * Gets the (Sum of reads Per Base) divided by total number of bases.
	 * 
	 * @param absoluteStart the absolute start of the exon
	 * @param absoluteEnd the absolute end of the exon
	 * @param compatibleShortReads the ArrayList of compatible ShortReads
	 * 
	 * @return the sum of reads per base divided by total number of bases
	 */
	public static float getAverage_ReadsPerBase(int absoluteStart, int absoluteEnd, ArrayList<ShortRead> compatibleShortReads){
		//TODO THIS IS NOT EFFICIENT
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
	 * Gets the number of body short reads divided by the exon length
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
			}
			count++;
		}
		return (float)count/(length);
	}
	public static float getAverage_ReadsPerExonLength_OverTotalNumberOfReads(int absoluteStart, int absoluteEnd, ArrayList<ShortRead> compatibleShortReads){
		return (getBodyReads_Per_ExonLength(absoluteStart, absoluteEnd, compatibleShortReads)/totalNumberOfReads);
		 
	}
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
	 * Gets a ArrayList of compatible ShortReads
	 * 
	 * @param isoform the isoform you want to find compatible reads for
	 * @param iSAMRecords the arrayList of SAMRecords you want to check (NonCompatible)
	 * 
	 * @return An ArrayList of compatible ShortReads are returned.
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
	 * (it calls getCompatibleShortReadsFirst)
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
	 * @param samRecord the SAMRecord that you want to find out is compatible or not
	 * @param samInterval the ArrayList of Intervals that contain the genomic coordinates 
	 * 		for which a short read spans
	 * @param exonList the Collection of exons that the SAMRecord might be found in or cross
	 * 
	 * @return a new ShortRead is returned if it is compatible. If it is not, null is returned.
	 */
	private static ShortRead getShortReadIfCompatible(SAMRecord samRecord,ArrayList<Interval> samInterval,Collection<Exon> exonList){
		//1)First find the exon where the short read begins
		//2)Catch the case of a single interval --See if the short read is completely within the exon
		//3)If more than one interval, check each interval with the next ones. Make sure short read 
		//	interval has 'start of exon'  and end of short read has 'end of exon' (except the last) 
		//4) Check that the last interval matches
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
	public static int getNumberOfJunctionReads(MRNA isoform,int absoluteStart, int absoluteEnd,
			ArrayList<SAMRecord> iSAMRecords){
		ArrayList<ShortRead> compatibleShortReads= getCompatibleShortReads(isoform, iSAMRecords);
		int count=0;
		for(ShortRead shortRead:compatibleShortReads){
			//Head
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

}
