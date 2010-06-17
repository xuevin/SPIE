package genomeBrowser;

import gffParser.CDS;
import gffParser.Exon;
import gffParser.GFF3;
import gffParser.Gene;
import gffParser.MRNA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.sql.rowset.spi.SyncResolver;

import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
import net.sf.samtools.SAMRecord;

import processing.core.*;

public class ProcessingApplet extends PApplet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int width;
	private int height;
	private boolean shortReadsPlotVisible,unweightedIsoformsVisible, isCodingStrand
					,splicingLinesVisible,weightedIsoformsVisible;
	private TreeSet<Integer> allScaledEndCoords;
	private List<Line> spliceLines,scaledConsitutiveLines;
	private List<Junction> junctionList;
	private List<GraphColumn> isformSpecificDensityPlot;
	private List<Rectangle> uneweightedIsoforms,referenceIsoforms,weightedIsoforms;
	private List<MRNA> currentlyViewingIsoforms;
	private List<ErrorBar> errorBars;
	private List<Label> unweightedIsoformLabels,referenceLabels;
	
	private ArrayList<Integer> constitutiveUnscaledPositions;
	private HashMap<Integer, Integer> absoluteDensityMap;
	private HashMap<Integer,GraphColumn>scaledShortReadsPlot;
	private int yScale;
	private int graphYStart,graphXEnd,graphXStart,graphYEnd,shortReadPlotYStart;
	//Data Specific to GENE
	private int absoluteStartOfGene;
	private int absoluteLengthOfGene;
	private int absoluteEndOfGene;
	private boolean strandOfGene;
	
	
	public ProcessingApplet(int iWidth, int iHeight){
		width=iWidth;
		height=iHeight;
		isCodingStrand=true;
		
		unweightedIsoformsVisible = false;
		shortReadsPlotVisible=false;
		splicingLinesVisible=false;
		weightedIsoformsVisible=false;
		
		absoluteDensityMap=null;
		spliceLines=null;
		uneweightedIsoforms=null;
		scaledShortReadsPlot=null;
		weightedIsoforms=null;
		currentlyViewingIsoforms=null;
		junctionList=null;
		
		isformSpecificDensityPlot=null;
		graphYStart = 500;
		graphYEnd = 80;
		graphXStart = 200;
		graphXEnd = 900;
		shortReadPlotYStart =600;
	
		
	
	}
	public void setup(){
		//Subtract the title bar height
		int titleBarHeight = getBounds().y;
	    size(width, height-titleBarHeight,P2D);
	    PFont font = loadFont("Courier-16.vlw");
	    textFont(font,16);
	    frameRate(30);
	    smooth();
	    yScale=5;
	    stroke(0);
	    
	}
	public void draw(){
		background(255);
//		line(0, 0, this.mouseX, this.mouseY);
//		line(width, 0, this.mouseX, this.mouseY);
		
		drawSplicingLines();
		drawShortReads();
		drawUnweightedIsoforms();
		drawWeightedIsoforms();
		drawLabelForHeight();
		drawHoverInfo();
		
	}
	public void setNewSize(int iWidth, int iHeight){
		width=iWidth;
		height=iHeight;
		graphYStart = (int) (((double)5/8) *iHeight);
		graphYEnd = (int) (((double)1/10)*iHeight);
		graphXStart = 200;//(int) (((double)2/10)*iWidth);
		graphXEnd = (int) (((double)9/10)*iWidth);
		shortReadPlotYStart =(int) (((double)6/8)*iHeight);
		int titleBarHeight = getBounds().y;
	    setSize(iWidth, iHeight-titleBarHeight);
	    
//		graphYStart = 500;
//		graphYEnd = 80;
//		graphXStart = 200;
//		graphXEnd = 900;
//		shortReadPlotYStart =600;
	}
	public void mousePressed(){
		if(mouseButton==RIGHT){
			//remove a mrna
			//reload with new list
			if(unweightedIsoformsVisible){
				int indexToRemove = (mouseY+5-graphYEnd)/30;
				if(indexToRemove>=0&&indexToRemove<currentlyViewingIsoforms.size()){
					currentlyViewingIsoforms.remove(indexToRemove);
					loadArrayOfUnweightedIsoforms(currentlyViewingIsoforms, absoluteStartOfGene, absoluteLengthOfGene, strandOfGene);
					
				}
			}				
		}
	}
	
	/**
	 * Flip. Flips the isoform so that the user can see the alternate strand
	 */
	public void flip() {
		isCodingStrand=!isCodingStrand;
		if(uneweightedIsoforms!=null){
			for(Rectangle rectangle:uneweightedIsoforms){
				rectangle.setScaledXCoord(reverse(rectangle.getScaledXCoord()+rectangle.getScaledLength()));
			}	
		}
		if(spliceLines!=null){
			for(Line line:spliceLines){	
				line.setXCoordStart(reverse(line.getXCoordStart()));
				line.setXCoordEnd(reverse(line.getXCoordEnd()));
			}	
		}
		if(scaledShortReadsPlot!=null){
			for(GraphColumn column:scaledShortReadsPlot.values()){
				column.setScaledX(reverse(column.getScaledX()));
			}
		}
		if(weightedIsoforms!=null){
			for(Rectangle rectangle:weightedIsoforms){
				rectangle.setScaledXCoord(reverse(rectangle.getScaledXCoord()+rectangle.getScaledLength()));
			}
		}
		if(errorBars!=null){
			for(ErrorBar errorBar:errorBars){
				errorBar.setScaledXCoord(reverse(errorBar.getScaledXPosition()));
			}
		}
		if(referenceIsoforms!=null){
			for(Rectangle rectangle:referenceIsoforms){
				rectangle.setScaledXCoord(reverse(rectangle.getScaledXCoord()+rectangle.getScaledLength()));
			}
		}
		if(scaledConsitutiveLines!=null){
			for(Line line:scaledConsitutiveLines){	
				line.setXCoordStart(reverse(line.getXCoordStart()));
				line.setXCoordEnd(reverse(line.getXCoordEnd()));
			}
		}
		if(junctionList!=null){
			for(Junction junction:junctionList){
				junction.setLeftScaled(reverse(junction.getLeftScaled()));
				junction.setRightScaled(reverse(junction.getRightScaled()));
			}
		}
		
	}
	public void setShortReadsVisible(boolean selected) {
		shortReadsPlotVisible=selected;	
	}
	public void setUnweightedIsoformsVisible(boolean selected) {
		unweightedIsoformsVisible=selected;
	}
	public void setSpliceLinesVisible(boolean selected) {
		splicingLinesVisible=selected;
	}
	public void setWeightedIsoformsVisible(boolean selected){
		weightedIsoformsVisible=selected;
	}
	/**
	 * @param mrnaList
	 * @param iAbsoluteStartOfGene
	 * @param iAbsoluteLengthOfGene
	 * @param strand
	 *  
	 */
	public synchronized void loadArrayOfUnweightedIsoforms(Collection<MRNA> isoforms,int iAbsoluteStartOfGene,int iAbsoluteLengthOfGene,boolean strand) {
		absoluteStartOfGene=iAbsoluteStartOfGene;
		absoluteLengthOfGene=iAbsoluteLengthOfGene;
		absoluteEndOfGene=iAbsoluteStartOfGene+iAbsoluteLengthOfGene-1;
		isCodingStrand = strand;
		
		uneweightedIsoforms = Collections.synchronizedList(new ArrayList<Rectangle>());
		spliceLines = Collections.synchronizedList(new ArrayList<Line>());
		unweightedIsoformLabels = Collections.synchronizedList(new ArrayList<Label>());
		ArrayList<MRNA> newListOfMRNA = new ArrayList<MRNA>();
		
		identifyConstituitiveBases(isoforms);
		scaledConsitutiveLines=Collections.synchronizedList(new ArrayList<Line>());
		
		//The code below fill in rectanglesToDraw with scaled values
		if(isoforms!=null){
			int yPosition = graphYEnd;	
			for(MRNA isoform: isoforms){
				newListOfMRNA.add(isoform);
				
				int color = color(40);//DarkGrey
				//sortedRectangles are used to draw the spliceLines
				ArrayList<Rectangle> sortedRectangles= new ArrayList<Rectangle>();
				
				for(Exon exons :isoform.getExons().values()){
					float scaledLength= map(exons.getLength(),0,absoluteLengthOfGene,0,graphXEnd-graphXStart);
					float scaledStart;
					
					if(isCodingStrand){
						scaledStart = map(exons.getStart(),absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd);
					}else{
						scaledStart = reverse(map(exons.getEnd(),absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd));
					}

					color=color(40);
					Rectangle temp = new Rectangle(scaledStart, yPosition+5, scaledLength, 
							10,exons.getStart(),exons.getEnd(),isoform.getId(),color);
					
					uneweightedIsoforms.add(temp);
					sortedRectangles.add(temp);
					
					float scaledPosition;
					for(Integer i :constitutiveUnscaledPositions){
						if(i>=exons.getStart()&&i<=exons.getEnd()){
							if(isCodingStrand){
								scaledPosition = map(i,absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd);
							}else{
								scaledPosition = reverse(map(i,absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd));
							}
							scaledConsitutiveLines.add(new Line(scaledPosition, yPosition+5, scaledPosition, yPosition+16));	
						}
					}
										
				}
				color = color(255,150); //Transparent
				for(CDS cds :isoform.getCDS().values()){
					//FIXME <<Slight issue with mapping
					float scaledLength= map(cds.getLength(),0,absoluteLengthOfGene,0,graphXEnd-graphXStart);
					float scaledStart;
					if(isCodingStrand){
						scaledStart =  map(cds.getStart(),absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd);
					}else{
						scaledStart = reverse(map(cds.getEnd(),absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd));
					}
					Rectangle temp = new Rectangle(scaledStart, yPosition, scaledLength, 20,cds.getStart(),cds.getEnd(),isoform.getId(),color);
					uneweightedIsoforms.add(temp);
					//sortedRectangles.add(temp);
				}

				color = color(0,255,0, 100);//Green
				float startCodonScaled=0;	//These Values are used to help plot the correct height to the CDS
				float stopCodonScaled=0; 	//These Values are used to help plot the correct height to the CDS
				if(isoform.getStartCodon()!=null){
					float scaledLength=map(isoform.getStartCodon().getLength(),0,absoluteLengthOfGene,
							0,graphXEnd-graphXStart);
					float scaledStart;
					if(isCodingStrand){
						scaledStart =  map(isoform.getStartCodon().getStart(),
									absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd);
						startCodonScaled=scaledStart;
					}else{
						scaledStart = reverse(map(isoform.getStartCodon().getEnd(),
								absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd));
						startCodonScaled=scaledStart;
					}
					uneweightedIsoforms.add(new Rectangle(scaledStart, yPosition, scaledLength, 
							20,isoform.getStartCodon().getStart(),isoform.getStartCodon().getEnd(),
							isoform.getId(),color));	
				}
				if(isoform.getStopCodon()!=null){
					float lengthScaled= map(isoform.getStopCodon().getLength(),0,absoluteLengthOfGene,0,graphXEnd-graphXStart);
					float startScaled;
					if(isCodingStrand){
						startScaled = map(isoform.getStopCodon().getStart(),absoluteStartOfGene,
								absoluteEndOfGene,graphXStart,graphXEnd);
						stopCodonScaled=startScaled;
						
					}else{
						startScaled = reverse(map(isoform.getStopCodon().getEnd(),
								absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd));
						stopCodonScaled=startScaled;
					}
					uneweightedIsoforms.add(new Rectangle(startScaled, yPosition, lengthScaled, 
							20,isoform.getStopCodon().getStart(),isoform.getStopCodon().getEnd(),
							isoform.getId(),color));
				}
				unweightedIsoformLabels.add(new Label(isoform.getId(), 10, yPosition+20));
				
				//****************************************
				//************************
				//******************
				//**********
				//*******
				//FIXME
				Collections.sort(sortedRectangles,new RectangleComparator());
				//THis is sorted on the start position of the scaled xPositions;
				float scaledLastExonEnd=0;
				float scaledLastExonY=0;
				for(int i =0;i<sortedRectangles.size();i++){
					//System.out.println(sortedRectangles.get(i).getXCoord());
					Rectangle rect = sortedRectangles.get(i);
					if(i!=0){
							float midPoint = (scaledLastExonEnd+rect.getScaledXCoord())/2;
							spliceLines.add(new Line(scaledLastExonEnd,scaledLastExonY,midPoint,yPosition-10));	
							spliceLines.add(new Line(midPoint,yPosition-10,rect.getScaledXCoord(),rect.getScaledYCoord()));
//						}
					}
//					if(lastExonEnd<rect.getXCoord()+rect.getLength()){
						scaledLastExonEnd = rect.getScaledXCoord()+rect.getScaledLength();
						scaledLastExonY=rect.getScaledYCoord();
//					}
				}
				//************************
				//****************************************				
				
				yPosition +=30;
			}
		}
		
		currentlyViewingIsoforms=newListOfMRNA;
	}
	public synchronized void loadArrayOfWeightedIsoforms(Collection<MRNA> isoforms,int iAbsoluteStartOfGene,int iAbsoluteLengthOfGene,boolean strand){
		absoluteStartOfGene=iAbsoluteStartOfGene;
		absoluteLengthOfGene=iAbsoluteLengthOfGene;
		absoluteEndOfGene=iAbsoluteStartOfGene+iAbsoluteLengthOfGene-1;
		isCodingStrand = strand;
		
		//Under the condition that the loadShortReads function has already been called
		if(absoluteDensityMap!=null){
			
			//Make new arrays
			weightedIsoforms = Collections.synchronizedList(new ArrayList<Rectangle>());
			referenceIsoforms=Collections.synchronizedList(new ArrayList<Rectangle>());
			referenceLabels = Collections.synchronizedList(new ArrayList<Label>());
			errorBars = Collections.synchronizedList(new ArrayList<ErrorBar>());
			ArrayList<MRNA> newListOfMRNA = new ArrayList<MRNA>();
			//Clear Junctions
			junctionList=null;
			
			identifyConstituitiveBases(isoforms);
			scaledConsitutiveLines=Collections.synchronizedList(new ArrayList<Line>());
			
			int yRefStart=30; // Start of where the reference labels go
			//For each isoform
			for(MRNA mrna:isoforms){
				newListOfMRNA.add(mrna);
				referenceLabels.add(new Label(mrna.getId(), 10, yRefStart+10));
				//Grey
				int color = color(40);//DarkGrey
				float yPosition = graphYStart;
				
				//Check each exon
				for(Exon exons :mrna.getExons().values()){
					//Find the average of the region in mention
					int sum = 0;
					for(Integer heightAtPosition:getArrayOfHeights(exons.getStart(), exons.getEnd())){
						sum+=heightAtPosition;
					}
					float average = ((float)sum/exons.getLength());
					
					//Scale the length and start
					float lengthScaled=map(exons.getLength(),0,absoluteLengthOfGene,0,graphXEnd-graphXStart);
					float startScaled;
					if(isCodingStrand){
						startScaled = map(exons.getStart(),absoluteStartOfGene,absoluteEndOfGene,
								graphXStart,graphXEnd);
					}else{
						startScaled = reverse(map(exons.getEnd(),absoluteStartOfGene,absoluteEndOfGene,
								graphXStart,graphXEnd));
					}
					
					color=color(40);
					Rectangle temp = new Rectangle(startScaled, yPosition-average, lengthScaled,10,
							exons.getStart(),exons.getEnd(),mrna.getId(),color);
					weightedIsoforms.add(temp);
					errorBars.add(new ErrorBar(getArrayOfHeights(exons.getStart(),exons.getEnd()),mrna.getId(),
							(startScaled+lengthScaled/2), yPosition-average));
					
					//Fill in array that draws the reference isoforms
					Rectangle temp2 = new Rectangle(startScaled, yRefStart, lengthScaled,10,exons.getStart(),
							exons.getEnd(),mrna.getId(),color);
					referenceIsoforms.add(temp2);
					float scaledPosition;

					//Fill in array that draws the scaled constitutive lines
					//The difference here is that that the first YStartPosition represents the absolute height
					//It still needs to be scaled (look at drawWeightedConstitutiveLines)
					for(Integer i :constitutiveUnscaledPositions){
						if(i>=exons.getStart()&&i<=exons.getEnd()){
							if(isCodingStrand){
								scaledPosition = map(i,absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd);
							}else{
								scaledPosition = reverse(map(i,absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd));
							}
							scaledConsitutiveLines.add(new Line(scaledPosition, yPosition-average, scaledPosition, yPosition-average+10));	
						}
					}
					
					
				}
	
				color = color(255,150); //Transparent
				for(CDS cds:mrna.getCDS().values()){
					float lengthScaled=map(cds.getLength(),0,absoluteLengthOfGene,0,graphXEnd-graphXStart);
					float startScaled;
					if(isCodingStrand){
						startScaled = map(cds.getStart(),absoluteStartOfGene,absoluteEndOfGene,
								graphXStart,graphXEnd);
					}else{
						startScaled = reverse(map(cds.getEnd(),absoluteStartOfGene,absoluteEndOfGene,
								graphXStart,graphXEnd));
					}
					
					//Get the rectangle that is nearest to the xCoordinate who has the same MRNA
					//to get the appropriate height
					yPosition=getRectangleNearestToXCoord(weightedIsoforms,mrna.getId(),startScaled).getScaledYCoord();
					Rectangle temp = new Rectangle(startScaled, yPosition, lengthScaled,20,
							cds.getStart(),cds.getEnd(),mrna.getId(),color);
					weightedIsoforms.add(temp);
					Rectangle temp2 = new Rectangle(startScaled, yRefStart, lengthScaled,10,
							cds.getStart(),cds.getEnd(),mrna.getId(),color);
					referenceIsoforms.add(temp2);
				}
				yRefStart+=10;
			}
			currentlyViewingIsoforms=newListOfMRNA;
			identifyConstituitiveBases(isoforms);
		}else{
			System.err.println("A call was made to loadWeightedIsoforms but there was no shortReadData");
		}
	}
	/**
	 * @param iShortReads
	 */
	public synchronized void loadShortReads(ArrayList<SAMRecord> iShortReads){
		//First load an absolute density plot hash
		absoluteDensityMap = new HashMap<Integer, Integer>();
		for(int i = absoluteStartOfGene;i<=(absoluteEndOfGene);i++){
			absoluteDensityMap.put(i,0);	
		}
		for(SAMRecord samRecord:iShortReads){
			//** CIGAR PARSING
			List<CigarElement> cigarElements = samRecord.getCigar().getCigarElements();
			int start = samRecord.getAlignmentStart();
			for(CigarElement cigarElement:cigarElements){
				if(cigarElement.getOperator().equals(CigarOperator.M)){
					for(int i = 0;i<cigarElement.getLength();i++){
						int prev = absoluteDensityMap.get(start);
						prev++;
						absoluteDensityMap.put(start, prev);
						start++;
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
		}
		
		fillScaledShortReadsPlot();
	}
	public synchronized ArrayList<ShortRead> loadCompatibleReads(MRNA isoform, ArrayList<SAMRecord> iShortReads){
		junctionList=Collections.synchronizedList(new ArrayList<Junction>()); 
		ArrayList<ShortRead> arrayOfCompatibleShortReads = new ArrayList<ShortRead>();

		
		int count = 0;
		for(SAMRecord samRecord:iShortReads){
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
			ShortRead temp =getShortReadIfCompatible(samRecordIntervals, isoform.getExons().values());  
			if(temp!=null){
				arrayOfCompatibleShortReads.add(temp);
			}else{
				//skip
			}
		}		
	
		for(ShortRead shortRead:arrayOfCompatibleShortReads){
			if(shortRead.isJunctionRead()){
				float startScaled = scaleAbsoluteCoord(shortRead.getFirstExonEnd());
				float endScaled = scaleAbsoluteCoord(shortRead.getLastExonBeginning());
//				if(isCodingStrand){
//					startScaled = map(shortRead.getFirstExonEnd(),absoluteStartOfGene,absoluteEndOfGene,
//							graphXStart,graphXEnd);
//					endScaled = map(shortRead.getLastExonBeginning(),absoluteStartOfGene,absoluteEndOfGene,
//							graphXStart,graphXEnd);
//				}else{
//					startScaled = reverse(map(shortRead.getFirstExonBeginning(),absoluteStartOfGene,absoluteEndOfGene,
//							graphXStart,graphXEnd));
//					endScaled = reverse(map(shortRead.getLastExonEnd(),absoluteStartOfGene,absoluteEndOfGene,
//							graphXStart,graphXEnd));
//				}
				
				
				
				boolean found =false;
				for(Junction junction:junctionList){
					if(junction.getLeftScaled()==startScaled && junction.getRightScaled()==endScaled){
						junction.increaseHit();
						found=true;
					}					
				}
				if(!found){
					junctionList.add(new Junction(startScaled,endScaled,1));
				}
				
			}
		}
		System.out.println(arrayOfCompatibleShortReads.size());
		return arrayOfCompatibleShortReads;
	}
	public void loadWeightsOfCurrentlyShowingIsoforms(){
		if(currentlyViewingIsoforms!=null){
			loadArrayOfWeightedIsoforms(currentlyViewingIsoforms,absoluteStartOfGene,absoluteLengthOfGene,strandOfGene);
		}
	}
	public void setYScale(int iYScale){
		yScale=iYScale;
	}
	private void fillInIsoformSpecificDensityPlot(ArrayList<SAMRecord> iShortReads,MRNA iMRNA) {
		isformSpecificDensityPlot= Collections.synchronizedList(new ArrayList<GraphColumn>());
			
	}
	private synchronized void fillScaledShortReadsPlot(){
		scaledShortReadsPlot = new HashMap<Integer, GraphColumn>();
		//Make a line for every Pixel
		for(int i =graphXStart;i<=graphXEnd;i++){
			if(isCodingStrand){
				scaledShortReadsPlot.put(i,(new GraphColumn(i, shortReadPlotYStart, 0, 0, 0)));	
			}else{
				scaledShortReadsPlot.put(i, (new GraphColumn(reverse(i), shortReadPlotYStart, 0, 0, 0)));
			}	
		}
		int prevPixel = -1;
		int currentSum=-1;
		int numberOfShortReads=-1;
		int frameAbsoluteStart=-1;
		int frameAbsoluteEnd = -1;
		
		for(int i = absoluteStartOfGene;i<=absoluteEndOfGene;i++){
			int mappedPixel = (int) map(i,absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd);
			//System.out.println(mappedPixel +"---Abs" + iAbsoluteDensity.get(i));
			if(prevPixel==-1){
				frameAbsoluteStart=i;
				frameAbsoluteEnd=i;
				numberOfShortReads=1;
				currentSum=absoluteDensityMap.get(i);
				prevPixel=mappedPixel;
			}else if(mappedPixel!= prevPixel){
				scaledShortReadsPlot.get(prevPixel).incrementHeight((int)((double)currentSum/numberOfShortReads));
				scaledShortReadsPlot.get(prevPixel).setAbsoluteXCoords(frameAbsoluteStart,frameAbsoluteEnd);
				prevPixel=mappedPixel;
				numberOfShortReads=1;
				currentSum=absoluteDensityMap.get(i);
				frameAbsoluteStart=i;
				frameAbsoluteEnd=i;
			}else if(i==absoluteEndOfGene){
				frameAbsoluteEnd=i;
				numberOfShortReads++;
				scaledShortReadsPlot.get(prevPixel).incrementHeight((int)((double)currentSum/numberOfShortReads));
				scaledShortReadsPlot.get(prevPixel).setAbsoluteXCoords(frameAbsoluteStart,frameAbsoluteEnd);
			}else{	
				currentSum+=absoluteDensityMap.get(i);
				frameAbsoluteEnd=i;
				numberOfShortReads++;
			} 
//			System.out.println(i + "----"  + iAbsoluteDensity.get(i));
		}
	}
	/**
	 * Draws the splice lines that connect the exons and cds.
	 * Draws only under the condition that splicingLinesVisible is true, geneVisible is true,
	 * and linesToDraw is not null and rectanglesToDraw is not null.
	 */
	private void drawSplicingLines() {
		if(splicingLinesVisible&&unweightedIsoformsVisible&&spliceLines!=null&&uneweightedIsoforms!=null){
			synchronized (spliceLines) {
				for(Line line:spliceLines){
					line(line.getXCoordStart(),line.getYCoordStart(),line.getXCoordEnd(),line.getYCoordEnd());
				}	
			}
				
		}
	}
	private void drawUnweightedIsoforms(){
		if(unweightedIsoformsVisible&&uneweightedIsoforms!=null){
			synchronized (uneweightedIsoforms) {
				for(Rectangle rectangle:uneweightedIsoforms){
					fill(rectangle.getColor());
					rect(rectangle.getScaledXCoord(),rectangle.getScaledYCoord(),rectangle.getScaledLength(),rectangle.getScaledHeight());
				}
			}
			fill(0);
			drawUnweightedIsoformLabels();
			drawUnweightedConstitutiveLines();
		}
	}
	private void drawReferenceIsoform() {
		if(referenceIsoforms!=null){
			synchronized (referenceIsoforms) {
				for(Rectangle rectangle:referenceIsoforms){
					fill(rectangle.getColor());
					rect(rectangle.getScaledXCoord(),rectangle.getScaledYCoord(),rectangle.getScaledLength(),rectangle.getScaledHeight());	
				}
			}
			fill(0);
			drawLabelsForReference();		
		}
		
	}
	private void drawLabelsForReference() {
		
		if(referenceLabels!=null){
			synchronized (referenceLabels) {
				for(Label label:referenceLabels){	
					text(label.getText(),label.getXScaled(),label.getYScaled());
				}	
			}
				
		}else{
			System.err.println("Reference Labels are Null!");
		}
		
		
	}
	private void drawWeightedIsoforms() {
		if(weightedIsoforms!=null && weightedIsoformsVisible){
			drawGrid();
			synchronized (weightedIsoforms) {
				for(Rectangle rectangle:weightedIsoforms){
					fill(rectangle.getColor());
					float newScaledYCoord = graphYStart-((graphYStart-rectangle.getScaledYCoord())*yScale);
					//Because the rectangle is drawn from the corner, subtract half its height;
					newScaledYCoord=newScaledYCoord-(rectangle.getScaledHeight()/2);
					rect(rectangle.getScaledXCoord(),newScaledYCoord,rectangle.getScaledLength(),rectangle.getScaledHeight());	
				}
			}
			fill(0);
			drawErrorBars();
			drawReferenceIsoform();
			drawWeightedConstitutiveLines();
			drawJunctionLines();
		}
		
	}
	private void drawJunctionLines() {
		if(junctionList!=null){
			synchronized (junctionList) {
				stroke(0,100);
				strokeWeight(5);
				for(Junction junction:junctionList){
					int yPos=605;
					for(int i=0;i<junction.getHits();i++){
						line(junction.getLeftScaled(),yPos,junction.getRightScaled(),yPos);
						yPos+=5;
					}
				}
				strokeWeight(1);
				stroke(1);
			}
		}
		
		
	}
	/**
	 * Draws the short read plot under the condition that densityPlot is not null and shortReadsVisible is true.
	 */
	private void drawShortReads(){
		if(scaledShortReadsPlot!=null && shortReadsPlotVisible){
			synchronized (scaledShortReadsPlot) {
				for(GraphColumn line:scaledShortReadsPlot.values()){
					stroke(0);
					line(line.getScaledX(),line.getScaledYStart(),line.getScaledX(),line.getScaledYEnd());
				}	
			}
			
		}
	}
	/**
	 * Draws a label that describes the height at each xCoordinate.
	 */
	private void drawLabelForHeight(){
		if(scaledShortReadsPlot!=null){
			if(mouseX>=graphXStart && mouseX<=graphXEnd){
				text("Average Height "+ getShortReadDensityHeightAt(mouseX) + " @ "+ 
						"\n"+getAbsoluteCoordinates(mouseX), 350, 650);
				line(mouseX,shortReadPlotYStart,400,620);
			}	
		}
		
	}
	private void drawUnweightedIsoformLabels() {
		if(unweightedIsoformLabels!=null){
			synchronized (unweightedIsoformLabels) {
				for(Label label: unweightedIsoformLabels){
					text(label.getText(),label.getXScaled(),label.getYScaled());
				}	
			}
			
		}
	
		
	}
	/**
	 * Draws a grid for the density plot
	 */
	private void drawGrid() {
		line(graphXStart,graphYStart+5,graphXEnd,graphYStart+5);
		line(graphXEnd,graphYStart+5,graphXEnd,50);
		int count =0;
		for(int i =graphYStart+5;i>graphYEnd; i-=(yScale)){
			if(count%5==0){
				text(""+count,graphXEnd,i);
				line(graphXStart,i,graphXEnd,i);
			}
			count++;	
		}
	}
	
	private void drawErrorBars(){
			if(errorBars!=null){
				synchronized (errorBars) {
					for(ErrorBar error:errorBars){
						double SD = error.getStandardDeviation()*yScale;
						float newScaledYPosition = graphYStart-(yScale*(graphYStart-error.getScaledYPosition()));
						int start = (int) (newScaledYPosition-SD);
						int end = (int) (newScaledYPosition+SD);
						line(error.getScaledXPosition(),start,error.getScaledXPosition(),end);
	//					strokeWeight(10);
	//					point(error.getScaledXPosition(),newScaledYPosition);
	//					strokeWeight(1);
					}	
				}
			}
		}
	/**
	 * Gets the short read density height at the specified xPixel
	 * 
	 * @param pixelPosition is the pixel you want the height of
	 * 
	 * @return the short read density height at
	 */
	private int getShortReadDensityHeightAt(int pixelPosition){
		if(scaledShortReadsPlot!=null){
			if(pixelPosition == scaledShortReadsPlot.get(pixelPosition).getScaledX()){
				return scaledShortReadsPlot.get(pixelPosition).getUnscaledHeight();
			}else{
				return scaledShortReadsPlot.get((int)reverse(pixelPosition)).getUnscaledHeight();
			}
		}else{
			System.err.println("An attempt was made to fetch the height of coordinate but scaledDensityPlot was null");
			return 0;
		}
	}
	/**
	 * @param pixelPosition is the pixel you want to look up. 
	 * @return the string describes the absolute coordinates that the pixel covers
	 */
	private String getAbsoluteCoordinates(int pixelPosition){
		if(scaledShortReadsPlot!=null){
			if(pixelPosition == scaledShortReadsPlot.get(pixelPosition).getScaledX()){
				return scaledShortReadsPlot.get(pixelPosition).getAbsoluteXStart()+"-"+ scaledShortReadsPlot.get(pixelPosition).getAbsoluteXEnd();
			}else{
				return scaledShortReadsPlot.get((int)reverse(pixelPosition)).getAbsoluteXStart()+"-"+ scaledShortReadsPlot.get((int)reverse(pixelPosition)).getAbsoluteXEnd();
			}
		}else{
			System.err.println("An attempt was made to fetch the span of the coordinate but scaledDensityPlot was null");
			return "Empty";
		}
	}
	private ArrayList<Integer> getArrayOfHeights(int absoluteStart, int absoluteEnd){
		ArrayList<Integer> arrayOfInts = new ArrayList<Integer>();
		for(int i =absoluteStart;i<=absoluteEnd;i++){
			arrayOfInts.add(absoluteDensityMap.get(i));
		}
		return arrayOfInts;
	}
	private Rectangle getRectangleNearestToXCoord(List<Rectangle> listOfRectangles,String mrnaID ,float iXCoord){
		//Gets the rectangle closest to the specified xCoord by cycling through;
		Rectangle closest = null;
		float howFar =99999;
		for(Rectangle rect:listOfRectangles){
			if(rect.getIsoformID()==mrnaID){
				if(closest==null){
					closest=rect;
				}
				if(abs(rect.getScaledXCoord()-iXCoord)<howFar){
					closest=rect;
					howFar=abs(rect.getScaledXCoord()-iXCoord);
				}
				if(abs(rect.getScaledXCoord()+rect.getScaledLength()-iXCoord)<howFar){
					closest=rect;
					howFar=abs(rect.getScaledXCoord()+rect.getScaledLength()-iXCoord);
				}	
			}
			
		}
		return closest;
	}
	private void drawHoverInfo(){
		if(weightedIsoformsVisible && errorBars!=null){
			if(mouseX<=graphXEnd && mouseX>=graphXStart){
				ErrorBar eBars = getErrorBarsClosestToXPosition(errorBars, mouseX, mouseY);
				if(eBars!=null){
					text(eBars.getIsoformID()+"\n"+eBars.getAbsoluteHeight(),mouseX,mouseY);	
				}else{
					System.err.println("A request for information on a weighted exon was made but it was not available");
				}	
			}
			
		}
		
	}
	/**
	 * @param An array of ErrorBars.
	 * @param iMouseX
	 * @param iMouseY
	 * @return an ErrorBar only if the distance is less than 10
	 */
	private ErrorBar getErrorBarsClosestToXPosition(List<ErrorBar> errorBars2,
			int iMouseX, int iMouseY) {
		ErrorBar closest =null;
		float howFar = 99999;
		synchronized (errorBars2) {
			for(ErrorBar eBar:errorBars2){
				float compare = dist(eBar.getScaledXPosition(),iMouseY,iMouseX,iMouseY);
				if(closest==null){
					closest=eBar;
					howFar=compare;
				}else if(compare<howFar){
					closest=eBar;
					howFar=compare;
				}
			}	
		}
		
		return closest;
	}
	private Rectangle getRectangleNearestToCoords(List<Rectangle> listOfRectangles,int iXCoord, int iYCoord){
		Rectangle closest =null;
		float howFar = 99999;
		for(Rectangle rect:listOfRectangles){
			float compare = dist(rect.getScaledXCoord(), rect.getScaledYCoord(), iXCoord, iYCoord);
			float compare2 = dist(rect.getScaledXCoord()+rect.getScaledLength(), rect.getScaledYCoord(), iXCoord, iYCoord);
			if(rect==null){
				closest=rect;
			}
			if(compare<howFar){
				closest=rect;
				howFar=compare;
			}
			if(compare2<howFar){
				closest=rect;
				howFar=compare2;
			}
		}
		return closest;
		
	}
	private float reverse(float position){
		return (graphXEnd-position)+graphXStart;
	}
	private void drawUnweightedConstitutiveLines(){
		if(scaledConsitutiveLines!=null){
			synchronized (scaledConsitutiveLines) {
				stroke(0,0,255);
				for(Line line:scaledConsitutiveLines){	
					line(line.getXCoordStart(),line.getYCoordStart(),line.getXCoordEnd(),line.getYCoordEnd());	
				}
				stroke(0);
				
			}
		}
	}
	private void drawWeightedConstitutiveLines(){
		if(scaledConsitutiveLines!=null){
			synchronized (scaledConsitutiveLines) {
				stroke(0,0,255);
				for(Line line:scaledConsitutiveLines){
					float newYPosition = graphYStart-(yScale*(graphYStart-line.getYCoordStart()));
					line(line.getXCoordStart(),newYPosition-5,line.getXCoordEnd(),newYPosition+6);	
				}
				stroke(0);
				
			}
		}
	}
	
	private void identifyConstituitiveBases(Collection<MRNA> mrnaList){
		HashMap<Integer,Integer> allPositions = new HashMap<Integer,Integer>();
		for(int i = absoluteStartOfGene;i<=(absoluteStartOfGene+absoluteLengthOfGene-1);i++){
			allPositions.put(i,0);
		}
		//For all MRNA
		for(MRNA mrna:mrnaList){
			//For All Exons
			for(Exon exon:mrna.getExons().values()){
				//For all positions in Exon
				for(int y=exon.getStart();y<=exon.getEnd();y++){
					//Take a tally of how many exons exist for a certain base
					//Those sites with a tally equal to the size of the MRNA list are constitutive
					int prev = allPositions.get(y);
					prev++;
					allPositions.put(y,prev);
				}
			}
		}
		constitutiveUnscaledPositions = new ArrayList<Integer>();
		if(mrnaList.size()>1){
			for(int i = absoluteStartOfGene;i<=absoluteEndOfGene;i++){
				if(allPositions.get(i)==mrnaList.size()){
//					//position is constitutive among the MRNA in the list
//					float scaledPosition;
//					if(isCodingStrand){
//						scaledPosition = map(i,absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd);
//					}else{
//						scaledPosition = reverse(map(i,absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd));
//					}
//					constitutiveUnscaledPositions.add(scaledPosition);
					constitutiveUnscaledPositions.add(i);
				}
			}	
		}
	}
	/**
	 * Checks if position is found among all isoforms. Performs an exhaustive search among all exons start coords
	 * 
	 * @param mrnaList the MRNA list to check against
	 * @param absolutePosition the absolute position
	 * 
	 * @return true, if checks if is found among all pieces
	 */
	private boolean isFoundAmongAllMRNA(Collection<MRNA> mrnaList,int absolutePosition){
		if(mrnaList.size()==1){
			return false;
		}
		boolean stillFound = false;
		for(MRNA mrna: mrnaList){
			stillFound = false;
			for(Exon exon: mrna.getExons().values()){
				if(exon.getStart()==absolutePosition){
					stillFound=true;
					break;
				}	
			}
			if(stillFound==false){
				return false;
			}	
		}
		return true;
	}
	
	/**
	 * @deprecated
	 * Find compatible short reads.
	 *  
	 * @param isoform the Isoform for which you want to find compatible reads for
	 * @param iShortReads An ArrayList of SAMRecords that may or may not be compatible with the isoform
	 * 
	 * @return An ArrayList of compatible SAMRecords are returned
	 */
	public ArrayList<SAMRecord> findCompatibleShortReads(MRNA isoform, ArrayList<SAMRecord> iShortReads){
		ArrayList<SAMRecord> arrayOfCompatibleShortReads = new ArrayList<SAMRecord>(); 
		
		for(SAMRecord samRecord:iShortReads){
			//Make a hash that will represent the positions that a short read covers
			HashMap<Integer, Integer> shortReadTally = new HashMap<Integer,Integer>();
			//Fill the temp hash with all zeros
			for(int i=samRecord.getAlignmentStart();i<=samRecord.getAlignmentEnd();i++){
				shortReadTally.put(i, 0);
			}
			
			
			//** CIGAR PARSING
			List<CigarElement> cigarElements = samRecord.getCigar().getCigarElements();
			int start = samRecord.getAlignmentStart();
			for(CigarElement cigarElement:cigarElements){
				if(cigarElement.getOperator().equals(CigarOperator.M)){
					for(int i = 0;i<cigarElement.getLength();i++){
						int prev = shortReadTally.get(start);
						prev++;
						shortReadTally.put(start, prev);
						start++;
					}
					//System.out.println("M"+cigarElement.getLength());
				}else if(cigarElement.getOperator().equals(CigarOperator.N)){
					start=start+cigarElement.getLength();
					//System.out.println("N"+cigarElement.getLength());
				}else if(cigarElement.getOperator().equals(CigarOperator.D)){
					start=start+cigarElement.getLength();
					System.out.println("D"+cigarElement.getLength());
				}	
			}
			//ShortReadTally now has all the positions that have a short read crossing it
			//Now cross ref it with all the exons to see if there are gaps (positions with only one)
			for(Exon exon:isoform.getExons().values()){
				//If the exon start is less than or equal to the alignment start
				for(int i = exon.getStart();i<=exon.getEnd();i++){
					if(i>=samRecord.getAlignmentStart() && i<=samRecord.getAlignmentEnd()){
						int prev = shortReadTally.get(i);
						prev++;
						shortReadTally.put(i, prev);
					}
				}
			}
			boolean compatible = true;
			for(Integer sum :shortReadTally.values()){
				if(sum==1){
					compatible=false;
					break;
				}else{
					
				}
			}
			if(compatible){
				arrayOfCompatibleShortReads.add(samRecord);
			}else{
			}
		}
		System.out.println(arrayOfCompatibleShortReads.size());
		return arrayOfCompatibleShortReads;
	}
	private ShortRead getShortReadIfCompatible(ArrayList<Interval> samInterval,Collection <Exon> exonsCollection){
		//1)First find the exon where the short read begins
		//2)Catch the case of a single interval --See if the short read is completely within the exon
		//3)If more than one interval, check each interval with the next ones. Make sure short read 
		//	interval has 'start of exon'  and end of short read has 'end of exon' (except the last) 
		//4) Check that the last interval matches
		if(samInterval.size()==0){
			System.err.println("samInterval Was Empty");
			return null;
		}
		
		ShortRead newShortRead = new ShortRead(samInterval);
		
		for(int i =0;i<samInterval.size();i++){
			if(i==0){
				boolean found = false;
				for(Exon exon: exonsCollection){
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
				for(Exon exon: exonsCollection){
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
				for(Exon exon: exonsCollection){
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
	private float scaleAbsoluteCoord(float inCoord){
		float scaledCoord;
		if(isCodingStrand){
			scaledCoord = map(inCoord,absoluteStartOfGene,absoluteEndOfGene,
					graphXStart,graphXEnd);
		}else{
			scaledCoord = reverse(map(inCoord,absoluteStartOfGene,absoluteEndOfGene,
					graphXStart,graphXEnd));
		}
		return scaledCoord;
	}
}
