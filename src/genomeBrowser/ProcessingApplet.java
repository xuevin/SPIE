package genomeBrowser;

import gffParser.CDS;
import gffParser.Exon;
import gffParser.MRNA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import drawableObjects.ConstitutiveLine_Unweighted;
import drawableObjects.ConstitutiveLine_Weighted;
import drawableObjects.ErrorBar;
import drawableObjects.GraphColumn;
import drawableObjects.Junction;
import drawableObjects.Label;
import drawableObjects.Line;
import drawableObjects.Rectangle;
import drawableObjects.RectangleComparator;
import drawableObjects.Rectangle_Weighted;

import backgroundTasks.UpdateAnimation;


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
	private List<Line> spliceLines;
	private List<ConstitutiveLine_Weighted> weightedConsitutiveLines;
	private List<ConstitutiveLine_Unweighted> unweightedConsitutiveLines;
	private List<Junction> junctionList;
	private List<GraphColumn> isformSpecificDensityPlot;
	private List<Rectangle> uneweightedIsoforms,referenceIsoforms;
	private List<Rectangle_Weighted> weightedIsoforms;
	private List<MRNA> currentlyViewingIsoforms;
	private List<ErrorBar> errorBars;
	private List<Label> unweightedIsoformLabels,referenceLabels;
	private ArrayList<Integer> constitutiveUnscaledPositions,userDefinedConstitutiveUnscaledPositions;
	private HashMap<Integer,GraphColumn>scaledShortReadsPlot;
	private int yScale;
	private int graphYStart,graphXEnd,graphXStart,graphYEnd,shortReadPlotYStart;
	//Data Specific to GENE/BAM
	private int absoluteStartOfGene;
	private int absoluteLengthOfGene;
	private int absoluteEndOfGene;
	private boolean strandOfGene;
	private ArrayList<SAMRecord> geneSAMRecords; //Different per gene
	private Thread currentAnimation;
	private HashMap<String,MRNA> hashOfIsoforms;
	
	
	/**
	 * Instantiates a new processing applet.
	 * 
	 * @param iWidth the width of the applet
	 * @param iHeight the height of the applet
	 */
	public ProcessingApplet(int iWidth, int iHeight){
		width=iWidth;
		height=iHeight;
		isCodingStrand=true;
		
		hashOfIsoforms=new HashMap<String, MRNA>();
		unweightedIsoformsVisible = false;
		shortReadsPlotVisible=false;
		splicingLinesVisible=false;
		weightedIsoformsVisible=false;
		
		currentlyViewingIsoforms= Collections.synchronizedList(new ArrayList<MRNA>());
		scaledShortReadsPlot = new HashMap<Integer, GraphColumn>();
		weightedIsoforms= Collections.synchronizedList(new ArrayList<Rectangle_Weighted>());
		weightedConsitutiveLines=Collections.synchronizedList(new ArrayList<ConstitutiveLine_Weighted>());
		referenceIsoforms = Collections.synchronizedList(new ArrayList<Rectangle>());
		referenceLabels=Collections.synchronizedList(new ArrayList<Label>());
		errorBars = Collections.synchronizedList(new ArrayList<ErrorBar>());
		//----
		junctionList=Collections.synchronizedList(new ArrayList<Junction>());
		uneweightedIsoforms=Collections.synchronizedList(new ArrayList<Rectangle>());
		unweightedConsitutiveLines=Collections.synchronizedList(new ArrayList<ConstitutiveLine_Unweighted>());
		unweightedIsoformLabels =Collections.synchronizedList(new ArrayList<Label>());
		spliceLines=Collections.synchronizedList(new ArrayList<Line>());
		
		geneSAMRecords=null;
		
		isformSpecificDensityPlot=null;
		graphYStart = 500;
		graphYEnd = 80;
		graphXStart = 200;
		graphXEnd = 900;
		shortReadPlotYStart =600;
	}
	
	/* (non-Javadoc)
	 * @see processing.core.PApplet#setup()
	 */
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
	
	/* (non-Javadoc)
	 * @see processing.core.PApplet#draw()
	 */
	public synchronized void draw(){
		background(255);
//		line(0, 0, this.mouseX, this.mouseY);
//		line(width, 0, this.mouseX, this.mouseY);
		
		drawSplicingLines();
		drawShortReads();
		drawUnweightedIsoforms();
		drawWeightedIsoforms();
		
		
	}
	
	/**
	 * Sets the new size of the PApplet
	 * 
	 * @param iWidth the new width of the PApplet
	 * @param iHeight the new height of the PApplet
	 */
	public void setNewSize(int iWidth, int iHeight){
		//width=iWidth;
		//height=iHeight;
		graphYStart = (int) (((double)5/8) *iHeight);
		graphYEnd = (int) (((double)1/10)*iHeight);
		graphXStart = 200;//(int) (((double)2/10)*iWidth);
		graphXEnd = (int) (((double)9/10)*iWidth);
		shortReadPlotYStart =(int) (((double)6/8)*iHeight);
		//int titleBarHeight = getBounds().y;
	    //setSize(iWidth, iHeight-titleBarHeight);
	    
	}
	
	/* (non-Javadoc)
	 * @see processing.core.PApplet#mousePressed()
	 * 
	 * When the right mouse button is pressed over a unweightedIsoform, it is removed from the
	 * list currentlyViewingIsoforms and constitutive sites are recalculated
	 */
	public void mousePressed(){
		if(mouseButton==RIGHT){
			//remove a mrna
			//reload with new list
			if(unweightedIsoformsVisible){
				int indexToRemove = (mouseY+5-graphYEnd)/30;
				if(indexToRemove>=0&&indexToRemove<currentlyViewingIsoforms.size()){
					currentlyViewingIsoforms.remove(indexToRemove);
					loadArrayOfUnweightedIsoforms(currentlyViewingIsoforms);
					
				}
				if(constitutiveUnscaledPositions!=null){
					userDefinedConstitutiveUnscaledPositions=constitutiveUnscaledPositions;
				}
			}				
		}
	}
	
	/**
	 * Flip. Flips the isoform so that the user can see the alternate strand
	 */
	public synchronized void flip() {
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
			for(Rectangle_Weighted rectangle:weightedIsoforms){
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
		if(unweightedConsitutiveLines!=null){
			for(ConstitutiveLine_Unweighted line:unweightedConsitutiveLines){	
				line.setXCoord(reverse(line.getXCoord()));
			}
		}
		if(weightedConsitutiveLines!=null){
			for(ConstitutiveLine_Weighted line:weightedConsitutiveLines){	
				line.setXCoord(reverse(line.getXCoord()));
			}
		}
		if(junctionList!=null){
			for(Junction junction:junctionList){
				junction.setLeftScaled(reverse(junction.getLeftScaled()));
				junction.setRightScaled(reverse(junction.getRightScaled()));
			}
		}
		
	}
	
	/**
	 * Sets the short reads visible or invisible
	 * 
	 * @param bool the boolean (true means the short reads are visible)
	 */
	public void setShortReadsVisible(boolean bool) {
		shortReadsPlotVisible=bool;	
	}
	
	/**
	 * Sets the unweighted isoforms visible or invisible
	 * 
	 * @param bool the boolean (true means that the unweighted isoforms are visible)
	 */
	public void setUnweightedIsoformsVisible(boolean bool) {
		unweightedIsoformsVisible=bool;
	}
	/**
	 * Sets the splice lines visible or invisible
	 * 
	 * @param bool the boolean (true means that the splicelines are visible)
	 */
	public void setSpliceLinesVisible(boolean bool) {
		splicingLinesVisible=bool;
	}
	/**
	 * Sets the weighted isoforms visible or invisible
	 * 
	 * @param bool the boolean (true means that the weighted isoforms are visible)
	 */
	public void setWeightedIsoformsVisible(boolean bool){
		weightedIsoformsVisible=bool;
	}
	
	/**
	 * Load array of unweighted isoforms without changing the parameters
	 * 
	 * @param listOfMRNA a List of MRNA objects
	 */
	public void loadArrayOfUnweightedIsoforms(List<MRNA> listOfMRNA){
		//TODO add checks to make sure that loadNewArray has been called
		loadNewArrayOfUnweightedIsoforms(listOfMRNA, absoluteStartOfGene, absoluteLengthOfGene, isCodingStrand);
	}
	/**
	 * Load array of weighted isoforms without changing the parameters
	 * 
	 * @param listOfMRNA a list of MRNA objects
	 */
	public void loadArrayOfWeightedIsoforms(List<MRNA> listOfMRNA) {
		//TODO add checks to make sure that loadNewArray has been called
		loadNewArrayOfWeightedIsoforms(listOfMRNA, absoluteStartOfGene, absoluteLengthOfGene, isCodingStrand);
	}
	/**
	 * Loads information for positioning unweighted isoforms into
	 * global arrays where they will be drawn through the draw function.
	 * 
	 * @param iAbsoluteStartOfGene the genomic coordinate of the gene (inclusive)
	 * @param iAbsoluteLengthOfGene the genomic length of the gene (inclusive)
	 * @param strand the direction of the gene (false means non coding)
	 * @param isoforms a collection of MRNA
	 */
	public synchronized void loadNewArrayOfUnweightedIsoforms(Collection<MRNA> isoforms,int iAbsoluteStartOfGene,int iAbsoluteLengthOfGene,boolean strand) {
		absoluteStartOfGene=iAbsoluteStartOfGene;
		absoluteLengthOfGene=iAbsoluteLengthOfGene;
		absoluteEndOfGene=iAbsoluteStartOfGene+iAbsoluteLengthOfGene-1;
		isCodingStrand = strand;
		
		//Make new data arrays that hold the items that will be drawn
		uneweightedIsoforms.clear(); //= Collections.synchronizedList(new ArrayList<Rectangle>());
		spliceLines.clear(); //= Collections.synchronizedList(new ArrayList<Line>());
		unweightedIsoformLabels.clear(); // = Collections.synchronizedList(new ArrayList<Label>());
		unweightedConsitutiveLines.clear(); //=Collections.synchronizedList(new ArrayList<ConstitutiveLine_Unweighted>());
		
		ArrayList<MRNA> newListOfMRNA = new ArrayList<MRNA>();
		
		//Identify the genomic coordinates which are constitutive for the all the isoforms
		constitutiveUnscaledPositions=identifyConstituitiveBases(isoforms);
		userDefinedConstitutiveUnscaledPositions=constitutiveUnscaledPositions;
		
		
		
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
						scaledStart = scaleAbsoluteCoord(exons.getStart());
					}else{
						scaledStart = scaleAbsoluteCoord(exons.getEnd());
					}

					//(Y position is +5) because the yPosition is relative to where the corner of the CDS are supposed
					//to start. The exons are 10 pixels tall so that means that the corner of the exon is 5 pixels lower
					Rectangle temp = new Rectangle(scaledStart, yPosition+5, scaledLength, 
							10,exons.getStart(),exons.getEnd(),isoform.getId(),color);
					
					uneweightedIsoforms.add(temp);
					sortedRectangles.add(temp);
					
					float scaledPosition;
					for(Integer i :constitutiveUnscaledPositions){
						if(i>=exons.getStart()&&i<=exons.getEnd()){
							scaledPosition=scaleAbsoluteCoord(i);
							//(Y position is +10) because the yPosition is relative to where the corner of the CDS are supposed
							//to start, the middle of 20, is 10
							unweightedConsitutiveLines.add(new ConstitutiveLine_Unweighted(scaledPosition,yPosition+10,10));	
						}
					}					
				}
				color = color(150);
				for(CDS cds :isoform.getCDS().values()){
					float scaledLength= map(cds.getLength(),0,absoluteLengthOfGene,0,graphXEnd-graphXStart);
					float scaledStart;
					if(isCodingStrand){
						scaledStart = scaleAbsoluteCoord(cds.getStart());
					}else{
						scaledStart = scaleAbsoluteCoord(cds.getEnd());
					}
					Rectangle temp = new Rectangle(scaledStart, yPosition, scaledLength, 20,cds.getStart(),cds.getEnd(),isoform.getId(),color);
					uneweightedIsoforms.add(temp);
					//sortedRectangles.add(temp);
					
					float scaledPosition;
					for(Integer i :constitutiveUnscaledPositions){
						if(i>=cds.getStart()&&i<=cds.getEnd()){
							if(isCodingStrand){
								scaledPosition = map(i,absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd);
							}else{
								scaledPosition = reverse(map(i,absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd));
							}
							//(Y position is +10) because the yPosition is relative to where the corner of the CDS are supposed
							//to start, the middle of 20, is 10
							unweightedConsitutiveLines.add(new ConstitutiveLine_Unweighted(scaledPosition,yPosition+10,20));	
						}
					}
				}

				color = color(0,255,0, 100);//Green
				if(isoform.getStartCodon()!=null){
					float scaledLength=map(isoform.getStartCodon().getLength(),0,absoluteLengthOfGene,
							0,graphXEnd-graphXStart);
					float scaledStart;
					if(isCodingStrand){
						scaledStart =  map(isoform.getStartCodon().getStart(),
									absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd);
					}else{
						scaledStart = reverse(map(isoform.getStartCodon().getEnd(),
								absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd));
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
						
					}else{
						startScaled = reverse(map(isoform.getStopCodon().getEnd(),
								absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd));
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
				//FIXME -- It looks ugly to connect the corners
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
	
	/**
	 * Loads information for positioning Weighted isoforms into 
	 * global arrays. These weighted isoforms will be drawn when the draw function is called.
	 * 
	 * @param listOfMRNA is a list of MRNA
	 * @param iAbsoluteStartOfGene is the genomic coodinate of the start of the gene (inclusive)
	 * @param iAbsoluteLengthOfGene is the genomic length of the gene (inclusive)
	 * @param strand is the strand of the gene (false means noncoding)
	 */
	public synchronized void loadNewArrayOfWeightedIsoforms(List<MRNA> listOfMRNA,int iAbsoluteStartOfGene,int iAbsoluteLengthOfGene,boolean strand){
		absoluteStartOfGene=iAbsoluteStartOfGene;
		absoluteLengthOfGene=iAbsoluteLengthOfGene;
		absoluteEndOfGene=iAbsoluteStartOfGene+iAbsoluteLengthOfGene-1;
		isCodingStrand = strand;
		
		//Under the condition that the loadShortReads function has already been called
		if(geneSAMRecords!=null){
			//Make new arrays
			weightedIsoforms.clear(); 
			referenceIsoforms.clear();
			referenceLabels.clear();
			errorBars.clear();
			weightedConsitutiveLines.clear();
			junctionList.clear();
			
			//This is to allow the program to highlight what the user defined as constitutive regions
			if(listOfMRNA.size()>1||userDefinedConstitutiveUnscaledPositions==null){
				constitutiveUnscaledPositions=identifyConstituitiveBases(listOfMRNA);		
			}else{
				constitutiveUnscaledPositions=userDefinedConstitutiveUnscaledPositions;
			}
			
			int yRefStart=30; // Start of where the reference labels go
			//For each isoform
			for(MRNA mrna:listOfMRNA){
				hashOfIsoforms.put(mrna.getId(),mrna);
				int cdsColor = color(150);
				int exonColor = color(40);
				if(listOfMRNA.size()>1){	
					colorMode(HSB, 450,100,50);
					cdsColor = color(yRefStart,100,100);
					exonColor = color(yRefStart,100,100);
					colorMode(RGB,255);
				}else{
					loadJunctionList(mrna);
				}
				
				ArrayList<ShortRead>compatibleShortReads = Statistics.getCompatibleShortReads(mrna, geneSAMRecords);
				
				referenceLabels.add(new Label(mrna.getId(), 10, yRefStart+10));
				
				//For each exon
				int exonCountPosition=0;
				for(Exon exon :mrna.getExons().values()){
					//Find the average of the region in mention
					
					boolean endExon = false;
					if(exonCountPosition==0 || exonCountPosition==mrna.getExons().values().size()-1){
						endExon=true;
					}
					float average = Statistics.getWeight(exon,compatibleShortReads,endExon);
					exonCountPosition++;
					
					//Scale the length and start
					float lengthScaled=map(exon.getLength(),0,absoluteLengthOfGene,0,graphXEnd-graphXStart);
					float startScaled;
					if(isCodingStrand){
						startScaled = scaleAbsoluteCoord(exon.getStart()); 
					}else{
						startScaled = scaleAbsoluteCoord(exon.getEnd());
					}
					
					Rectangle_Weighted temp = new Rectangle_Weighted(startScaled, lengthScaled,10,
							exon.getStart(),exon.getEnd(),mrna.getId(),average,exonColor);
					weightedIsoforms.add(temp);
					errorBars.add(new ErrorBar(average,
							Statistics.getStandardDeviation_ReadsPerBase(exon.getStart(),exon.getEnd(),compatibleShortReads),
							temp,mrna.getId(),(startScaled+lengthScaled/2)));
					
					//Fill in array that draws the reference isoforms
					Rectangle temp2 = new Rectangle(startScaled, yRefStart, lengthScaled,10,exon.getStart(),
							exon.getEnd(),mrna.getId(),exonColor);
					referenceIsoforms.add(temp2);
					

					//Fill in array that draws the scaled constitutive lines
					//The difference here is that that the first YStartPosition represents the absolute height
					//It still needs to be scaled (look at drawWeightedConstitutiveLines)
					float scaledPosition;
					for(Integer i :constitutiveUnscaledPositions){
						if(i>=exon.getStart()&&i<=exon.getEnd()){
							if(isCodingStrand){
								scaledPosition = map(i,absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd);
							}else{
								scaledPosition = reverse(map(i,absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd));
							}
							weightedConsitutiveLines.add(new ConstitutiveLine_Weighted(scaledPosition, 10, average));	
						}
					}
				}
				//For each CDS
				for(CDS cds:mrna.getCDS().values()){
					float lengthScaled=map(cds.getLength(),0,absoluteLengthOfGene,0,graphXEnd-graphXStart);
					float startScaled;
					if(isCodingStrand){
						startScaled = scaleAbsoluteCoord(cds.getStart());
					}else{
						startScaled = scaleAbsoluteCoord(cds.getEnd());
					}
					//Get the rectangle that is nearest to the xCoordinate who has the same MRNA
					//to get the appropriate height
					Rectangle_Weighted exon_Rect=getRectangleNearestToXCoord(weightedIsoforms,mrna.getId(),startScaled);
					float weight = exon_Rect.getWeight();
					Rectangle_Weighted temp = new Rectangle_Weighted(startScaled,lengthScaled,20,
							cds.getStart(),cds.getEnd(),mrna.getId(),weight,cdsColor);
					weightedIsoforms.add(temp);
					Rectangle temp2 = new Rectangle(startScaled, yRefStart-5, lengthScaled,20,
							cds.getStart(),cds.getEnd(),mrna.getId(),cdsColor);
					referenceIsoforms.add(temp2);
					
//					//Fill in array that draws the scaled constitutive lines
//					//The difference here is that that the first YStartPosition represents the absolute height
//					//It still needs to be scaled (look at drawWeightedConstitutiveLines)
					float scaledPosition;
					for(Integer i :constitutiveUnscaledPositions){
						if(i>=cds.getStart()&&i<=cds.getEnd()){
							scaledPosition=scaleAbsoluteCoord(i);
							weightedConsitutiveLines.add(new ConstitutiveLine_Weighted(scaledPosition,20,weight));	
						}
					}
				}
				//FIXME Start and Stop codons are not drawn! This may pose an issue in the future
				yRefStart+=20;
			}
		}else{
			System.err.println("A call was made to loadWeightedIsoforms but there was no shortReadData");
		}
	}
	
	/**
	 * Load short reads.
	 * 
	 * @param iShortReads the i short reads
	 * @param iTotalNumberOfReads the i total number of reads
	 */
	public synchronized void loadShortReads(ArrayList<SAMRecord> iShortReads){
		geneSAMRecords = iShortReads;
		fillScaledShortReadsPlot();
	}
	
	/**
	 * Load junction list.
	 * 
	 * @param isoform the isoform you want to have junction reads calculated for
	 */
	public void loadJunctionList(MRNA isoform){	
		for(ShortRead shortRead:Statistics.getCompatibleShortReads(isoform, geneSAMRecords)){
			if(shortRead.isJunctionRead()){
				float startScaled = scaleAbsoluteCoord(shortRead.getFirstExonEnd());
				float endScaled = scaleAbsoluteCoord(shortRead.getLastExonBeginning());				
				
				boolean found =false;
				for(Junction junction:junctionList){
					if(junction.getLeftScaled()==startScaled && junction.getRightScaled()==endScaled){
						junction.incrementWeight(1);
						found=true;
					}					
				}
				if(!found){
					junctionList.add(new Junction(startScaled,endScaled,1));
				}		
			}	
		} 
	}
	public void loadWeightsOfCurrentlyShowingIsoforms(){
		if(currentlyViewingIsoforms!=null){
			loadNewArrayOfWeightedIsoforms(currentlyViewingIsoforms,absoluteStartOfGene,absoluteLengthOfGene,strandOfGene);
		}
	}
	public void loadUnweightedOFCurrentlyShowingIsoforms(){
		if(currentlyViewingIsoforms!=null){
			loadNewArrayOfUnweightedIsoforms(currentlyViewingIsoforms,absoluteStartOfGene,absoluteLengthOfGene,strandOfGene);
		}
	}
	
	/**
	 * Adjusts the Y Scale
	 * 
	 * @param iYScale the new y scale value (default is 5)
	 */
	public void setYScale(int iYScale){
		yScale=iYScale;
	}
	//This is how you will do zooming
	private void drawWindow(int startCoord, int endCoord){
		
	}
	private void fillInIsoformSpecificDensityPlot(ArrayList<SAMRecord> iShortReads,MRNA iMRNA) {
		isformSpecificDensityPlot= Collections.synchronizedList(new ArrayList<GraphColumn>());
			
	}
	private synchronized void fillScaledShortReadsPlot(){
		scaledShortReadsPlot.clear();
		
		HashMap<Integer, Integer> absoluteDensityMap = Statistics.getDensityMap(absoluteStartOfGene, absoluteEndOfGene, geneSAMRecords);
		
		//Make a line for every Pixel
		for(int i =graphXStart;i<=graphXEnd;i++){
			if(isCodingStrand){
				scaledShortReadsPlot.put(i,(new GraphColumn(i, 0, 0, 0)));	
			}else{
				scaledShortReadsPlot.put(i, (new GraphColumn(reverse(i), 0, 0, 0)));
			}	
		}
		int prevPixel = -1;
		int currentSum=-1;
		int numberOfShortReads=-1;
		int frameAbsoluteStart=-1;
		int frameAbsoluteEnd = -1;
		
		//Goes through each of the genomic coordinates and attempts to map it to a pixel
		//In the end, a pixel represents the average number of short reads that cross it
		int maxAverage =0;
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
				int average = (int)((double)currentSum/numberOfShortReads);
				if(average>maxAverage){
					maxAverage=average;
				}
				scaledShortReadsPlot.get(prevPixel).incrementHeight(average);
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
		
		for(GraphColumn column:scaledShortReadsPlot.values()){
			column.setScaledHeight((int) map(column.getUnscaledHeight(),0,maxAverage,0,80));
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
	
	/**
	 * Draws unweighted isoforms starting a graphYStart at every 30 pixels
	 */
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
	
	/**
	 * Draw the reference isoforms and the labels
	 */
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
	
	/**
	 * Draw the labels for the reference isoforms.
	 */
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
	
	/**
	 * Draw the weighted isoforms
	 *  
	 * This function iterates through the list weightedIsoforms to draw them at the correct height.
	 * It also draws other components such as the grid, errorbars, reference isoforms, and hover 
	 * information
	 */
	private void drawWeightedIsoforms() {
		if(weightedIsoforms!=null && weightedIsoformsVisible){
			drawGrid();
			synchronized (weightedIsoforms) {
				
				for(Rectangle_Weighted rectangle:weightedIsoforms){
					
					fill(rectangle.getColor());
					float newScaledYCoord = graphYStart-(yScale*rectangle.getWeight()); 
						//graphYStart-((graphYStart-rectangle.getScaledYCoord())*yScale);
					//Because the rectangle is drawn from the corner, subtract half its height;
					newScaledYCoord=newScaledYCoord-(rectangle.getScaledHeight()/2);
					colorMode(HSB, 450,100,50);
					rect(rectangle.getScaledXCoord(),newScaledYCoord,rectangle.getScaledLength(),rectangle.getScaledHeight());
					colorMode(RGB,255);
				}
				
			}
			fill(0);
			drawErrorBars();
			drawReferenceIsoform();
			drawWeightedConstitutiveLines();
			drawJunctionLines();
			drawLabelForHeight();
			drawHoverInfo();
		}
		
	}
	private void drawJunctionLines() {
		if(junctionList!=null){
			synchronized (junctionList) {
				stroke(255,0,0);
				strokeWeight(3);
				for(Junction junction:junctionList){
					int yPos=graphYStart;
//					for(int i=0;i<junction.getHits();i++){
//						line(junction.getLeftScaled(),yPos,junction.getRightScaled(),yPos);
//						yPos-=yScale;
//					}
					int height = (int) (yPos-(yScale*junction.getWeight()));
					line(junction.getLeftScaled(),height,junction.getRightScaled(),height);
					text(junction.getWeight(),junction.getRightScaled(),height);
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
				stroke(0,100);
				for(GraphColumn column:scaledShortReadsPlot.values()){
					line(column.getScaledX(),shortReadPlotYStart,column.getScaledX(),shortReadPlotYStart-column.getScaledHeight());
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
	 * Draws a grid for the weighted Isoforms
	 */
	private void drawGrid() {
		
		line(graphXStart,graphYStart,graphXEnd,graphYStart);
		line(graphXStart,graphYStart,graphXStart,50);
		int count =0;
		stroke(150);
		for(int i =graphYStart;i>graphYEnd; i-=(yScale)){
			if(count%5==0){
				text(""+count,graphXStart-20,i+5);
				line(graphXStart,i,graphXEnd,i);
			}
			count++;	
		}
		stroke(0);
	}
	
	private void drawErrorBars(){
			if(errorBars!=null){
				synchronized (errorBars) {
					for(ErrorBar error:errorBars){
						double SD = error.getStandardDeviation()*yScale;
						float newScaledYPosition = graphYStart-(yScale*error.getWeight());
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
			if(scaledShortReadsPlot.get(pixelPosition)==null){
				System.err.println("A request for short read density was made but the pixel was not found" +
						"\nYou may have to call loadShortReads again if the dimensions have changed");
				return 0;
			}
			if(pixelPosition == scaledShortReadsPlot.get(pixelPosition).getScaledX()){
				return scaledShortReadsPlot.get(pixelPosition).getUnscaledHeight();
			}else{
				return scaledShortReadsPlot.get((int)reverse(pixelPosition)).getUnscaledHeight();
			}
		}else{
			System.err.println("An attempt was made to fetch the height of coordinate but scaledDensityPlot was null" +
					"\nAre you sure that the loadShortReads Function was called?");
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
	private Rectangle_Weighted getRectangleNearestToXCoord(List<Rectangle_Weighted> listOfRectangles,String mrnaID ,float iXCoord){
		//Gets the rectangle closest to the specified xCoord by cycling through;
		Rectangle_Weighted closest = null;
		float howFar =99999;
		for(Rectangle_Weighted rect:listOfRectangles){
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
		if(weightedIsoformsVisible && weightedIsoforms!=null){
			Rectangle_Weighted rect =  getRectUnderMouse();
			if(rect!=null){
				fill(200,200);
				rect(mouseX,mouseY-20,250,120);
				fill(0);
				MRNA isoform = hashOfIsoforms.get(rect.getIsoformID());
				text("Isoform:" + rect.getIsoformID()+
					"\nStart:\t"+rect.getAbsoluteStart() + 
					"\nEnd: \t " + rect.getAbsoluteEnd()+
					"\nLength:\t" + rect.getAbsoluteLength() +
					"\n#Body Reads:\t" +  Statistics.getNumberOfBodyReads(isoform, rect.getAbsoluteStart(), rect.getAbsoluteEnd(), geneSAMRecords) + 
					"\n#Junction Reads:\t" + Statistics.getNumberOfJunctionReads(isoform, rect.getAbsoluteStart(), rect.getAbsoluteEnd(), geneSAMRecords)
					,mouseX+10,mouseY);
					
			}
			
		}
		
	}
	private float reverse(float position){
		return (graphXEnd-position)+graphXStart;
	}
	private void drawUnweightedConstitutiveLines(){
		if(unweightedConsitutiveLines!=null){
			synchronized (unweightedConsitutiveLines) {
				stroke(0,0,255);
				for(ConstitutiveLine_Unweighted line:unweightedConsitutiveLines){	
					line(line.getXCoord(),line.getYCoord()-(line.getHeight()/2),line.getXCoord(),line.getYCoord()+(line.getHeight()/2)+1);	
				}
				stroke(0);
			}
		}
	}
	private void drawWeightedConstitutiveLines(){
		if(weightedConsitutiveLines!=null){
			synchronized (weightedConsitutiveLines) {
				stroke(0,0,255);
				for(ConstitutiveLine_Weighted line:weightedConsitutiveLines){
					float newYPosition = graphYStart-(yScale*line.getWeight());
					line(line.getXCoord(),newYPosition-(line.getVerticalLength()/2),line.getXCoord(),newYPosition+(line.getVerticalLength()/2)+1);	
				}
				stroke(0);	
				
			}
		}
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
	private ArrayList<Integer> identifyConstituitiveBases(Collection<MRNA> mrnaList){
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
		ArrayList<Integer> newConstitutiveUnscaledPositions = new ArrayList<Integer>();
		if(mrnaList.size()>1){
			for(int i = absoluteStartOfGene;i<=absoluteEndOfGene;i++){
				if(allPositions.get(i)==mrnaList.size()){
					newConstitutiveUnscaledPositions.add(i);
				}
			}	
		}
		return newConstitutiveUnscaledPositions;
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
	
	/**
	 * Clears all the constitutive arrays
	 */
	public synchronized void clearConstitutive() {
		constitutiveUnscaledPositions.clear();
		userDefinedConstitutiveUnscaledPositions.clear();
		unweightedConsitutiveLines.clear();
		weightedConsitutiveLines.clear();
	}
	/**
	 * Loads Short Read data but also animates the exons so that they float in the direction of the 
	 * new short reads
	 * 
	 * @param listOfSamRecords a list of SAMRecord objects
	 * @param isoform the isoform you want to see animated
	 * @param iTotalNumberOfReads the total number of reads in the SAMFile
	 */
	public synchronized void animatedLoadShortReads(ArrayList<SAMRecord> newShortReads,MRNA isoform) {
		junctionList.clear();
		errorBars.clear();
		weightedConsitutiveLines.clear();
		loadShortReads(newShortReads);
		ArrayList<ShortRead> compatibleShortReads = Statistics.getCompatibleShortReads(isoform, newShortReads);
		ArrayList<Rectangle_Weighted> exonsOnly= new ArrayList<Rectangle_Weighted>();
		synchronized (weightedIsoforms) {
			for(Rectangle_Weighted rect:weightedIsoforms){
				if(rect.getScaledHeight()==10){
					exonsOnly.add(rect);
				}
			}
			weightedIsoforms=exonsOnly;
		}
		
		if(currentAnimation!=null && currentAnimation.isAlive()){
			currentAnimation.interrupt();
		}else{
			currentAnimation = new Thread(new UpdateAnimation(this,isoform,compatibleShortReads,weightedIsoforms));
			currentAnimation.start();	
		}	
	}
	/**
	 * Gets the WeightedRectangle underneath the mouse.
	 * 
	 * Iterates through the array of weightedIsoforms and checks to see if the mouse is over it.
	 * If so, it returns the rectangle.
	 * 
	 * @return a Rectangle_Weighted that is underneath the mouse coordinates. If there is none, return is null 
	 */
	private Rectangle_Weighted getRectUnderMouse(){
		synchronized (weightedIsoforms) {
			for(Rectangle_Weighted rect:weightedIsoforms){
				if(	mouseX>rect.getScaledXCoord() &&
					mouseX<rect.getScaledXCoord()+rect.getScaledLength() &&
					mouseY>(graphYStart-rect.getWeight()*yScale-rect.getScaledHeight()/2) && 
					mouseY<(graphYStart-rect.getWeight()*yScale+rect.getScaledHeight()/2) &&
					rect.getScaledHeight()==10){
					return rect;
				}
			}	
		}
		return null;
		
	}

	public void normalize(int totalNumberOfReads,int method) {
		
//		for(GraphColumn column: scaledShortReadsPlot){
//			column.
//		}
		// TODO Auto-generated method stub
		Statistics.setTotalNumberOfReads(totalNumberOfReads);
		Statistics.setMethod(method);
		System.out.println("Method: " + method);
		
		
	}
}
