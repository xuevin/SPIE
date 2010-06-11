package genomeBrowser;

import gffParser.CDS;
import gffParser.Exon;
import gffParser.Gene;
import gffParser.MRNA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
	private boolean tightIsoformsVisible;
	private boolean shortReadsPlotVisible,geneIsoformsVisible, isCodingStrand,weightedIsoformVisible,errorBarsVisible
					,referenceIsoformVisible,splicingLinesVisible,isoformLabelsVisible,selectedIsoformsVisible;
	private TreeSet<Integer> allScaledEndCoords;
	private List<Line> linesToDraw;
	private List<GraphColumn> isformSpecificDensityPlot;
	private List<Rectangle> geneIsoforms,weightedIsoform,referenceIsoform,selectedIsoforms;
	private List<ErrorBars> errorBars;
	private List<Label> isoformLabels;
	private HashMap<Integer, Integer> absoluteDensityMap;
	private HashMap<Integer,GraphColumn>scaledShortReadsPlot;
	private int yScale,xScale;
	private int graphYStart,graphXEnd,graphXStart,graphYEnd,shortReadPlotYStart;
	

	
	
	public ProcessingApplet(int iWidth, int iHeight){
		width=iWidth;
		height=iHeight;
		isCodingStrand=true;
		
		geneIsoformsVisible = false;
		tightIsoformsVisible = false;
		shortReadsPlotVisible=false;
		weightedIsoformVisible=false;
		splicingLinesVisible=false;
		referenceIsoformVisible=false;
		isoformLabelsVisible=true;
		selectedIsoformsVisible=false;
		
		absoluteDensityMap=null;
		linesToDraw=null;
		geneIsoforms=null;
		scaledShortReadsPlot=null;
		selectedIsoforms=null;
		
		isformSpecificDensityPlot=null;
		graphYStart = 500;
		graphYEnd = 40;
		graphXStart = 200;
		graphXEnd = 900;
		shortReadPlotYStart =600;
	
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
		drawShortReads();
		drawGeneIsoforms();
		drawWeightedIsoforms();
		drawErrorBars();
		drawReferenceIsoform();
		drawIsoformLabels();
		drawSelectedIsoforms();
		
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
		if(geneIsoforms!=null){
			for(Rectangle rectangle:geneIsoforms){
				rectangle.setScaledXCoord(reverse(rectangle.getScaledXCoord()+rectangle.getScaledLength()));
			}	
		}
		if(linesToDraw!=null){
			for(Line line:linesToDraw){	
				line.setXCoordStart(reverse(line.getXCoordStart()));
				line.setXCoordEnd(reverse(line.getXCoordEnd()));
			}	
		}
		if(scaledShortReadsPlot!=null){
			for(GraphColumn column:scaledShortReadsPlot.values()){
				column.setScaledX(reverse(column.getScaledX()));
			}
		}
		if(weightedIsoform!=null){
			for(Rectangle rectangle:weightedIsoform){
				rectangle.setScaledXCoord(reverse(rectangle.getScaledXCoord()+rectangle.getScaledLength()));
			}
		}
		if(errorBars!=null){
			for(ErrorBars errorBar:errorBars){
				errorBar.setScaledXCoord(reverse(errorBar.getScaledXPosition()));
			}
		}
		if(referenceIsoform!=null){
			for(Rectangle rectangle:referenceIsoform){
				rectangle.setScaledXCoord(reverse(rectangle.getScaledXCoord()+rectangle.getScaledLength()));
			}
		}
		
	}
	public void setShortReadsVisible(boolean selected) {
		shortReadsPlotVisible=selected;	
	}
	public void setWeightedIsoformsVisible(boolean selected) {
		weightedIsoformVisible=selected;	
	}
	public void setGeneVisible(boolean selected) {
		geneIsoformsVisible=selected;
	}
	public void setSpliceLinesVisible(boolean selected) {
		splicingLinesVisible=selected;
	}
	public void setErrorBarsVisible(boolean selected){
		errorBarsVisible=selected;
	}
	public void setReferenceIsoformVisible(boolean selected){
		referenceIsoformVisible=selected;
	}
	public void setSelectedIsoformsVisible(boolean selected){
		selectedIsoformsVisible=selected;
	}
	/**
	 * Show new isoform.
	 * 
	 * @param draws the gene you want to see
	 */
	public synchronized void loadGene(Gene gene) {
		System.out.println("Call Made to Load Gene");
		Collection<MRNA> isoforms;
		isoforms = gene.getMRNA().values();
		int absoluteLengthOfGene = gene.getLength();
		int absoluteStartOfGene= gene.getStart();
		int absoluteEndOfGene=gene.getEnd();
		geneIsoforms = Collections.synchronizedList(new ArrayList<Rectangle>());
		linesToDraw = Collections.synchronizedList(new ArrayList<Line>());
		isoformLabels = Collections.synchronizedList(new ArrayList<Label>());
		isCodingStrand = gene.getStrand();
		
		
		//The code below fill in rectanglesToDraw with scaled values
		if(isoforms!=null){
			int yPosition = graphYEnd;	
			for(MRNA isoform: isoforms){
				
				//Grey
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
					Rectangle temp = new Rectangle(scaledStart, yPosition+5, scaledLength, 10,exons.getStart(),exons.getEnd(), color);
					
					geneIsoforms.add(temp);
					sortedRectangles.add(temp);					
				}
					
				color = color(150); //Light Grey
				for(CDS cds :isoform.getCDS().values()){
					//FIXME <<Slight issue with mapping
					float scaledLength= map(cds.getLength(),0,absoluteLengthOfGene,1,graphXEnd-graphXStart);
					float scaledStart;
					if(isCodingStrand){
						scaledStart =  map(cds.getStart(),absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd);
					}else{
						scaledStart = reverse(map(cds.getEnd(),absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd));
					}
					Rectangle temp = new Rectangle(scaledStart, yPosition, scaledLength, 20,cds.getStart(),cds.getEnd(), color);
					geneIsoforms.add(temp);
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
					float scaledLength=map(isoform.getStartCodon().getLength(),0,absoluteLengthOfGene,0,graphXEnd-graphXStart);
					float scaledStart;
					if(isCodingStrand){
						scaledStart =  map(isoform.getStartCodon().getStart(),
									absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd);
					}else{
						scaledStart = reverse(map(isoform.getStartCodon().getEnd(),
								absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd));
					}
					geneIsoforms.add(new Rectangle(scaledStart, yPosition, scaledLength, 20,isoform.getStartCodon().getStart(),isoform.getStartCodon().getEnd(), color));	
				}
				if(isoform.getStopCodon()!=null){
					float lengthScaled= map(isoform.getStopCodon().getLength(),0,absoluteLengthOfGene,0,graphXEnd-graphXStart);
					float startScaled;
					if(isCodingStrand){
						startScaled = map(isoform.getStopCodon().getStart(),absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd);
					}else{
						startScaled = reverse(map(isoform.getStopCodon().getEnd(),
								absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd));
					}
					geneIsoforms.add(new Rectangle(startScaled, yPosition, lengthScaled, 20,isoform.getStopCodon().getStart(),isoform.getStopCodon().getEnd(), color));
				}
				isoformLabels.add(new Label(isoform.getId(), 0, yPosition+20));
				yPosition +=30;
			}
		}
	}
	public synchronized void loadShortReads(ArrayList<SAMRecord> iShortReads,int iShortReadsStart,int iShortReadsEnd){
		
		//First load an absolute density plot hash
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
		}
		
		fillScaledShortReadsPlot(absoluteDensityMap,iShortReadsStart,iShortReadsEnd);
	}
	/**
	 * @param mrna is the isoform the user will view
	 * @param startOfGene is the start coordinates of the gene (inclusive)
	 * @param lengthOfGene is the length of the gene (inclusive)
	 * 
	 * This function should only be called after the absoluteDensityPlot is loaded through the loadShortReadsFunction
	 */
	public synchronized void loadWeightedIsoform(MRNA mrna,int startOfGene,int lengthOfGene) {
		//Under the condition that the loadShortReads function has already been calleld
		if(absoluteDensityMap!=null){
			weightedIsoform = Collections.synchronizedList(new ArrayList<Rectangle>());
			referenceIsoform=Collections.synchronizedList(new ArrayList<Rectangle>());
			errorBars = Collections.synchronizedList(new ArrayList<ErrorBars>());
			int absoluteLengthOfGene = lengthOfGene;
			int absoluteStartOfGene= startOfGene;
			int absoluteEndOfGene= startOfGene+lengthOfGene-1; //Last coordinate
			
			
			//Grey
			int color = color(40);//DarkGrey
			float yPosition = graphYStart;
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
				//Add it to the arrays
				Rectangle temp = new Rectangle(startScaled, yPosition-average, lengthScaled,10,
						exons.getStart(),exons.getEnd(), color);
				weightedIsoform.add(temp);
				errorBars.add(new ErrorBars(getArrayOfHeights(exons.getStart(),exons.getEnd()),
						(startScaled+lengthScaled/2), yPosition-average));
				Rectangle temp2 = new Rectangle(startScaled, 30, lengthScaled,10,exons.getStart(),
						exons.getEnd(), color);
				referenceIsoform.add(temp2);
			}

			color = color(150); //Light Grey
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
				
				//Get the rectangle that is nearest to the xCoordinate to get the appropriate height
				yPosition=getRectangleNearestToXCoord(weightedIsoform,startScaled).getScaledYCoord();
				Rectangle temp = new Rectangle(startScaled, yPosition, lengthScaled,10,
						cds.getStart(),cds.getEnd(), color);
				weightedIsoform.add(temp);
				Rectangle temp2 = new Rectangle(startScaled, 30, lengthScaled,10,
						cds.getStart(),cds.getEnd(), color);
				referenceIsoform.add(temp2);
			}	
		}
		
	}
	public synchronized void loadArrayOfIsoforms(ArrayList<MRNA> iMRNAArray){
		for(MRNA mrna:iMRNAArray){
			selectedIsoforms=Collections.synchronizedList(new ArrayList<Rectangle>());
			
		}
		
	}
	public void setYScale(int iYScale){
		yScale=iYScale;
	}
	public void clearWeightedIsoforms(){
		weightedIsoform=null;
	}
	private void fillInIsoformSpecificDensityPlot(ArrayList<SAMRecord> iShortReads,MRNA iMRNA) {
		isformSpecificDensityPlot= Collections.synchronizedList(new ArrayList<GraphColumn>());
			
	}
	private void fillScaledShortReadsPlot(HashMap<Integer, Integer> iAbsoluteDensity,int geneStart,int geneEnd){
		System.out.print(geneStart +" " +geneEnd);
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
		
		for(int i = geneStart;i<=geneEnd;i++){
			int mappedPixel = (int) map(i,geneStart,geneEnd,graphXStart,graphXEnd);
			//System.out.println(mappedPixel +"---Abs" + iAbsoluteDensity.get(i));
			if(prevPixel==-1){
				frameAbsoluteStart=i;
				frameAbsoluteEnd=i;
				numberOfShortReads=1;
				currentSum=iAbsoluteDensity.get(i);
				prevPixel=mappedPixel;
			}else if(mappedPixel!= prevPixel){
				scaledShortReadsPlot.get(prevPixel).incrementHeight((int)((double)currentSum/numberOfShortReads));
				scaledShortReadsPlot.get(prevPixel).setAbsoluteXCoords(frameAbsoluteStart,frameAbsoluteEnd);
				prevPixel=mappedPixel;
				numberOfShortReads=1;
				currentSum=iAbsoluteDensity.get(i);
				frameAbsoluteStart=i;
				frameAbsoluteEnd=i;
			}else if(i==geneEnd){
				frameAbsoluteEnd=i;
				numberOfShortReads++;
				scaledShortReadsPlot.get(prevPixel).incrementHeight((int)((double)currentSum/numberOfShortReads));
				scaledShortReadsPlot.get(prevPixel).setAbsoluteXCoords(frameAbsoluteStart,frameAbsoluteEnd);
			}else{	
				currentSum+=iAbsoluteDensity.get(i);
				frameAbsoluteEnd=i;
				numberOfShortReads++;
			} 
//			System.out.println(i + "----"  + iAbsoluteDensity.get(i));
		}
		strokeWeight(1);
		shortReadsPlotVisible=true;
	}
/**
	 * Draws the splice lines that connect the exons and cds.
	 * Draws only under the condition that splicingLinesVisible is true, geneVisible is true,
	 * and linesToDraw is not null and rectanglesToDraw is not null.
	 */
	private void drawSplicingLines() {
		if(splicingLinesVisible&&geneIsoformsVisible&&linesToDraw!=null&&geneIsoforms!=null){
			for(Line line:linesToDraw){
				line(line.getXCoordStart(),line.getYCoordStart(),line.getXCoordEnd(),line.getYCoordEnd());
			}	
		}
	}
	
	/**
	 * Draws all the isoforms of a gene under the condition that genes are visible and rectanglesToDraw is not null. 
	 * It iterates through the rectanglesToDraw
	 */
	private void drawGeneIsoforms(){
		if(geneIsoformsVisible&&geneIsoforms!=null){
			for(Rectangle rectangle:geneIsoforms){
				fill(rectangle.getColor());
				rect(rectangle.getScaledXCoord(),rectangle.getScaledYCoord(),rectangle.getScaledLength(),rectangle.getScaledHeight());
			}	
		}
		fill(0);
	}
	
	private void drawTightIsoforms(){
	}
	
	/**
	 * Draws the weighted isoforms under the conditions that weightedIsoformVisible is true and weightedRectangles is not null
	 */
	private void drawWeightedIsoforms() {
		if(weightedIsoform!=null && weightedIsoformVisible){
			drawGrid();
			synchronized (weightedIsoform) {
				for(Rectangle rectangle:weightedIsoform){
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
	
	private void drawReferenceIsoform() {
		if(referenceIsoform!=null && referenceIsoformVisible){
			synchronized (weightedIsoform) {
				for(Rectangle rectangle:referenceIsoform){
					fill(rectangle.getColor());
					rect(rectangle.getScaledXCoord(),rectangle.getScaledYCoord(),rectangle.getScaledLength(),rectangle.getScaledHeight());	
				}
			}
				
		}
		
		
	}
	private void drawSelectedIsoforms() {
		if(selectedIsoforms!=null && selectedIsoformsVisible){
			synchronized (selectedIsoforms) {
				for(Rectangle rectangle:selectedIsoforms){
					fill(rectangle.getColor());
					rect(rectangle.getScaledXCoord(),rectangle.getScaledYCoord(),rectangle.getScaledLength(),rectangle.getScaledHeight());	
				}
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
	 * Draws a floating label that describes the height at each xCoordinate.
	 */
	private void drawLabelForHeight(){
		if(mouseX>=graphXStart && mouseX<=graphXEnd){
			text("Average Height "+ getShortReadDensityHeightAt(mouseX) + " @ "+ 
					"\n"+getAbsoluteCoordinates(mouseX), 350, 650);
			line(mouseX,shortReadPlotYStart,400,620);
		}
	}
	private void drawIsoformLabels() {
		if(isoformLabels!=null && isoformLabelsVisible){
			synchronized (isoformLabels) {
				for(Label label: isoformLabels){
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
		line(graphXEnd,graphYStart,graphXEnd,50);
		int count =0;
		for(int i =graphYStart+5;i>50; i-=(yScale)){
			if(count%5==0){
				text(""+count,graphXEnd,i);
				line(graphXStart,i,graphXEnd,i);
			}
			count++;	
		}
	}
	
	private void drawErrorBars(){
			if(errorBarsVisible && errorBars!=null){
				synchronized (errorBars) {
					for(ErrorBars error:errorBars){
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
	private Rectangle getRectangleNearestToXCoord(List<Rectangle> listOfRectangles, float iXCoord){
		//Gets the rectangle closest to the specified xCoord by cycling through;
		Rectangle closest = null;
		float howFar =99999;
		for(Rectangle rect:listOfRectangles){
			if(rect==null){
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
		return closest;
	}
	private Rectangle getRectangleNearestToCoords(List<Rectangle> listOfRectangles,int iXCoord, int iYCoord){
		Rectangle closest =null;
		float howFar = 99999;
		for(Rectangle rect:listOfRectangles){
			float compare = dist(rect.getScaledXCoord(), rect.getScaledYCoord(), iXCoord, iYCoord);
			if(rect==null){
				closest=rect;
			}else if(compare<howFar){
				closest=rect;
				howFar=abs(rect.getScaledXCoord()-iXCoord);
			}
		}
		return closest;
		
	}
	private float reverse(float position){
		return (graphXEnd-position)+graphXStart;
	}
	
}
