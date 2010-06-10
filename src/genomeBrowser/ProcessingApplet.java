package genomeBrowser;

import gffParser.CDS;
import gffParser.Exon;
import gffParser.Gene;
import gffParser.MRNA;

import java.awt.font.NumericShaper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

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
	private boolean shortReadsVisible;
	private boolean geneVisible,tightIsoformsVisible, isCodingStrand,weightedIsoformVisible,errorBarsVisible;
	private TreeSet<Integer> allScaledEndCoords;
	private boolean splicingLinesVisible;
	private List<Line> linesToDraw;
	private List<GraphColumn> scaledDensityPlot;
	private java.util.List<Rectangle> rectanglesToDraw,weightedRectangles;
	private java.util.List<ErrorBars> errorBars;
	private HashMap<Integer, Integer> absoluteDensityMap;
	private int yScale;
	private int graphYStart;
	
	
	
	public ProcessingApplet(int iWidth, int iHeight){
		width=iWidth;
		height=iHeight;
		geneVisible = false;
		tightIsoformsVisible = false;
		shortReadsVisible=false;
		weightedIsoformVisible=false;
		isCodingStrand=true;
		splicingLinesVisible=false;
		linesToDraw=null;
		scaledDensityPlot=null;
		rectanglesToDraw=null;
		graphYStart = 500;
		
	
	}
	public void setup(){
		//Subtract the title bar height
		int titleBarHeight = getBounds().y;
	    size(width, height-titleBarHeight,P2D);
	    PFont font = loadFont("AndaleMono-20.vlw");
	    textFont(font);
	    //frameRate(30);
	    smooth();
	    yScale=10;
	    
	}
	public void draw(){
		background(255);
		stroke(0);
//		line(0, 0, this.mouseX, this.mouseY);
//		line(width, 0, this.mouseX, this.mouseY);
		
		drawSplicingLines();
		drawTightIsoforms();
		drawGene();
		drawShortReads();
		drawWeightedIsoforms();
		drawErrorBars();
		
	}
	public void setNewSize(int iHeight, int iWidth){
		width=iWidth;
		height=iHeight;
		int titleBarHeight = getBounds().y;
	    size(width, height-titleBarHeight);
	}
	
	/**
	 * Disable introns.
	 * 
	 * @param boolean - true if you want to disable introns
	 */
	public void disableIntrons(boolean b) {
		//Fill a TreeMap Once with all the pieces.
		//Then when you are drawing,subtract until you reach the largest piece...
//		if(b){
//			isoformsVisible=false;
//			tightIsoformsVisible=true;
//			allScaledEndCoords = new TreeSet<Integer>();
//			for(MRNA mrna:isoforms){
//				for(CDS cds:mrna.getCDS().values()){
//					if(mrna.getStrand()){
//						allScaledEndCoords.add((int) map(cds.getEnd()-startOfGene,0,lengthOfGene,0,800));	
//					}else{
//						allScaledEndCoords.add((int) map(cds.getEnd()-startOfGene,0,lengthOfGene,0,800));
//					}
//				}
//				for(Exon exon:mrna.getExon().values()){
//					allScaledEndCoords.add((int) map(exon.getEnd()-startOfGene,0,lengthOfGene,0,800));	
//				}
//				allScaledEndCoords.add((int) map(mrna.getStartCodon().getEnd()-startOfGene,0,lengthOfGene,0,800));
//				allScaledEndCoords.add((int) map(mrna.getStopCodon().getEnd()-startOfGene,0,lengthOfGene,0,800));	
//			}
//		}else{
//			isoformsVisible=true;
//			tightIsoformsVisible=false;
//		}
	}
	public void flip() {
		isCodingStrand=!isCodingStrand;
		if(rectanglesToDraw!=null){
			for(Rectangle rectangle:rectanglesToDraw){
				rectangle.setScaledXCoord(800-(rectangle.getScaledXCoord()+rectangle.getScaledLength()));
			}	
		}
		if(linesToDraw!=null){
			for(Line line:linesToDraw){	
				line.setXCoordStart(800-line.getXCoordStart());
				line.setXCoordEnd(800-line.getXCoordEnd());
			}	
		}
		if(scaledDensityPlot!=null){
			for(GraphColumn column:scaledDensityPlot){
				column.setScaledX(800-column.getScaledX());
			}
		}
		if(weightedRectangles!=null){
			for(Rectangle rectangle:weightedRectangles){
				rectangle.setScaledXCoord(800-(rectangle.getScaledXCoord()+rectangle.getScaledLength()));
			}
		}
		if(errorBars!=null){
			for(ErrorBars errorBar:errorBars){
				errorBar.setScaledXCoord(800-errorBar.getScaledXPosition());
			}
		}
		
	}
	public void setShortReadsVisible(boolean selected) {
		shortReadsVisible=selected;	
	}
	public void setWeightedIsoformsVisible(boolean selected) {
		weightedIsoformVisible=selected;	
	}
	public void setGeneVisible(boolean selected) {
		geneVisible=selected;
	}
	public void setSpliceLinesVisible(boolean selected) {
		splicingLinesVisible=selected;
	}
	public void setErrorBarsVisible(boolean selected){
		errorBarsVisible=selected;
	}
	/**
	 * Show new isoform.
	 * 
	 * @param draws the gene you want to see
	 */
	public synchronized void loadGene(Gene gene) {
		Collection<MRNA> isoforms;
		isoforms = gene.getMRNA().values();
		int absoluteLengthOfGene = gene.getLength();
		int absoluteStartOfGene= gene.getStart();
		rectanglesToDraw = Collections.synchronizedList(new ArrayList<Rectangle>());
		linesToDraw = Collections.synchronizedList(new ArrayList<Line>());
		isCodingStrand = gene.getStrand();
		
		
		//The code below fill in rectanglesToDraw with scaled values
		if(isoforms!=null){
			int yPosition = 40;	
			for(MRNA isoform: isoforms){
				
				//Grey
				int color = color(40);//DarkGrey
				//sortedRectangles are used to draw the spliceLines
				ArrayList<Rectangle> sortedRectangles= new ArrayList<Rectangle>();
				
				for(Exon exons :isoform.getExons().values()){
					int scaledLength=(int) map(exons.getLength(),0,absoluteLengthOfGene,0,800);
					int scaledStart;
					
					if(isCodingStrand){
						scaledStart = (int) map(exons.getStart()-absoluteStartOfGene,0,absoluteLengthOfGene,0,800);
					}else{
						scaledStart = 800 - ((int) map(exons.getStart()+exons.getLength()
								-absoluteStartOfGene,0,absoluteLengthOfGene,0,800));
					}
					Rectangle temp = new Rectangle(scaledStart, yPosition+5, scaledLength, 10,exons.getStart(),exons.getEnd(), color);
					rectanglesToDraw.add(temp);
					sortedRectangles.add(temp);					
				}
					
				color = color(150); //Light Grey
				for(CDS cds :isoform.getCDS().values()){
					int scaledLength=(int) map(cds.getLength(),0,absoluteLengthOfGene,0,800);
					int scaledStart;
					if(isCodingStrand){
						scaledStart = (int) map(cds.getStart()-absoluteStartOfGene,0,absoluteLengthOfGene,0,800);
					}else{
						scaledStart = 800 - ((int) map(cds.getStart()+cds.getLength()
								-absoluteStartOfGene,0,absoluteLengthOfGene,0,800));
					}
					Rectangle temp = new Rectangle(scaledStart, yPosition, scaledLength, 20,cds.getStart(),cds.getEnd(), color);
					rectanglesToDraw.add(temp);
					//sortedRectangles.add(temp);					
				}
				
				//****************************************
				//************************
				//******************
				//**********
				//*******
				//FIXME
				Collections.sort(sortedRectangles,new RectangleComparator());
				float scaledLastExonEnd=0;
				float scaledLastExonY=0;
				for(int i =0;i<sortedRectangles.size();i++){
					//System.out.println(sortedRectangles.get(i).getXCoord());
					Rectangle rect = sortedRectangles.get(i);
					if(i!=0){
						if(scaledLastExonEnd>rect.getScaledXCoord()){
							//SKIP (There is an overlap)
						}else{
							float midPoint = (scaledLastExonEnd+rect.getScaledXCoord())/2;
							linesToDraw.add(new Line(scaledLastExonEnd,scaledLastExonY,midPoint,yPosition-10));	
							linesToDraw.add(new Line(midPoint,yPosition-10,rect.getScaledXCoord(),rect.getScaledYCoord()));
						}
					}
//					if(lastExonEnd<rect.getXCoord()+rect.getLength()){
						scaledLastExonEnd = rect.getScaledXCoord()+rect.getScaledLength();
						scaledLastExonY=rect.getScaledYCoord();
//					}
				}
				//************************
				//****************************************

				color = color(0,255,0, 100);//Green
				if(isoform.getStartCodon()!=null){
					int scaledLength=(int) map(isoform.getStartCodon().getLength(),0,absoluteLengthOfGene,0,800);
					int scaledStart;
					if(isCodingStrand){
						scaledStart = (int) map(isoform.getStartCodon().getStart()
								-absoluteStartOfGene,0,absoluteLengthOfGene,0,800);
					}else{
						scaledStart = 800 - ((int) map(isoform.getStartCodon().getStart()+
								isoform.getStartCodon().getLength()-absoluteStartOfGene,0,absoluteLengthOfGene,0,800)+scaledLength);
					}
					rectanglesToDraw.add(new Rectangle(scaledStart, yPosition, scaledLength, 20,isoform.getStartCodon().getStart(),isoform.getStartCodon().getEnd(), color));	
				}
				if(isoform.getStopCodon()!=null){
					int lengthScaled=(int) map(isoform.getStopCodon().getLength(),0,absoluteLengthOfGene,0,800);
					int startScaled;
					if(isCodingStrand){
						startScaled = (int) map(isoform.getStopCodon().getStart()-absoluteStartOfGene,0,absoluteLengthOfGene,0,800);
					}else{
						startScaled = 800 - ((int) map(isoform.getStopCodon().getStart()
								+isoform.getStopCodon().getLength()-absoluteStartOfGene,0,absoluteLengthOfGene,0,800)+lengthScaled);
					}
					rectanglesToDraw.add(new Rectangle(startScaled, yPosition, lengthScaled, 20,isoform.getStopCodon().getStart(),isoform.getStopCodon().getEnd(), color));
				}
				yPosition +=30;
			}
		}
	}
	public synchronized void loadShortReads(ArrayList<SAMRecord> iShortReads,int iShortReadsStart,int iShortReadsEnd){
		
		
		//ABSOLUTE Density Plot HASH
		absoluteDensityMap = new HashMap<Integer, Integer>();
		for(int i = iShortReadsStart;i<=iShortReadsEnd;i++){
			absoluteDensityMap.put(i,0);	
		}
		for(SAMRecord samRecord:iShortReads){
			//** CIGAR PARSING
			List<CigarElement> cigarElements = samRecord.getCigar().getCigarElements();
			int start = samRecord.getAlignmentStart();
			for(CigarElement cigarElement:cigarElements){
				if(cigarElement.getOperator().equals(CigarOperator.M)){
					for(int i = 0;i<=cigarElement.getLength();i++){
						int prev = absoluteDensityMap.get(start);
						prev++;
						absoluteDensityMap.put(start, prev);
						start++;
					}
					//System.out.println("M"+cigarElement.getLength());
				}else if(cigarElement.getOperator().equals(CigarOperator.N)){
					for(int i = 0;i<=cigarElement.getLength();i++){
						start++;
					}
					//System.out.println("N"+cigarElement.getLength());
				}else if(cigarElement.getOperator().equals(CigarOperator.D)){
					for(int i = 0;i<=cigarElement.getLength();i++){
						start++;
					}
					System.out.println("D"+cigarElement.getLength());
				}
					
			}
			
			
			//**
//			for(int i = samRecord.getAlignmentStart();i<=samRecord.getAlignmentEnd(); i++){
//				if(absoluteDensityMap.get(i)==null){
//					System.err.println("THIS SHOULDNT HAPPEN");
//				}else{
//					 int prev = absoluteDensityMap.get(i);
//					 prev++;
//					 absoluteDensityMap.put(i, prev);	 
//				}
//				
//			}
			
		}
		
		//fillDensityPlot(iShortReads,iShortReadsStart,iShortReadsEnd);
		fillDensityPlot(absoluteDensityMap,iShortReadsStart,iShortReadsEnd);
	}
	//	private void fillDensityPlot(ArrayList<SAMRecord> iShortReads,int iShortReadsStart,int iShortReadsEnd) {
	//		//Fills in an array Left to Right (You CAN use the index to indicate the pixel (from the left))
	//		densityPlot = Collections.synchronizedList(new ArrayList<Line>());
	//		//Make a line for every Pixel
	//		for(int i =0;i<800;i++){
	//			if(isCodingStrand){
	//				densityPlot.add(new Line(i, 600, i, 600, color(0)));	
	//			}else{
	//				densityPlot.add(new Line(800-i, 600, 800-i, 600, color(0)));
	//			}	
	//		}
	//		
	//		ArrayList<SAMRecord> shortReads=iShortReads;		
	//		int shortReadsStart = iShortReadsStart;
	//		int shortReadsEnd = iShortReadsEnd;
	//		
	//		for(SAMRecord shortRead :shortReads){
	//			int scaledStart = (int) map(shortRead.getAlignmentStart(),shortReadsStart,shortReadsEnd,0,800);
	//			int scaledEnd = (int) map(shortRead.getAlignmentStart(), shortReadsStart, shortReadsEnd, 0, 800);
	//			for(int i=scaledStart;i<=scaledEnd;i++){
	//				densityPlot.get(i).incrementHeight(5);
	//			}
	//			//line(scaledStart, yPosition, scaledEnd, yPosition);
	//			//yPosition--;
	//		}
	//		strokeWeight(1);
	//		shortReadsVisible=true;
	//		
	//	}
		public synchronized void loadWeightedIsoform(MRNA mrna,int startOfGene,int lengthOfGene) {
			//This is under the condition that you already have shortReadData
			//Find the average number of reads for each exon exon/CDS based on the positions on the graph
			
			weightedRectangles = Collections.synchronizedList(new ArrayList<Rectangle>());
			errorBars = Collections.synchronizedList(new ArrayList<ErrorBars>());
			
			//Grey
			int color = color(40);//DarkGrey
			int yPosition = graphYStart;
			for(Exon exons :mrna.getExons().values()){
				float lengthScaled=map(exons.getLength(),0,lengthOfGene,0,800);
				float startScaled;
				
				if(isCodingStrand){
					startScaled = (int) map(exons.getStart()-startOfGene,0,lengthOfGene,0,800);
				}else{
					startScaled = 800 - ((int) map(exons.getStart()+exons.getLength()
							-startOfGene,0,lengthOfGene,0,800));
				}
				
//				//For the region mentioned average it
				int sum = 0;
				for(Integer heightAtPosition:getArrayOfHeights(exons.getStart(), exons.getEnd())){
					sum+=heightAtPosition;
				}
				
				float average = ((float)sum/exons.getLength());
				
				Rectangle temp = new Rectangle(startScaled, yPosition-average, lengthScaled,10,
						exons.getStart(),exons.getEnd(), color);
				weightedRectangles.add(temp);
				
				//TODO (Check if this is right)
				errorBars.add(new ErrorBars(getArrayOfHeights(exons.getStart(),exons.getEnd()), 
						(startScaled+lengthScaled/2), yPosition-average));
			}
				
			color = color(150); //Light Grey
			for(CDS cds :mrna.getCDS().values()){
				float lengthScaled=map(cds.getLength(),0,lengthOfGene,0,800);
				float startScaled;
				if(isCodingStrand){
					startScaled = map(cds.getStart()-startOfGene,0,lengthOfGene,0,800);
				}else{
					startScaled = 800 - map(cds.getStart()+cds.getLength()
							-startOfGene,0,lengthOfGene,0,800);
				}
				//For the region mentioned average it
				int sum = 0;
				for(Integer heightAtPosition:getArrayOfHeights(cds.getStart(), cds.getEnd())){
					sum+=heightAtPosition;
				}
				float average = ((float)sum/cds.getLength());
				
				Rectangle temp = new Rectangle(startScaled, yPosition-average, lengthScaled,10,cds.getStart(),cds.getEnd(), color);
				weightedRectangles.add(temp);
				errorBars.add(new ErrorBars(getArrayOfHeights(cds.getStart(),cds.getEnd()), 
						(startScaled+lengthScaled/2), yPosition-average));
			}
		}
	private void fillDensityPlot(HashMap<Integer, Integer> iAbsoluteDensity,int iShortReadsStart,int iShortReadsEnd){
		System.out.print(iShortReadsEnd +" " +iShortReadsStart);
		scaledDensityPlot = Collections.synchronizedList(new ArrayList<GraphColumn>());
		//Make a line for every Pixel
		for(int i =0;i<=800;i++){
			if(isCodingStrand){
				scaledDensityPlot.add(new GraphColumn(i, 600, 0, 0, 0));	
			}else{
				scaledDensityPlot.add(new GraphColumn(800-i, 600, 0, 0, 0));
			}	
		}
		int prevPixel = -1;
		int currentSum=-1;
		int numberOfShortReads=-1;
		int frameAbsoluteStart=-1;
		int frameAbsoluteEnd = -1;
		
		for(int i = iShortReadsStart;i<=iShortReadsEnd;i++){
			int mappedPixel = (int) map(i,iShortReadsStart,iShortReadsEnd,0,800);
			//System.out.println(mappedPixel +"---Abs" + iAbsoluteDensity.get(i));
			if(prevPixel==-1){
				frameAbsoluteStart=i;
				frameAbsoluteEnd=i;
				numberOfShortReads=1;
				currentSum=iAbsoluteDensity.get(i);
				prevPixel=mappedPixel;
			}else if(mappedPixel!= prevPixel){
				scaledDensityPlot.get(prevPixel).incrementHeight((int)((double)currentSum/numberOfShortReads));
				scaledDensityPlot.get(prevPixel).setAbsoluteXCoords(frameAbsoluteStart,frameAbsoluteEnd);
				prevPixel=mappedPixel;
				numberOfShortReads=1;
				currentSum=iAbsoluteDensity.get(i);
				frameAbsoluteStart=i;
				frameAbsoluteEnd=i;
			}else if(i==iShortReadsEnd){
				frameAbsoluteEnd=i;
				numberOfShortReads++;
				scaledDensityPlot.get(prevPixel).incrementHeight((int)((double)currentSum/numberOfShortReads));
				scaledDensityPlot.get(prevPixel).setAbsoluteXCoords(frameAbsoluteStart,frameAbsoluteEnd);
			}else{	
				currentSum+=iAbsoluteDensity.get(i);
				frameAbsoluteEnd=i;
				numberOfShortReads++;
			} 
//			System.out.println(i + "----"  + iAbsoluteDensity.get(i));
		}
		strokeWeight(1);
		shortReadsVisible=true;
	}
/**
	 * Draws the splice lines that connect the exons and cds.
	 * Draws only under the condition that splicingLinesVisible is true, geneVisible is true,
	 * and linesToDraw is not null and rectanglesToDraw is not null.
	 */
	private void drawSplicingLines() {
		if(splicingLinesVisible&&geneVisible&&linesToDraw!=null&&rectanglesToDraw!=null){
			for(Line line:linesToDraw){
				line(line.getXCoordStart(),line.getYCoordStart(),line.getXCoordEnd(),line.getYCoordEnd());
			}	
		}
	}
	
	/**
	 * Draws all the isoforms of a gene under the condition that genes are visible and rectanglesToDraw is not null. 
	 * It iterates through the rectanglesToDraw
	 */
	private void drawGene(){
		if(geneVisible&&rectanglesToDraw!=null){
			for(Rectangle rectangle:rectanglesToDraw){
				fill(rectangle.getColor());
				rect(rectangle.getScaledXCoord(),rectangle.getScaledYCoord(),rectangle.getScaledLength(),rectangle.getScaledHeight());
			}	
		}
		
	}
	
	/**
	 * Draws the short read plot under the condition that densityPlot is not null and shortReadsVisible is true.
	 */
	private void drawShortReads(){
		if(scaledDensityPlot!=null && shortReadsVisible){
			for(GraphColumn line:scaledDensityPlot){
				stroke(0);
				line(line.getScaledX(),line.getScaledYStart(),line.getScaledX(),line.getScaledYEnd());
			}
		}
	}
	private void drawTightIsoforms(){
	//		if(tightIsoformsVisible && allScaledEndCoords!=null){
	//			for(Integer currentEnd:allScaledEndCoords){
	//				line(800-currentEnd,0,800-currentEnd,100);
	//			}
	//			int yPosition = 40;
	//			for(MRNA isoform: isoforms){
	//				fill(0,0,255, 100); //Blue
	//				for(Exon exons :isoform.getExon().values()){
	//					int startScaled = (int) map(exons.getStart()-startOfGene,0,lengthOfGene,0,800);
	//					int lengthScaled=(int) map(exons.getLength(),0,lengthOfGene,0,800);
	//					
	//					if(isoform.getStrand()){
	//						rect(startScaled,yPosition+5,lengthScaled,10);
	//					}else{
	//						rect(800-(startScaled+lengthScaled),yPosition+5,lengthScaled,10);
	//					}
	//							
	//				}
	//				fill(255,0,0); //Red
	//				for(CDS cds :isoform.getCDS().values()){
	//						int startScaled =(int) map(cds.getStart()-startOfGene, 0,lengthOfGene,0,800);
	//						int lengthScaled=(int) map(cds.getLength(),0,lengthOfGene,0,800);
	//						if(isoform.getStrand()){
	//							rect(startScaled,yPosition,lengthScaled,20);
	//						}else{
	//							rect(800-(startScaled+lengthScaled),yPosition,lengthScaled,20);
	//						}
	//						
	//				}
	//				fill(0,255,0, 100);//Green
	//				if(isoform.getStartCodon()!=null){
	//					int startScaled = (int) map(isoform.getStartCodon().getStart()-startOfGene,0,lengthOfGene,0,800);
	//					int lengthScaled=(int) map(isoform.getStartCodon().getLength(),0,lengthOfGene,0,800);
	//					if(isoform.getStrand()){
	//						rect(startScaled,yPosition,lengthScaled,20);
	//					}else{
	//						rect(800-(startScaled+lengthScaled),yPosition,lengthScaled,20);
	//					}
	//				}
	//				if(isoform.getStopCodon()!=null){
	//					int startScaled = (int) map(isoform.getStopCodon().getStart()-startOfGene,0,lengthOfGene,0,800);
	//					int lengthScaled=(int) map(isoform.getStopCodon().getLength(),0,lengthOfGene,0,800);
	//					if(isoform.getStrand()){
	//						rect(startScaled,yPosition,lengthScaled,20);
	//					}else{
	//						rect(800-(startScaled+lengthScaled),yPosition,lengthScaled,20);
	//					}
	//				}
	//				
	//				yPosition +=30;
	//			}
	//		}	
		}
	
	/**
	 * Draws the weighted isoforms under the conditions that weightedIsoformVisible is true and weightedRectangles is not null
	 */
	private void drawWeightedIsoforms() {
		if(weightedRectangles!=null && weightedIsoformVisible){
			drawGrid();
			synchronized (weightedRectangles) {
				for(Rectangle rectangle:weightedRectangles){
					fill(rectangle.getColor());
					float newScaledYCoord = graphYStart-((graphYStart-rectangle.getScaledYCoord())*yScale);
					//Becasue the rectangle is drawn from the corner, subtract half its height;
					newScaledYCoord=newScaledYCoord-(rectangle.getScaledHeight()/2);
					rect(rectangle.getScaledXCoord(),newScaledYCoord,rectangle.getScaledLength(),rectangle.getScaledHeight());
				}	
			}
			drawLabelForHeight();
		}
		fill(0);
	}
	
	/**
	 * Draws a floating label that describes the height at each xCoordinate.
	 */
	private void drawLabelForHeight(){
		if(mouseX<=800){
			
			text("Average Height "+ getShortReadDensityHeightAt(mouseX) + " @ "+ 
					"\n"+tempFunction(mouseX), 350, 650);
			line(mouseX,600,400,620);
		}
	}
	
	/**
	 * Draws a grid for the density plot
	 */
	private void drawGrid() {
		line(0,510,800,510);
		line(800,0,800,510);
		int count =0;
		for(int i =510;i>0; i-=(yScale)){
			if(count%5==0){
				text(""+count,800,i);
				line(0,i,800,i);
			}
			
			count++;	
		}
		
	}
	
	/**
	 * Gets the short read density height at the specified xPixel
	 * 
	 * @param position is the pixel you want the height of
	 * 
	 * @return the short read density height at
	 */
	private int getShortReadDensityHeightAt(int position){
		if(scaledDensityPlot!=null){
			if(position == scaledDensityPlot.get(position).getScaledX()){
				return scaledDensityPlot.get(position).getHeight();
			}else{
				return scaledDensityPlot.get(800-position).getHeight();
			}
		}else{
			System.err.println("An attempt was made to fetch the height of coordinate but densityPlot was null");
			return 0;
		}
	}
	private String tempFunction(int position){
		if(scaledDensityPlot!=null){
			if(position == scaledDensityPlot.get(position).getScaledX()){
				return scaledDensityPlot.get(position).getAbsoluteXStart()+"-"+ scaledDensityPlot.get(position).getAbsoluteXEnd();
			}else{
				return scaledDensityPlot.get(800-position).getAbsoluteXStart()+"-"+ scaledDensityPlot.get(800-position).getAbsoluteXEnd();
			}
		}else{
			System.err.println("An attempt was made to fetch the height of coordinate but densityPlot was null");
			return "Empty";
		}
	}
	private void drawErrorBars(){
		if(errorBarsVisible && errorBars!=null){
			for(ErrorBars error:errorBars){
				double SD = error.getStandardDeviation()*yScale;
				float newScaledYPosition = graphYStart-(yScale*(graphYStart-error.getScaledYPosition()));
				int start = (int) (newScaledYPosition-SD);
				int end = (int) (newScaledYPosition+SD);
				line(error.getScaledXPosition(),start,error.getScaledXPosition(),end);
//				strokeWeight(10);
//				point(error.getScaledXPosition(),newScaledYPosition);
//				strokeWeight(1);
			}
		}
	}
	public void setYScale(int iYScale){
		yScale=iYScale;
	}
	private ArrayList<Integer> getArrayOfHeights(int absoluteStart, int absoluteEnd){
		ArrayList<Integer> arrayOfInts = new ArrayList<Integer>();
		for(int i =absoluteStart;i<=absoluteEnd;i++){
			arrayOfInts.add(absoluteDensityMap.get(i));
		}
		return arrayOfInts;
		
		
	}
}
