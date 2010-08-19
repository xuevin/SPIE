package spie;


import gffParser.CDS;
import gffParser.Exon;
import gffParser.MRNA;


import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import drawableObjects.ErrorBar;
import drawableObjects.GraphColumn;
import drawableObjects.Junction;
import drawableObjects.Label;
import drawableObjects.Line;
import drawableObjects.RectangleComparator;
import drawableObjects.Rectangle_Unweighted;
import drawableObjects.Rectangle_Weighted;

import backgroundTasks.UpdateAnimation;

import net.sf.samtools.SAMRecord;

import processing.core.*;
import processing.pdf.*;
import spie.Read;
import spie.Statistics;

/**
 * The Class ProcessingApplet.
 */
public class ProcessingApplet extends PApplet{
	public enum View{
		COLLAPSED_UNWEIGHTED,
		COLLAPSED_WEIGHTED,
		UNCOLLAPSED_WEIGHTED,
		UNCOLLAPSED_UNWEIGHTED
	}
	
	/**
	 * The Class Weighted.
	 */
	private abstract class Weighted{
			protected ArrayList<Junction> junctionList;
			protected ArrayList<Rectangle_Weighted> weightedIsoforms;
			protected ArrayList<Rectangle_Weighted> weightedConstitutiveRectangles;
			protected ArrayList<ErrorBar> errorBars;
			protected ArrayList<Rectangle_Unweighted>referenceIsoforms;
			protected ArrayList<Label> referenceLabels;
			
			/**
			 * Animated load.
			 * Loads short read data and animates the rectangles so that they float.
			 *
			 * @param pApplet the applet
			 * @param sampleName the sample name
			 * @param newSampleReads the new sample reads
			 * @param isoform the isoform
			 */
			public void animatedLoad(ProcessingApplet pApplet,String sampleName,ArrayList<SAMRecord> newSampleReads, MRNA isoform) {
				junctionList.clear();
				errorBars.clear();
				weightedConstitutiveRectangles.clear();
				loadSingleReadSample(sampleName,newSampleReads);
				ArrayList<Read> compatibleReads = Statistics.getCompatibleReads(isoform, newSampleReads);
				ArrayList<Rectangle_Weighted> exonsOnly= new ArrayList<Rectangle_Weighted>();
				synchronized (weightedIsoforms) {
					String id="";
					for(Rectangle_Weighted rect:weightedIsoforms){
						if(rect.getScaledHeight()==10){
							exonsOnly.add(rect);
						}
						if(id==""){
							id=rect.getIsoformID();
						}else if(id!=rect.getIsoformID()){
							ArrayList<MRNA> temp = new ArrayList<MRNA>();
							temp.add(isoform);
							System.err.println("An attempt was made to animate multiple isoforms at once");
							loadArrayOfIsoforms(currentlyViewingIsoforms);
							return;
						}
					}
					weightedIsoforms=exonsOnly;
				}
	
				if(currentAnimation!=null && currentAnimation.isAlive()){
					currentAnimation.interrupt();
				}else{
					currentAnimation = new Thread(new UpdateAnimation(pApplet,isoform,compatibleReads,weightedIsoforms));
					currentAnimation.start();	
				}	
				
			}
	
			/**
			 * Clears the weighted constitutive rectangles
			 */
			public synchronized void clear() {
				weightedConstitutiveRectangles.clear();
				
			}
	
			/**
			 * Draws the weighted isoforms
			 */
			public synchronized void draw(){
				drawWeightedIsoforms();
			}
	
			/**
			 * Flips the strandess of the isoform
			 */
			public synchronized void flip(){
				for(Rectangle_Weighted rectangle:weightedIsoforms){
					rectangle.setScaledXCoord(reverse(rectangle.getScaledXCoord()+rectangle.getScaledLength()));
				}
				for(ErrorBar errorBar:errorBars){
					errorBar.setScaledXCoord(reverse(errorBar.getScaledXPosition()));
				}
				for(Rectangle_Unweighted rectangle:referenceIsoforms){
					rectangle.setScaledXCoord(reverse(rectangle.getScaledXCoord()+rectangle.getScaledLength()));
				}
				for(Rectangle_Weighted rectangle:weightedConstitutiveRectangles){	
					rectangle.setScaledXCoord(reverse(rectangle.getScaledXCoord()+rectangle.getScaledLength()));
				}
				for(Junction junction:junctionList){
					junction.setLeftScaled(reverse(junction.getLeftScaled()));
					junction.setRightScaled(reverse(junction.getRightScaled()));
				}
			}

			/**
			 * Gets the exon from the mouse coordinates.
			 *
			 * @param mouseX the mouse x
			 * @param mouseY the mouse y
			 * @return the rectangle(exon) at the coordinate
			 */
			public Rectangle_Weighted getRectFromCoord(int mouseX, int mouseY) {
				synchronized (weightedIsoforms) {
					//Iterate through the array and checks to see if the mouse is over it
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

			/**
			 * Load new array of weighted isoforms.
			 *
			 * @param listOfMRNA the list of mrna
			 */
			public synchronized void loadNewArrayOfWeightedIsoforms(Collection<MRNA> listOfMRNA){
				if(currentListOfSamples.size()!=0){
					//Make new arrays
					weightedIsoforms.clear(); 
					referenceIsoforms.clear();
					referenceLabels.clear();
					errorBars.clear();
					weightedConstitutiveRectangles.clear();
					junctionList.clear();
					
					
					//Get a list of the the constitutive intervals for coloring in constitutive regions
					ArrayList<Interval> listOfConstitutiveIntervals = Statistics.getConstitutiveIntervals(tempConstitutiveUnscaledPositions);
					
					//Get a list of the large consitutitive exons
					ArrayList<Interval> listOfIntrons = getLargeConstitutiveIntrons(listOfMRNA);
					
					//Fill in the junctions
					fillJunctionList(junctionList,listOfMRNA);
					
					int yRefStart=30; // Start of where the reference labels go
					int count =0;
					
					
					//For each isoform					
					for(MRNA mrna:listOfMRNA){
						hashOfIsoforms.put(mrna.getId(),mrna);
						int cdsColor = color(convertColor(colorScheme.getCDSColor()));
						int exonColor = color(convertColor(colorScheme.getExonColor()));
						if(listOfMRNA.size()>1){	
							cdsColor = convertColor(colorScheme.getColorOf(count));
							exonColor = convertColor(colorScheme.getColorOf(count));	
							count++;
						}
						
						ArrayList<Read>compatibleReads = Statistics.getCompatibleReads(mrna, currentListOfSamples.get(0));
						referenceLabels.add(new Label(mrna.getId(), 10, yRefStart+10));
						
						//For each exon
						int exonCountPosition=0;
						for(Exon exon :mrna.getExons().values()){
							//Find the average of the region in mention
							
							boolean endExon = false;
							if(exonCountPosition==0 || exonCountPosition==mrna.getExons().values().size()-1){
								endExon=true;
							}
							double average = Statistics.getWeightOfExon(exon,compatibleReads,endExon);
							exonCountPosition++;
							
							//Scale the length and start
							int lengthScaled= scaleLength(exon.getLength(), listOfIntrons);
							int startScaled = scalePosition(exon.getStart(), listOfIntrons); 
							
							Rectangle_Weighted temp = new Rectangle_Weighted(	startScaled, 
																				lengthScaled,
																				10,
																				exon.getStart(),
																				exon.getEnd(),
																				mrna.getId(),
																				average,
																				exonColor);
							weightedIsoforms.add(temp);
							errorBars.add(new ErrorBar(	average,
									Statistics.getStandardDeviation(exon.getStart(),exon.getEnd(),compatibleReads),
									temp.getAbsoluteStart(),
									temp.getAbsoluteEnd(),
									mrna.getId(),
									(startScaled+lengthScaled/2)));
							
							//Fill in array that draws the reference isoforms
							Rectangle_Unweighted temp2 = new Rectangle_Unweighted(startScaled, yRefStart, lengthScaled,10,exon.getStart(),
									exon.getEnd(),mrna.getId(),exonColor);
							referenceIsoforms.add(temp2);
							
			
							//Fill in array that draws the scaled constitutive lines
							//The difference here is that that the first YStartPosition represents the absolute height
							//It still needs to be scaled (look at drawWeightedConstitutiveLines)
							for(Interval interval :listOfConstitutiveIntervals){
								if(interval.getStartCoord()>=exon.getStart()&&interval.getEndCoord()<=exon.getEnd()){
									int color = convertColor(colorScheme.getConstitutiveColor());
									weightedConstitutiveRectangles.add(new Rectangle_Weighted(	scalePosition(interval.getStartCoord(),listOfIntrons), 
																								scaleLength(interval.getLength(),listOfIntrons), 
																								10, 
																								interval.getStartCoord(), 
																								interval.getEndCoord(), 
																								mrna.getId(), 
																								average, 
																								color));	
								}
							}
						}
						//For each CDS
						for(CDS cds:mrna.getCDS().values()){
							int lengthScaled= scaleLength(cds.getLength(),listOfIntrons);
							int startScaled = scalePosition(cds.getStart(), listOfIntrons);
							
							//Get the rectangle that is nearest to the xCoordinate who has the same MRNA
							//to get the appropriate height
							Rectangle_Weighted exon_Rect=getRectangleNearestToXCoord(weightedIsoforms,mrna.getId(),startScaled);
							double weight = exon_Rect.getWeight();
							Rectangle_Weighted temp = new Rectangle_Weighted(	startScaled,
																				lengthScaled,
																				20,
																				cds.getStart(),
																				cds.getEnd(),
																				mrna.getId(),
																				weight,
																				cdsColor);
							weightedIsoforms.add(temp);
							Rectangle_Unweighted temp2 = new Rectangle_Unweighted(startScaled, yRefStart-5, lengthScaled,20,
									cds.getStart(),cds.getEnd(),mrna.getId(),cdsColor);
							referenceIsoforms.add(temp2);
							
							//Fill in array that draws the scaled constitutive lines
							//The difference here is that that the first YStartPosition represents the absolute height
							//It still needs to be scaled (look at drawWeightedConstitutiveLines)
							for(Interval interval :listOfConstitutiveIntervals){
								//if the coding region is engulfed in the constitutive interval
								int color = convertColor(colorScheme.getConstitutiveColor());
								if(interval.getStartCoord()>=cds.getStart()&&interval.getEndCoord()<=cds.getEnd()){
									weightedConstitutiveRectangles.add(new Rectangle_Weighted(	scalePosition(interval.getStartCoord(),listOfIntrons), 
																								scaleLength(interval.getLength(),listOfIntrons),
																								20, 
																								interval.getStartCoord(), 
																								interval.getEndCoord(), 
																								mrna.getId(), 
																								weight, 
																								color));
								//the coding region has the beginning of the interval
								}else if(	cds.getStart()<=interval.getStartCoord() && //cds start < interval start
											cds.getEnd()<=interval.getEndCoord() && 	//cds end < interval end
											cds.getEnd()>=interval.getStartCoord()){ 	//cds end > interval start
									
									weightedConstitutiveRectangles.add(new Rectangle_Weighted(	scalePosition(interval.getStartCoord(),listOfIntrons), 
																								scaleLength((cds.getEnd()-interval.getStartCoord()+1),listOfIntrons),
																								20, 
																								interval.getStartCoord(), 
																								cds.getEnd(), 
																								mrna.getId(), 
																								weight, 
																								color));
								//the coding region has the end of the interval
								}else if(	cds.getStart()<=interval.getEndCoord() && 	//cds start < interval end 
											cds.getEnd()>=interval.getEndCoord() && 	//cds end > interval end
											cds.getStart()>=interval.getStartCoord()){	//cds start > interval start 
									weightedConstitutiveRectangles.add(new Rectangle_Weighted(	scalePosition(cds.getStart(),listOfIntrons), 
																								scaleLength(interval.getEndCoord()-cds.getStart()+1,listOfIntrons),
																								20, 
																								cds.getStart(), 
																								interval.getEndCoord(), 
																								mrna.getId(), 
																								weight, 
																								color));
									
								}
							}
						}
						yRefStart+=20;
					}
					if(!isCodingStrand){
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
							for(Rectangle_Unweighted rectangle:referenceIsoforms){
								rectangle.setScaledXCoord(reverse(rectangle.getScaledXCoord()+rectangle.getScaledLength()));
							}
						}
						if(weightedConstitutiveRectangles!=null){
							for(Rectangle_Weighted rectangle:weightedConstitutiveRectangles){	
								rectangle.setScaledXCoord(reverse(rectangle.getScaledXCoord()+rectangle.getScaledLength()));
							}
						}
						if(junctionList!=null){
							for(Junction junction:junctionList){
								junction.setLeftScaled(reverse(junction.getLeftScaled()));
								junction.setRightScaled(reverse(junction.getRightScaled()));
							}
						}	
					}
				}else{
					System.err.println("A call was made to loadWeightedIsoforms but there was no Read Data");
				}
				
			}

			/**
			 * Draw error bars.
			 */
			private void drawErrorBars(){
						if(errorBars!=null && errorBarsVisible){
							synchronized (errorBars) {
								for(ErrorBar error:errorBars){
									double SD = error.getStandardDeviation()*yScale;
									int newScaledYPosition = (int)(graphYStart-(yScale*error.getWeight()));
									int start = (int) (newScaledYPosition-SD);
									int end = (int) (newScaledYPosition+SD);
									line(error.getScaledXPosition(),start,error.getScaledXPosition(),end);
			//						strokeWeight(10);
			//						point(error.getScaledXPosition(),newScaledYPosition);
			//						strokeWeight(1);
								}	
							}
						}
					}
	
			/**
			 * Draws a grid for the weighted Isoforms
			 */
			private void drawGrid() {
				if(gridLinesVisible){
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
			}
	
			/**
			 * Draw mouse hover info.
			 */
			private void drawHoverInfo(){
				Rectangle_Weighted rect =  getRectUnderMouse();
				if(rect!=null){
					fill(200);
					rect(mouseX,mouseY-20,250,120);
					fill(0);
					MRNA isoform = hashOfIsoforms.get(rect.getIsoformID());
					text("Isoform:" + rect.getIsoformID()+
						"\nStart:\t"+rect.getAbsoluteStart() + 
						"\nEnd: \t " + rect.getAbsoluteEnd()+
						"\nLength:\t" + rect.getAbsoluteLength() +
						"\n#Body Reads:\t" +  Statistics.getNumberOfBodyReads(isoform, rect.getAbsoluteStart(), rect.getAbsoluteEnd(), currentListOfSamples.get(0)) + 
						"\n#Junction Reads:\t" + Statistics.getNumberOfJunctionReads(isoform, rect.getAbsoluteStart(), rect.getAbsoluteEnd(), currentListOfSamples.get(0))
						,mouseX+10,mouseY);
						
				}	
			}
	
			/**
			 * Draw junction lines.
			 */
			private void drawJunctionLines() {
				if(junctionList!=null){
					synchronized (junctionList) {
						
						strokeWeight(3);
						for(Junction junction:junctionList){
							stroke(junction.getColor());
							int yPos=graphYStart;
	//						for(int i=0;i<junction.getHits();i++){
	//							line(junction.getLeftScaled(),yPos,junction.getRightScaled(),yPos);
	//							yPos-=yScale;
	//						}
							int height = (int) (yPos-(yScale*junction.getWeight()));
							line(junction.getLeftScaled(),height,junction.getRightScaled(),height);
						}
						strokeWeight(1);
						stroke(0);
					}
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
			 * Draw the reference isoforms and the labels
			 */
			private void drawReferenceIsoform() {
				if(referenceIsoforms!=null){
					synchronized (referenceIsoforms) {
						for(Rectangle_Unweighted rectangle:referenceIsoforms){
							fill(rectangle.getColor());
							rect(rectangle.getScaledXCoord(),rectangle.getScaledYCoord(),rectangle.getScaledLength(),rectangle.getScaledHeight());	
						}
					}
					fill(0);
					drawLabelsForReference();		
				}
				
			}
	
			/**
			 * Draw weighted constitutive rectangles.
			 */
			private void drawWeightedConstitutiveRectangles(){
				if(constitutiveRegionsVisible){
					synchronized (weightedConstitutiveRectangles) {
							for(Rectangle_Weighted rectangle:weightedConstitutiveRectangles){
								fill(rectangle.getColor());
								int newScaledYCoord = (int)(graphYStart-(yScale*rectangle.getWeight())); 
									//graphYStart-((graphYStart-rectangle.getScaledYCoord())*yScale);
								//Because the rectangle is drawn from the corner, subtract half its height;
								newScaledYCoord=newScaledYCoord-(rectangle.getScaledHeight()/2);
								rect(rectangle.getScaledXCoord(),newScaledYCoord,rectangle.getScaledLength(),rectangle.getScaledHeight());
							}
							fill(0);
					}
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
				drawGrid();
				synchronized (weightedIsoforms) {
					for(Rectangle_Weighted rectangle:weightedIsoforms){
						
						fill(rectangle.getColor());
						int newScaledYCoord = (int)(graphYStart-(yScale*rectangle.getWeight())); 
							//graphYStart-((graphYStart-rectangle.getScaledYCoord())*yScale);
						//Because the rectangle is drawn from the corner, subtract half its height;
						newScaledYCoord=newScaledYCoord-(rectangle.getScaledHeight()/2);
						rect(rectangle.getScaledXCoord(),newScaledYCoord,rectangle.getScaledLength(),rectangle.getScaledHeight());
					}
					
				}
				fill(0);
				drawErrorBars();
				drawReferenceIsoform();
				drawWeightedConstitutiveRectangles();
				drawJunctionLines();
				drawHoverInfo();
			}
	
			/**
			 * Fill in the arrays with the scaled junctions.
			 *
			 * @param iJunctionList the input junction list
			 * @param listOfMRNA the list of mrna
			 */
			private void fillJunctionList(ArrayList<Junction> iJunctionList,Collection<MRNA> listOfMRNA) {

				ArrayList<Interval> listOfIntrons = getLargeConstitutiveIntrons(listOfMRNA);
				int count = 0;
				for(MRNA mrna :listOfMRNA){
					for(Read read:Statistics.getCompatibleReads(mrna, currentListOfSamples.get(0))){
						if(read.isJunctionRead()){
							
							int junctionStart = read.getFirstExonEnd()-(readLength-overhang-1);
							int junctionEnd = read.getLastExonBeginning()+(readLength-overhang-1);
							
							int startScaled = scalePosition(junctionStart,listOfIntrons);
							int endScaled = scalePosition(junctionEnd,listOfIntrons);				
							
							boolean found =false;
							for(Junction junction:iJunctionList){
								if(junction.getLeftScaled()==startScaled && junction.getRightScaled()==endScaled && junction.getIsoformID()==mrna.getId()){
									junction.addRead(read);
									found=true;
								}					
							}
							if(!found){
								Junction tempJunction = new Junction(	junctionStart,
																		junctionEnd,
																		startScaled,
																		endScaled,
																		mrna.getId(),
																		convertColor(colorScheme.getColorOf(count)));
								tempJunction.addRead(read);
								iJunctionList.add(tempJunction);
								
							}		
						}	
					}
					for(Junction junction:iJunctionList){
						double weight = Statistics.getWeightOfJunction(junction);
						junction.setWeight(weight);
						errorBars.add(new ErrorBar(	weight, 
													Statistics.getStandardDeviation(junction.getAbsoluteStart(),
																					junction.getAbsoluteEnd(),
																					junction.getSpanningReads()), 
													junction.getAbsoluteStart(),
													junction.getAbsoluteEnd(),
													junction.getIsoformID(), 
													junction.getScaledMiddle()));
					}
					count++;
				}
			}
			
			/**
			 * Scale length.
			 *
			 * @param length the length
			 * @param listOfIntrons the list of introns
			 * @return the scaled length
			 */
			protected abstract int scaleLength(int length,ArrayList<Interval> listOfIntrons);
			
			/**
			 * Scale position.
			 *
			 * @param position the position
			 * @param listOfIntrons the list of introns
			 * @return the scaled position
			 */
			protected abstract int scalePosition(int position,ArrayList<Interval> listOfIntrons);
			
	//		protected boolean isARepeat(Rectangle_Weighted inputRect){
	//			for(Rectangle_Weighted rect:weightedIsoforms){
	//				float lengthScaled= map(exon.getLength(),0,absoluteLengthOfGene-sum+(listOfIntrons.size()*200),0,graphXEnd-graphXStart);
			//float startScaled = scaleAbsoluteCoord_Collapsed(exon.getStart(), listOfIntrons);
	//			}
	//		}
		}
	
	/**
	 * The Class Unweighted.
	 */
	private abstract class Unweighted{
			protected ArrayList<Rectangle_Unweighted> unweightedIsoforms;
			protected ArrayList<Rectangle_Unweighted> unweightedConstitutiveRectangle;
			protected ArrayList<Line> spliceLines;
			protected ArrayList<Label> unweightedIsoformLabels;
			
			/**
			 * Draw.
			 */
			public synchronized void draw() {
				drawUnweightedIsoforms();
			}
			
			/**
			 * Clears unweighted constitutive rectangles
			 */
			public synchronized void clear() {
				unweightedConstitutiveRectangle.clear();	
			}
			
			/**
			 * Flips the strand of the isoform
			 */
			public synchronized void flip() {
				for(Rectangle_Unweighted rectangle: unweightedIsoforms){
					rectangle.setScaledXCoord(reverse(rectangle.getScaledXCoord()+rectangle.getScaledLength()));
				}
				for(Line line:spliceLines){	
					line.setXCoordStart(reverse(line.getXCoordStart()));
					line.setXCoordEnd(reverse(line.getXCoordEnd()));
				}
				for(Rectangle_Unweighted rectangle:unweightedConstitutiveRectangle){	
					rectangle.setScaledXCoord(reverse(rectangle.getScaledXCoord()+rectangle.getScaledLength()));
				}
				
			}
			
			/**
			 * Draw unweighted isoforms.
			 */
			private void drawUnweightedIsoforms() {
				drawSplicingLines();
				synchronized (unweightedIsoforms) {
					for(Rectangle_Unweighted rectangle:unweightedIsoforms){
						fill(rectangle.getColor());
						rect(rectangle.getScaledXCoord(),rectangle.getScaledYCoord(),rectangle.getScaledLength(),rectangle.getScaledHeight());
					}
				}
				fill(0);
				drawUnweightedIsoformLabels();
				drawUnweightedConstitutiveRects();
				
			}
			
			/**
			 * Draw splicing lines.
			 */
			private void drawSplicingLines() {
				if(splicingLinesVisible){
					synchronized (spliceLines) {
						stroke(0);
						for(Line line:spliceLines){
							line(line.getXCoordStart(),line.getYCoordStart(),line.getXCoordEnd(),line.getYCoordEnd());
						}
					}
						
				}
				
			}
			
			/**
			 * Draw unweighted constitutive rectangles.
			 */
			private void drawUnweightedConstitutiveRects() {
				if(constitutiveRegionsVisible){
					synchronized (unweightedConstitutiveRectangle) {
						for(Rectangle_Unweighted rectangle:unweightedConstitutiveRectangle){
							fill(rectangle.getColor());
							rect(rectangle.getScaledXCoord(), rectangle.getScaledYCoord(), rectangle.getScaledLength(), rectangle.getScaledHeight());	
						}
						fill(0);
					}	
				}
				
				
			}
			
			/**
			 * Draw unweighted isoform labels.
			 */
			private void drawUnweightedIsoformLabels() {
				synchronized (unweightedIsoformLabels) {
					for(Label label: unweightedIsoformLabels){
						text(label.getText(),label.getXScaled(),label.getYScaled());
					}	
				}
			}
	
		/**
		 * Load new array of unweighted isoforms.
		 *
		 * @param isoforms the isoforms
		 */
		public synchronized void loadNewArrayOfUnweightedIsoforms(Collection<MRNA> isoforms){
					
			//Clear Existing Arrays
			unweightedIsoforms.clear(); 
			spliceLines.clear();
			unweightedIsoformLabels.clear(); 
			unweightedConstitutiveRectangle.clear(); 
			
			ArrayList<Interval> listOfConstitutiveIntervals = Statistics.getConstitutiveIntervals(tempConstitutiveUnscaledPositions);
			
			
			//Get a list of the large consitutitive exons
			ArrayList<Interval> listOfIntrons = getLargeConstitutiveIntrons(isoforms);
			
			int yPosition=graphYEnd;
			
			for(MRNA mrna : isoforms){
				ArrayList<Rectangle_Unweighted> sortedRectangles = new ArrayList<Rectangle_Unweighted>();
				
				// Exons
				int cdsColor = convertColor(colorScheme.getCDSColor());
				int exonColor = convertColor(colorScheme.getExonColor());
				for(Exon exon:mrna.getExons().values()){
					int scaledLength= scaleLength(exon.getLength(),listOfIntrons);
					int scaledStart = scalePosition(exon.getStart(), listOfIntrons);
					 
					Rectangle_Unweighted temp = new Rectangle_Unweighted(	scaledStart,
																			yPosition+5,
																			scaledLength, 
																			10,
																			exon.getStart(),
																			exon.getEnd(),
																			mrna.getId(),
																			exonColor);
					unweightedIsoforms.add(temp);
					sortedRectangles.add(temp);
					
	
					//Fill In the constitutitive sites per exon by iterating through each constitutitve interval
					for(Interval interval :listOfConstitutiveIntervals){
						if(interval.getStartCoord()>=exon.getStart()&&interval.getEndCoord()<=exon.getEnd()){
							int color = convertColor(colorScheme.getConstitutiveColor());;
							unweightedConstitutiveRectangle.add(
									new Rectangle_Unweighted(	scalePosition(interval.getStartCoord(),listOfIntrons),
																yPosition+5, 
																scaleLength(interval.getLength(),listOfIntrons), 
																10, 
																interval.getStartCoord(), 
																interval.getEndCoord(), 
																mrna.getId(), 
																color));
						}
					}
					
				}
				
				//Load CDS
				for(CDS cds :mrna.getCDS().values()){
					int scaledLength= scaleLength(cds.getLength(),listOfIntrons);
					int scaledStart = scalePosition(cds.getStart(),listOfIntrons);
					
					Rectangle_Unweighted temp = new Rectangle_Unweighted(	scaledStart,
																			yPosition, 
																			scaledLength, 
																			20,
																			cds.getStart(),
																			cds.getEnd(),
																			mrna.getId(),
																			cdsColor);
					unweightedIsoforms.add(temp);
					
					//Fill In the constitutitive sites per exon by iterating through each constitutitve interval
					for(Interval interval :listOfConstitutiveIntervals){
						int color = convertColor(colorScheme.getConstitutiveColor());
						if(interval.getStartCoord()>=cds.getStart()&&interval.getEndCoord()<=cds.getEnd()){
							
							unweightedConstitutiveRectangle.add(
									new Rectangle_Unweighted(	scalePosition(interval.getStartCoord(),listOfIntrons),
																yPosition, 
																scaleLength(interval.getLength(),listOfIntrons), 
																20, 
																interval.getStartCoord(), 
																interval.getEndCoord(), 
																mrna.getId(), 
																color));
						}else if(	cds.getStart()<=interval.getStartCoord() && //cds start < interval start
									cds.getEnd()<=interval.getEndCoord() && 	//cds end < interval end
									cds.getEnd()>=interval.getStartCoord()){ 	//cds end > interval start
							
							unweightedConstitutiveRectangle.add(new Rectangle_Unweighted(	scalePosition(interval.getStartCoord(),listOfIntrons), 
																							yPosition,
																							scaleLength((cds.getEnd()-interval.getStartCoord()+1),listOfIntrons),
																							20, 
																							interval.getStartCoord(), 
																							cds.getEnd(), 
																							mrna.getId(),  
																							color));
						//the coding region has the end of the interval
						}else if(	cds.getStart()<=interval.getEndCoord() && 	//cds start < interval end 
									cds.getEnd()>=interval.getEndCoord() && 	//cds end > interval end
									cds.getStart()>=interval.getStartCoord()){	//cds start > interval start 
							unweightedConstitutiveRectangle.add(new Rectangle_Unweighted(	scalePosition(cds.getStart(),listOfIntrons), 
																							yPosition,
																							scaleLength(interval.getEndCoord()-cds.getStart()+1,listOfIntrons),
																							20, 
																							cds.getStart(), 
																							interval.getEndCoord(), 
																							mrna.getId(), 
																							color));
							
						}
					}
				}
				
				//Make Splice lines
				spliceLines.addAll(getSpliceLines(sortedRectangles, yPosition));
				
				
				//Load Start and Stop Codons
				int color = convertColor(colorScheme.getStartStopColor());
				if(mrna.getStartCodon()!=null){
					int scaledLength= scaleLength(mrna.getStartCodon().getLength(),listOfIntrons);
					int scaledStart = scalePosition(mrna.getStartCodon().getStart(),listOfIntrons);
					
					unweightedIsoforms.add(new Rectangle_Unweighted(	scaledStart, 
																		yPosition,
																		scaledLength, 
																		20,
																		mrna.getStartCodon().getStart(),
																		mrna.getStartCodon().getEnd(),
																		mrna.getId(),
																		color));	
				}
				if(mrna.getStopCodon()!=null){
					int scaledLength= scaleLength(mrna.getStopCodon().getLength(),listOfIntrons);
					int scaledStart = scalePosition(mrna.getStopCodon().getStart(),listOfIntrons);
					unweightedIsoforms.add(new Rectangle_Unweighted(	scaledStart, 
																		yPosition,
																		scaledLength, 
																		20,
																		mrna.getStopCodon().getStart(),
																		mrna.getStopCodon().getEnd(),
																		mrna.getId(),
																		color));
				}
				unweightedIsoformLabels.add(new Label(mrna.getId(), 10, yPosition+20));
				yPosition+=30;
			}
			if(!isCodingStrand){
				if(unweightedIsoforms!=null){
					for(Rectangle_Unweighted rectangle:unweightedIsoforms){
						rectangle.setScaledXCoord(reverse(rectangle.getScaledXCoord()+rectangle.getScaledLength()));
					}	
				}
				if(spliceLines!=null){
					for(Line line:spliceLines){	
						line.setXCoordStart(reverse(line.getXCoordStart()));
						line.setXCoordEnd(reverse(line.getXCoordEnd()));
					}	
				}
				if(unweightedConstitutiveRectangle!=null){
					for(Rectangle_Unweighted rectangle:unweightedConstitutiveRectangle){
						rectangle.setScaledXCoord(reverse(rectangle.getScaledXCoord()+rectangle.getScaledLength()));
					}
				}	
			}	
		}
		
		/**
		 * Scale length.
		 *
		 * @param length the length
		 * @param listOfIntrons the list of introns
		 * @return the scaled length
		 */
		protected abstract int scaleLength(int length,ArrayList<Interval> listOfIntrons);
		
		/**
		 * Scale position.
		 *
		 * @param position the position
		 * @param listOfIntrons the list of introns
		 * @return the scaled position
		 */
		protected abstract int scalePosition(int position,ArrayList<Interval> listOfIntrons);
	}
	
	/**
	 * The Class Uncollapsed_Weighted.
	 */
	private class Uncollapsed_Weighted extends Weighted{
		
		/**
		 * Instantiates a new uncollapsed weighted view.
		 */
		public Uncollapsed_Weighted() {
			weightedIsoforms=new ArrayList<Rectangle_Weighted>();
			weightedConstitutiveRectangles = new ArrayList<Rectangle_Weighted>();
			referenceIsoforms = new ArrayList<Rectangle_Unweighted>();
			referenceLabels= new ArrayList<Label>();
			errorBars = new ArrayList<ErrorBar>();
			junctionList = new ArrayList<Junction>();
		}
	
		/* (non-Javadoc)
		 * @see spie.ProcessingApplet.Weighted#scaleLength(int, java.util.ArrayList)
		 */
		@Override
		protected int scaleLength(int length,
				ArrayList<Interval> listOfIntrons) {
			return (int)map(length,0,absoluteLengthOfGene,0,graphXEnd-graphXStart);
		}
	
		/* (non-Javadoc)
		 * @see spie.ProcessingApplet.Weighted#scalePosition(int, java.util.ArrayList)
		 */
		@Override
		protected int scalePosition(int position,
				ArrayList<Interval> listOfIntrons) {
			return scaleAbsoluteCoord_Uncollapsed(position);
		}
	}
	
	/**
	 * The Class Collapsed_Weighted.
	 */
	private class Collapsed_Weighted extends Weighted{
		
		/**
		 * Instantiates a new collapsed weighted view
		 */
		public Collapsed_Weighted() {
			weightedIsoforms=new ArrayList<Rectangle_Weighted>();
			weightedConstitutiveRectangles = new ArrayList<Rectangle_Weighted>();
			referenceIsoforms = new ArrayList<Rectangle_Unweighted>();
			referenceLabels= new ArrayList<Label>();
			errorBars = new ArrayList<ErrorBar>();
			junctionList = new ArrayList<Junction>();
		}
		
		/* (non-Javadoc)
		 * @see spie.ProcessingApplet.Weighted#scaleLength(int, java.util.ArrayList)
		 */
		@Override
		protected int scaleLength(int length,
				ArrayList<Interval> listOfIntrons) {
			int sum=0;
			for(Interval interval:listOfIntrons){
				sum+=interval.getLength();
			}	
			return (int)map(length,0,absoluteLengthOfGene-sum+(listOfIntrons.size()*200),0,graphXEnd-graphXStart);
		}
	
		/* (non-Javadoc)
		 * @see spie.ProcessingApplet.Weighted#scalePosition(int, java.util.ArrayList)
		 */
		@Override
		protected int scalePosition(int position,
				ArrayList<Interval> listOfIntrons) {
			return scaleAbsoluteCoord_Collapsed(position, listOfIntrons);
		}
	}
	
	/**
	 * The Class Collapsed_Unweighted.
	 */
	private class Collapsed_Unweighted extends Unweighted{
	
		/**
		 * Instantiates a new collapsed_unweighted view.
		 */
		public Collapsed_Unweighted(){
			unweightedConstitutiveRectangle = new ArrayList<Rectangle_Unweighted>();
			spliceLines=new ArrayList<Line>();
			unweightedIsoforms=new ArrayList<Rectangle_Unweighted>();
			unweightedIsoformLabels = new ArrayList<Label>();
		}
		
		
	
		/* (non-Javadoc)
		 * @see spie.ProcessingApplet.Unweighted#scaleLength(int, java.util.ArrayList)
		 */
		@Override
		protected int scaleLength(int length,
				ArrayList<Interval> listOfIntrons) {
			int sum=0;
			for(Interval interval:listOfIntrons){
				sum+=interval.getLength();
			}
			return (int)map(length,0,absoluteLengthOfGene-sum+(listOfIntrons.size()*200),0,graphXEnd-graphXStart);
		}
	
		/* (non-Javadoc)
		 * @see spie.ProcessingApplet.Unweighted#scalePosition(int, java.util.ArrayList)
		 */
		@Override
		protected int scalePosition(int position,
				ArrayList<Interval> listOfIntrons) {
			return scaleAbsoluteCoord_Collapsed(position, listOfIntrons);
		}
	}
	
	/**
	 * The Class Uncollapsed_Unweighted.
	 */
	private class Uncollapsed_Unweighted extends Unweighted{	
		
		/**
		 * Instantiates a new uncollapsed unweighted view
		 */
		public Uncollapsed_Unweighted(){
			spliceLines = new ArrayList<Line>();
			unweightedIsoforms=new ArrayList<Rectangle_Unweighted>();
			unweightedConstitutiveRectangle=new ArrayList<Rectangle_Unweighted>();
			unweightedIsoformLabels = new ArrayList<Label>();
			
		}
			
		/* (non-Javadoc)
		 * @see spie.ProcessingApplet.Unweighted#scaleLength(int, java.util.ArrayList)
		 */
		@Override
		protected int scaleLength(int length,
				ArrayList<Interval> listOfIntrons) {
			return (int)map(length,0,absoluteLengthOfGene,0,graphXEnd-graphXStart);
		}
		
		/* (non-Javadoc)
		 * @see spie.ProcessingApplet.Unweighted#scalePosition(int, java.util.ArrayList)
		 */
		@Override
		protected int scalePosition(int position,
				ArrayList<Interval> listOfIntrons) {
			return scaleAbsoluteCoord_Uncollapsed(position);
		}
	}
	private Collapsed_Weighted collapsed_Weighted = new Collapsed_Weighted();
	private Collapsed_Unweighted collapsed_Unweighted = new Collapsed_Unweighted();
	private Uncollapsed_Weighted uncollapsed_Weighted = new Uncollapsed_Weighted();
	private Uncollapsed_Unweighted uncollapsed_Unweighted = new Uncollapsed_Unweighted();

	private static final long serialVersionUID = 1L;
	private int width;
	private int height;
	
	private boolean readsPlotVisible; 
	private boolean isCodingStrand;
	private boolean splicingLinesVisible;
	private boolean gridLinesVisible;
	
	private ArrayList<MRNA> currentlyViewingIsoforms;
	private ArrayList<MRNA> customListOfIsoforms;
	private ArrayList<Integer> tempConstitutiveUnscaledPositions,customConstitutiveUnscaledPositions;
	private ArrayList<ArrayList<GraphColumn> > readsPlotToDraw;
	
	private int yScale;
	private int graphYStart,graphXEnd,graphXStart,graphYEnd,readPlotYStart;
	
	
	//Data Specific to GENE (Viwer can at most view one gene at a time)
	private int absoluteStartOfGene;
	private int absoluteLengthOfGene;
	private int absoluteEndOfGene;
	private Thread currentAnimation;
	private HashMap<String,MRNA> hashOfIsoforms; //Key is the MRNA ID
	private View view;
	private boolean constitutiveRegionsVisible;
	private boolean recordPDF;
	private String locationToSave;
	
	private ArrayList<String> currentListOfSampleNames;
	private ArrayList<Integer> currentListOfBamCounts;
	private ArrayList<ArrayList<SAMRecord>> currentListOfSamples;
	private boolean multipleReadsVisible;
	private boolean errorBarsVisible;
	private int readsPlotHeight;
	private ArrayList<GraphColumn> distributionPlot;
	private int overhang;
	private int readLength;
	private ColorScheme colorScheme;
	
	
	/**
	 * Instantiates a new processing applet.
	 * 
	 * @param iWidth the width of the applet
	 * @param iHeight the height of the applet
	 */
	public ProcessingApplet(int iWidth, int iHeight, ColorScheme inputColorScheme){
		colorScheme = inputColorScheme;
		view = View.UNCOLLAPSED_UNWEIGHTED;
		width=iWidth;
		height=iHeight;
		isCodingStrand=true;
		
		//Global to all views
		hashOfIsoforms=new HashMap<String, MRNA>();
		currentlyViewingIsoforms= new ArrayList<MRNA>();
		customListOfIsoforms = new ArrayList<MRNA>();
		multipleReadsVisible=false;
		
		readsPlotToDraw = new ArrayList<ArrayList<GraphColumn>>();
		distributionPlot = new ArrayList<GraphColumn>();
		readsPlotVisible=true;
		splicingLinesVisible=true;
		gridLinesVisible=true;
		constitutiveRegionsVisible=true;
		errorBarsVisible = true;
		
		tempConstitutiveUnscaledPositions=new ArrayList<Integer>();
		customConstitutiveUnscaledPositions = new ArrayList<Integer>();	
		
		//----
		currentListOfBamCounts = new ArrayList<Integer>();
		currentListOfSampleNames = new ArrayList<String>();
		currentListOfSamples = new ArrayList<ArrayList<SAMRecord>>();
		
		graphYStart = 500;
		graphYEnd = 80;
		graphXStart = 200;
		graphXEnd = 900;
		readPlotYStart =600;
		readsPlotHeight=80;
	}
	
	/**
	 * Sets the view.
	 *
	 * @param iView the new view
	 */
	public void setView(View iView){
		view=iView;
	}
	/* (non-Javadoc)
	 * @see processing.core.PApplet#setup()
	 */
	public void setup(){
		//Subtract the title bar height
		int titleBarHeight = getBounds().y;
	    size(width, height-titleBarHeight);
	    PFont font = loadFont("Calibri-16.vlw");
	    textFont(font);
	    frameRate(30);
	    smooth();
	    yScale=5;
	    stroke(0);
	}
	
	/* (non-Javadoc)
	 * @see processing.core.PApplet#draw()
	 */
	public synchronized void draw(){
		if(mouseY<readPlotYStart&&mouseY>readPlotYStart-readsPlotHeight){
			cursor(HAND);
		}else{
			cursor(ARROW);
			
		}
		
		if(recordPDF){
			beginRecord(PDF,locationToSave);
			textFont(createFont("Arial", 16));
		}
		
		
		background(255);
		//text(frameRate,20,20);
		drawReadsPlot();
		switch(view){
			case UNCOLLAPSED_UNWEIGHTED:uncollapsed_Unweighted.draw();break;
			case UNCOLLAPSED_WEIGHTED:uncollapsed_Weighted.draw();break;
			case COLLAPSED_UNWEIGHTED:collapsed_Unweighted.draw();break;
			case COLLAPSED_WEIGHTED:collapsed_Weighted.draw();break;
		}
		drawLabelForHeight();
		
		
		if(recordPDF){
			endRecord();
			recordPDF=false;
		    textFont(loadFont("Calibri-16.vlw"));
		}
		
	}
	/**
	 * Draws a label that describes the height at each xCoordinate.
	 */
	private void drawLabelForHeight(){
		
		if(readsPlotToDraw.size()!=0){
			if(mouseX>=graphXStart && mouseX<=graphXEnd){
				text("Average Height "+ getShortReadDensityHeightAt(mouseX) + " @ "+ 
						"\n"+getAbsoluteCoordinates(mouseX), 350, 650);
				line(mouseX,readPlotYStart,mouseX,readPlotYStart+10);
				line(mouseX,readPlotYStart+10,400,620);
			}	
		}	
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
		readPlotYStart =(int) (((double)6/8)*iHeight);
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
			switch(view){
				case UNCOLLAPSED_UNWEIGHTED:{
					int indexToRemove = (mouseY+5-graphYEnd)/30;
					if(indexToRemove>=0&&indexToRemove<currentlyViewingIsoforms.size()){
						currentlyViewingIsoforms.remove(indexToRemove);
						loadArrayOfIsoforms(currentlyViewingIsoforms);
						customListOfIsoforms=currentlyViewingIsoforms;
					}
					customConstitutiveUnscaledPositions=tempConstitutiveUnscaledPositions;
					break;
				}
				case UNCOLLAPSED_WEIGHTED:break;//Do Nothing
				case COLLAPSED_UNWEIGHTED:{
					int indexToRemove = (mouseY+5-graphYEnd)/30;
					if(indexToRemove>=0&&indexToRemove<currentlyViewingIsoforms.size()){
						currentlyViewingIsoforms.remove(indexToRemove);
						loadArrayOfIsoforms(currentlyViewingIsoforms);
						customListOfIsoforms=currentlyViewingIsoforms;
					}
					customConstitutiveUnscaledPositions=tempConstitutiveUnscaledPositions;
					break;
				}
				case COLLAPSED_WEIGHTED:break;//Do Nothing
				
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see processing.core.PApplet#mouseDragged()
	 * 
	 * If the mouse is dragged while hovering over the reads plot, the reads plot is scaled
	 */
	public void mouseDragged(){
		if(mouseY<readPlotYStart&&mouseY>readPlotYStart-readsPlotHeight){
			readsPlotHeight=(readsPlotHeight+(pmouseY-mouseY));
			line(graphXStart,readPlotYStart-readsPlotHeight,graphXEnd,readPlotYStart-readsPlotHeight);
		}	
	}
	
	/* (non-Javadoc)
	 * @see processing.core.PApplet#mouseReleased()
	 * 
	 * Once the mouse has been released, the reads plot is redrawn
	 */
	public void mouseReleased(){
		if(currentListOfBamCounts.size()==currentListOfSamples.size() &&
			currentListOfSampleNames.size()==currentListOfSamples.size()){
			loadMultipleSamples(currentListOfSampleNames, currentListOfSamples, currentListOfBamCounts);		
		}else{
			loadSingleReadSample(currentListOfSampleNames.get(0), currentListOfSamples.get(0));
		}
	}
	
	/**
	 * Draw reads plot.
	 */
	private void drawReadsPlot(){
		if(readsPlotVisible){
			synchronized (readsPlotToDraw) {
				strokeCap(SQUARE);
				if(multipleReadsVisible){
					int count = 0;
					for(ArrayList<GraphColumn> sample:readsPlotToDraw){
						
						stroke(convertColor(colorScheme.getColorOf(count)));
						count++;
						for(GraphColumn column:sample){
							line(column.getScaledX(),readPlotYStart,column.getScaledX(),readPlotYStart-column.getScaledHeight());

						}
						stroke(0);
					}
					
				}else{
					for(ArrayList<GraphColumn> sample:readsPlotToDraw){
						for(GraphColumn column:sample){
							line(column.getScaledX(),readPlotYStart,column.getScaledX(),readPlotYStart-column.getScaledHeight());
							
						}
					}
				}
				
			}
			strokeCap(ROUND);
		}
		
	}
	/**
	 * Flip. Flips the isoform so that the user can see the alternate strand
	 *
	 */
	public synchronized void flip() {
		isCodingStrand=!isCodingStrand;
		switch(view){
			case UNCOLLAPSED_UNWEIGHTED:uncollapsed_Unweighted.flip(); break;
			case UNCOLLAPSED_WEIGHTED:uncollapsed_Weighted.flip();break;
			case COLLAPSED_UNWEIGHTED:collapsed_Unweighted.flip();break;
			case COLLAPSED_WEIGHTED:collapsed_Weighted.flip();break;
		}
		
		for(ArrayList<GraphColumn> sample:readsPlotToDraw){
			for(GraphColumn column:sample){
				column.setScaledX(reverse(column.getScaledX()));
			}
		}
	}	
	/**
	 * Sets the short reads visible or invisible
	 * 
	 * @param bool the boolean (true means the short reads are visible)
	 */
	public void setReadsVisible(boolean bool) {
		readsPlotVisible=bool;	
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
	 * Load array of isoforms, maintaining the strand
	 *
	 * @param listOfMRNA the list of mrna
	 */
	public void loadArrayOfIsoforms(ArrayList<MRNA> listOfMRNA){
		loadNewArrayOfIsoforms(listOfMRNA, absoluteStartOfGene, absoluteLengthOfGene, isCodingStrand);
	}
	
	/**
	 * Load new array of isoforms.
	 *
	 * @param isoforms the isoforms
	 * @param iAbsoluteStartOfGene the i absolute start of gene
	 * @param iAbsoluteLengthOfGene the i absolute length of gene
	 * @param strand the strand
	 */
	public synchronized void loadNewArrayOfIsoforms(Collection<MRNA> isoforms,int iAbsoluteStartOfGene,int 
			iAbsoluteLengthOfGene, boolean strand){
		//clear the constitutitve if you are viewing a new gene
		if(absoluteStartOfGene != iAbsoluteStartOfGene || absoluteLengthOfGene !=iAbsoluteLengthOfGene){
			clearConstitutive();
			ArrayList<MRNA> temp = new ArrayList<MRNA>(isoforms);
			absoluteStartOfGene=iAbsoluteStartOfGene;
			absoluteLengthOfGene=iAbsoluteLengthOfGene;
			absoluteEndOfGene=iAbsoluteStartOfGene+iAbsoluteLengthOfGene-1;
			isCodingStrand=strand;
			customConstitutiveUnscaledPositions=Statistics.getConstituitiveBases(temp);
																			
			customListOfIsoforms=temp;
		}		
		isCodingStrand=strand;
		
		//Identify the genomic coordinates which are constitutive for the all the isoforms
		//IT WILL CRASH WHEN U ARE VIEWING 1 and load a need isoform
		if(isoforms.size()==1){
			tempConstitutiveUnscaledPositions=Statistics.getConstituitiveBases(customListOfIsoforms);
																				
		}else{
			tempConstitutiveUnscaledPositions=Statistics.getConstituitiveBases(isoforms);
																					
		}
		
		//Make a new list of MRNA that will update the list of currently viewing isoforms
		ArrayList<MRNA> newListOfMRNA = new ArrayList<MRNA>();
		for(MRNA mrna:isoforms){
			newListOfMRNA.add(mrna);
		}
		currentlyViewingIsoforms=newListOfMRNA;
		
		
		switch(view){
			case UNCOLLAPSED_UNWEIGHTED:{
				uncollapsed_Unweighted.loadNewArrayOfUnweightedIsoforms(isoforms);
				break;
			}
			case UNCOLLAPSED_WEIGHTED:{ 
				uncollapsed_Weighted.loadNewArrayOfWeightedIsoforms(isoforms);												
				break;
			}
			case COLLAPSED_UNWEIGHTED:{ 
				collapsed_Unweighted.loadNewArrayOfUnweightedIsoforms(isoforms);
				break;
			}
			case COLLAPSED_WEIGHTED:{
				collapsed_Weighted.loadNewArrayOfWeightedIsoforms(isoforms);
				break;
			}
		}
	}
	
	/**
	 * Load single read sample.
	 *
	 * @param sampleName the sample name
	 * @param iSamRecords the i sam records
	 */
	public synchronized void loadSingleReadSample(String sampleName, ArrayList<SAMRecord> iSamRecords){
		currentListOfSamples.clear();
		currentListOfSampleNames.clear();
		currentListOfSamples.add(iSamRecords);		// Refresh the applet so that it knows what sample
		currentListOfSampleNames.add(sampleName);	// you are viewing
		
		readsPlotToDraw.clear(); 		// Clear Existing Arrays
		
		HashMap<Integer, Integer> absoluteDensityMap = Statistics.getDensityMap(absoluteStartOfGene, absoluteEndOfGene, iSamRecords);
		ArrayList<GraphColumn> samplePlot = new ArrayList<GraphColumn>(); 
		fillScaledReadsPlot(samplePlot,absoluteDensityMap,currentlyViewingIsoforms);
		readsPlotToDraw.add(samplePlot);
		
		if(!isCodingStrand){
			for(GraphColumn column:samplePlot){
				column.setScaledX(reverse(column.getScaledX()));
			}
		}
	}
	
	/**
	 * Load multiple samples.
	 *
	 * @param listOfSampleNames the list of sample names
	 * @param listOfSamples the list of samples
	 * @param listOfBamCounts the list of bam counts
	 */
	public synchronized void loadMultipleSamples(
			ArrayList<String> listOfSampleNames,
			ArrayList<ArrayList<SAMRecord> >listOfSamples,
			ArrayList<Integer> listOfBamCounts){
		
//		if(listOfSampleNames.size()==listOfSamples.size() &&
//				listOfSamples.size()==listOfBamCounts.size()){
			currentListOfSampleNames=listOfSampleNames;
			currentListOfSamples=listOfSamples;
			currentListOfBamCounts=listOfBamCounts;
			
			readsPlotToDraw.clear();
			//These three arrays must be preserved in order
			
			//Iterate through the samples and find the max height for a single base
			ArrayList<Double> maxCoverageForBaseInSample = new ArrayList<Double>();
			ArrayList<HashMap<Integer,Integer>> absoluteDensityMaps = new ArrayList<HashMap<Integer,Integer>>();
			
			for(ArrayList<SAMRecord> samRecords :listOfSamples){
				HashMap<Integer,Integer> tempHash = Statistics.getDensityMap(absoluteStartOfGene, absoluteEndOfGene, samRecords);
				absoluteDensityMaps.add(tempHash);
				double sampleMax = 0;
				for (Integer i:tempHash.values()){
					if(i.intValue()>sampleMax){
						sampleMax=i.intValue();
					}
				}
				maxCoverageForBaseInSample.add(sampleMax);
			}
			
			//Determine the largest ratio
			double largestRatio=(float) 0;
			for(int i=0;i<listOfSamples.size();i++){
				double tempRatio = maxCoverageForBaseInSample.get(i)/listOfBamCounts.get(i);
				if(tempRatio >largestRatio){
					largestRatio=tempRatio;
				} 
			}

					
			//It is acceptable to use the average of number of hits per pixel 
			//because the absolute genomic coordinate will map to it anyways.
			//Once the average is calculated it is acceptable to divide by 
			//total number of reads in bam file as order does not change where each maps to.
			
			
			//Scale each sample
			
			
			for(int i=0;i<listOfSamples.size();i++){
				ArrayList<GraphColumn> plot = new ArrayList<GraphColumn>();
				fillScaledReadsPlot(plot, absoluteDensityMaps.get(i), currentlyViewingIsoforms);
				for(GraphColumn graphColumn:plot){
					double value = graphColumn.getAverageHits()/listOfBamCounts.get(i);
					graphColumn.setScaledHeight((int)map((float)value,0,(float)largestRatio,0,readsPlotHeight));
					//If the graph is not coding, reverse it
					if(!isCodingStrand){
						graphColumn.setScaledX(reverse(graphColumn.getScaledX()));
						
					}
				}
				readsPlotToDraw.add(plot);
			}
//			return true;
//		}else{
//			System.err.println("The sizes of the three arrays are not equivalent. Please wait" +
//					"untill all sam counts are available");
//			return false;
//		}
	}
	
	/**
	 * Reload currently viewing isoforms.
	 */
	public synchronized void reloadCurrentlyViewingIsoforms(){
				loadNewArrayOfIsoforms(	currentlyViewingIsoforms,
										absoluteStartOfGene,
										absoluteLengthOfGene,
										isCodingStrand);
	}
	
	/**
	 * Load currently viewing short reads.
	 */
	public void loadCurrentlyViewingShortReads() {
		if(currentListOfSamples.size()!=0){
			if(multipleReadsVisible){
				loadMultipleSamples(currentListOfSampleNames, currentListOfSamples, currentListOfBamCounts);	
			}else{
				loadSingleReadSample(currentListOfSampleNames.get(0),currentListOfSamples.get(0));
			}
			
			
			
		}
				
	}
	
	/**
	 * Gets the custom list of isoforms.
	 *
	 * @return the custom list of isoforms
	 */
	public ArrayList<MRNA> getCustomListOfIsoforms(){
		return customListOfIsoforms;
	}
	/**
	 * Adjusts the Y Scale
	 * 
	 * @param iYScale the new y scale value (default is 5)
	 */
	public void setYScale(int iYScale){
		yScale=iYScale;
	}
	
	/**
	 * Sets the constitutive regions visible.
	 *
	 * @param bool the new constitutive regions visible
	 */
	public void setConstitutiveRegionsVisible(boolean bool) {
		constitutiveRegionsVisible=bool;
		
	}
	/**
	 * Clears all the constitutive arrays
	 */
	public synchronized void clearConstitutive() {
		collapsed_Unweighted.clear();
		collapsed_Weighted.clear();
		uncollapsed_Unweighted.clear();
		uncollapsed_Weighted.clear();
		tempConstitutiveUnscaledPositions.clear();
		customConstitutiveUnscaledPositions.clear();		
	}
	
	/**
	 * Animated load reads.
	 *
	 * @param sampleName the sample name
	 * @param newSampleReads the new sample reads
	 * @param isoform the isoform
	 */
	public synchronized void animatedLoadReads(String sampleName,ArrayList<SAMRecord> newSampleReads,MRNA isoform) {
		switch(view){
			case UNCOLLAPSED_UNWEIGHTED: loadSingleReadSample(sampleName,newSampleReads); break;//Do Nothing
			case UNCOLLAPSED_WEIGHTED: uncollapsed_Weighted.animatedLoad(this,sampleName,newSampleReads,isoform);break;
			case COLLAPSED_UNWEIGHTED: loadSingleReadSample(sampleName,newSampleReads); break;//Do Nothing
			case COLLAPSED_WEIGHTED: collapsed_Weighted.animatedLoad(this, sampleName,newSampleReads, isoform);break;
		}		
		
		
		
	}
	
	/**
	 * Change method.
	 *
	 * @param totalNumberOfReads the total number of reads
	 * @param method the method
	 * @param iOverhang the i overhang
	 * @param iReadLength the i read length
	 */
	public void changeMethod(int totalNumberOfReads,int method,int iOverhang,int iReadLength) {
		//TODO make sure that changing bam samples also calls this method... (may have to implement it in load single read)
		
		Statistics.setReadLength(readLength);
		Statistics.setTotalNumberOfReads(totalNumberOfReads);
		Statistics.setOverhang(iOverhang);
		overhang=iOverhang;
		readLength=iReadLength;
		
		if(method==0){
			Statistics.setMethod(Statistics.Method.COVERAGEPEREXON);
		}else if(method==1){
			Statistics.setMethod(Statistics.Method.RPK);
		}else if(method==2){
			Statistics.setMethod(Statistics.Method.RPKM);
		}else{
			Statistics.setMethod(Statistics.Method.COVERAGEPEREXON);
		}
	}
	
	/**
	 * Gets the RPKM of a gene
	 *
	 * @param samRecords the sam records
	 * @param totalNumberOfReads the total number of reads
	 * @return the RPKM
	 */
	public double getRPKM(ArrayList<SAMRecord> samRecords,int totalNumberOfReads) {
		return Statistics.getRPKM(samRecords, Statistics.getConstitutiveIntervals(customConstitutiveUnscaledPositions),totalNumberOfReads);
		
	}
	
	/**
	 * Gets the splice lines.
	 *
	 * @param exonsToSort the exons to sort
	 * @param yPosition the y position
	 * @return the splice lines
	 */
	private ArrayList<Line> getSpliceLines(ArrayList<Rectangle_Unweighted> exonsToSort,int yPosition){
		
		ArrayList<Line> spliceLineArray = new ArrayList<Line>();
		
		Collections.sort(exonsToSort,new RectangleComparator());
		
		//THis is sorted on the start position of the scaled xPositions;
		int scaledLastExonEnd=0;
		int scaledLastExonY=0;
		
		for(int i =0;i<exonsToSort.size();i++){
			//System.out.println(sortedRectangles.get(i).getXCoord());
			Rectangle_Unweighted rect = exonsToSort.get(i);
			
			if(i!=0){
					int midPoint = (scaledLastExonEnd+rect.getScaledXCoord())/2;
					spliceLineArray.add(new Line(scaledLastExonEnd,scaledLastExonY,midPoint,yPosition-10));	
					spliceLineArray.add(new Line(midPoint,yPosition-10,rect.getScaledXCoord(),rect.getScaledYCoord()));
			}
			scaledLastExonEnd = rect.getScaledXCoord()+rect.getScaledLength();
			scaledLastExonY=rect.getScaledYCoord();
		}
		return spliceLineArray;		
	}		
	/**
	 * Gets the short read density height at the specified xPixel
	 * 
	 * @param pixelPosition is the pixel you want the height of
	 * 
	 * @return the short read density height at
	 */
	private double getShortReadDensityHeightAt(int pixelPosition){
		if(readsPlotToDraw.size()==1){
			for(GraphColumn graphColumn:readsPlotToDraw.get(0)){
				if(graphColumn.getScaledX()==pixelPosition){
					return graphColumn.getAverageHits();	
				}
			}
			return 0; 
		}else{
			return 0;
		}
	}
	/**
	 * @param pixelPosition is the pixel you want to look up. 
	 * @return the string describes the absolute coordinates that the pixel covers
	 */
	private String getAbsoluteCoordinates(int pixelPosition){
		
		if(readsPlotToDraw.size()==1){
			for(GraphColumn graphColumn:readsPlotToDraw.get(0)){
				if(graphColumn.getScaledX()==pixelPosition){
					return graphColumn.getAbsoluteXStart() + "-" +
					graphColumn.getAbsoluteXEnd();
				}
			}
			return "Hmm Nothing Mapped Here";
		}else{
			return "Empty";
		}
	}
	
	/**
	 * Gets the rectangle nearest to x coord. (for putting constitutive rectangles at the right height)
	 *
	 * @param listOfRectangles the list of rectangles
	 * @param mrnaID the mrna id
	 * @param iXCoord the i x coord
	 * @return the rectangle nearest to x coord
	 */
	private Rectangle_Weighted getRectangleNearestToXCoord(ArrayList<Rectangle_Weighted> listOfRectangles,String mrnaID ,float iXCoord){
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
	
	/**
	 * Reverse a value
	 *
	 * @param position the position
	 * @return the int
	 */
	private int reverse(int position){
		return (graphXEnd-position)+graphXStart;
	}
	
	/**
	 * Scale absolute coord uncollapsed.
	 *
	 * @param inCoord the in coord
	 * @return the int
	 */
	private int scaleAbsoluteCoord_Uncollapsed(int inCoord){
		return mapToInt(inCoord,absoluteStartOfGene,absoluteEndOfGene,
				graphXStart,graphXEnd);
	}
	
	/**
	 * Re-maps a number from one range to another. 
	 *
	 * @param value the value
	 * @param low1 the low1
	 * @param high1 the high1
	 * @param low2 the low2
	 * @param high2 the high2
	 * @return the int
	 */
	public int mapToInt(int value, int low1, int high1, int low2, int high2){
		 
			
		double ratio = (double)(value-low1)/(high1-low1);
		double ratioLength = (double)(high2-low2)*ratio;
		double scaledPosition= low2+ratioLength;
		int temp = (int)scaledPosition;
		if(temp<low2){
			return low2;
		}else if(temp>high2){
			return high2;
		}else{
			return temp;
		}
		

	}
	
	/**
	 * Scale absolute coord_collapsed.
	 *
	 * @param value the value
	 * @param intervalsToExclude the intervals to exclude
	 * @return the int
	 */
	private int scaleAbsoluteCoord_Collapsed(int value,ArrayList<Interval> intervalsToExclude){
		//Find the sum of the lengths of the introns that are less than the position.
		int sum=0;
		int totalSum=0;
		int count=0;
		
		boolean valueIsInIntron = false;
		double relativePosition=0;
		//Go through the intervals and if the end coord of the interval is less than the value
		//then increment the count of intervals that need to be substituted
		for(Interval interval:intervalsToExclude){
			totalSum+=interval.getLength();
			if(interval.getEndCoord()<=value){
				//count how many long introns occur before this position 
				count++;
				sum+=interval.getLength();
			}
			//Ocassionally there are short reads that fall in regions that should be intronic
			if(interval.getStartCoord()<value && interval.getEndCoord()>value){
				valueIsInIntron=true;
				relativePosition=interval.getStartCoord()+
				200*((double)(value-interval.getStartCoord())/interval.getLength());//Relative position to the intron
			}
		}
		int newHigh1=(absoluteEndOfGene-totalSum)+(intervalsToExclude.size()*200);
		
		
		if(valueIsInIntron){
			int newValue = (int) ((relativePosition-sum)+(count*200));
			return mapToInt(newValue,absoluteStartOfGene,newHigh1,graphXStart,graphXEnd);
		}else{
			//All the regions longer than 500 are shortened to 200
			int newValue = (value-sum)+(count*200);
			return mapToInt(newValue,absoluteStartOfGene,newHigh1,graphXStart,graphXEnd);
		}
	}

	/**
	 * Gets the Rectangle underneath the mouse.
	 * 
	 * Iterates through the array of weightedIsoforms and checks to see if the mouse is over it.
	 * If so, it returns the rectangle.
	 * 
	 * @return a Rectangle_Weighted that is underneath the mouse coordinates. If there is none, return is null 
	 */
	private Rectangle_Weighted getRectUnderMouse(){
		switch(view){
			case UNCOLLAPSED_UNWEIGHTED:return null; //TODO
			case UNCOLLAPSED_WEIGHTED: return uncollapsed_Weighted.getRectFromCoord(mouseX,mouseY);
			case COLLAPSED_UNWEIGHTED: return null;//TODO
			case COLLAPSED_WEIGHTED: return collapsed_Weighted.getRectFromCoord(mouseX,mouseY);
			default:return null;
		}		
	}

	/**
	 * Gets the large constitutive introns.
	 *
	 * @param isoforms the isoforms
	 * @return the large constitutive introns
	 */
	private ArrayList<Interval> getLargeConstitutiveIntrons(
			Collection<MRNA> isoforms) {
		//Make a hashmap that represents all the positions available on the gene
		HashMap<Integer, Boolean> exonMap = new HashMap<Integer, Boolean>();
		for(int i = absoluteStartOfGene;i<=absoluteEndOfGene;i++){
			exonMap.put(i, false);
		}
		
		
		//Get a feel for the span of the exons by plotting them
		for(MRNA mrna:isoforms){
			for(Exon exon:mrna.getExons().values()){
				for(int i = exon.getStart();i<=exon.getEnd();i++){
					exonMap.put(i, true);
				}
			}
		}
		//Get the introns by looking for consecutive stretches of false
		ArrayList<Interval> listOfIntrons = new ArrayList<Interval>();
		int length =-1;
		int lastFalse=-1;
		for(int i = absoluteStartOfGene;i<=absoluteEndOfGene;i++){
			if(exonMap.get(i)==false){
				if(lastFalse == -1){
					lastFalse=i;
					length=1;
				}else{
					length++;
				}
			}else if(exonMap.get(i)==true && lastFalse!=-1){
				listOfIntrons.add(new Interval(lastFalse,length));
				lastFalse=-1;
				length=-1;
			}
		}
		if(lastFalse!=-1){
			listOfIntrons.add(new Interval(lastFalse,length));
		}
		
		//Remove all the introns that are smaller than 500
		for(Iterator<Interval> itr = listOfIntrons.iterator();itr.hasNext();){
			Interval interval = itr.next();
			if(interval.getLength()<500){
				itr.remove();
			}
		}
		return listOfIntrons;
	}
	
	/**
	 * Fill scaled reads plot.
	 *
	 * @param readsSample the reads sample
	 * @param densityMap the density map
	 * @param isoforms the isoforms
	 */
	private synchronized void fillScaledReadsPlot(ArrayList<GraphColumn> readsSample,
				HashMap<Integer,Integer> densityMap,ArrayList<MRNA> isoforms){
			
			//Get a list of the large consitutitive introns
			ArrayList<Interval> listOfIntrons = getLargeConstitutiveIntrons(isoforms);
					
			int prevPixel = -1;
			int currentSum=-1;
			int numberOfBases=-1;
			int frameAbsoluteStart=-1;
			int frameAbsoluteEnd = -1;
			
			//Goes through each of the genomic coordinates and attempts to map it to a pixel
			//In the end, a pixel represents the average number of short reads that cross it
			float maxAverage =0;
			for(int i = absoluteStartOfGene;i<=absoluteEndOfGene;i++){
				int mappedPixel;
				switch(view){
					case COLLAPSED_UNWEIGHTED:	mappedPixel= scaleAbsoluteCoord_Collapsed(i,listOfIntrons);break;
					case COLLAPSED_WEIGHTED:	mappedPixel= scaleAbsoluteCoord_Collapsed(i,listOfIntrons);break;
					case UNCOLLAPSED_UNWEIGHTED:mappedPixel= scaleAbsoluteCoord_Uncollapsed(i);break;
					case UNCOLLAPSED_WEIGHTED:	mappedPixel= scaleAbsoluteCoord_Uncollapsed(i);break;
					default: mappedPixel= scaleAbsoluteCoord_Uncollapsed(i);break;
				}
				if(mappedPixel<graphXStart||mappedPixel>graphXEnd){
					System.err.println(mappedPixel);
					return;
				}
				//System.out.println(i + "-->" + mappedPixel);// +"---Abs" + iAbsoluteDensity.get(i));
				if(prevPixel==-1){
					frameAbsoluteStart=i;
					frameAbsoluteEnd=i;
					numberOfBases=1;
					currentSum=densityMap.get(i);
					prevPixel=mappedPixel;
				}else if(i==absoluteEndOfGene){
					float average = (float)currentSum/numberOfBases;
					if(average>maxAverage){
						maxAverage=average;
					}
					frameAbsoluteEnd=i;
					numberOfBases++;
					readsSample.add(new GraphColumn(prevPixel, average, frameAbsoluteStart, frameAbsoluteEnd));
				}else if(mappedPixel!= prevPixel){
					float average = (float)currentSum/numberOfBases;
					if(average>maxAverage){
						maxAverage=average;
					}
					readsSample.add(new GraphColumn(prevPixel, average, frameAbsoluteStart, frameAbsoluteEnd));
					
					prevPixel=mappedPixel;
					numberOfBases=1;
					currentSum=densityMap.get(i);
					frameAbsoluteStart=i;
					frameAbsoluteEnd=i;	
				}else{	
					currentSum+=densityMap.get(i);
					frameAbsoluteEnd=i;
					numberOfBases++;
				} 
	//			System.out.println(i + "----"  + iAbsoluteDensity.get(i));
			}
			for(GraphColumn column:readsSample){
				column.setScaledHeight((int)map((float)column.getAverageHits(),0,maxAverage,0,readsPlotHeight));
			}
		}
	public synchronized void printPDF(String string) {
		locationToSave=string;
		recordPDF =true;
		
	}
	
	/**
	 * Sets the multiple reads visible.
	 *
	 * @param bool the new multiple reads visible
	 */
	public void setMultipleReadsVisible(boolean bool){
		multipleReadsVisible=bool;
	}
	
	/**
	 * Load distribution. //TODO INCOMPLETE!
	 *
	 * @param distributionArrayList the distribution array list
	 */
	public synchronized void loadDistribution(ArrayList<Integer> distributionArrayList) {
		Iterator<Integer> itr = distributionArrayList.iterator();
		distributionPlot.clear();
		int largestSize = distributionArrayList.get(distributionArrayList.size()-1).intValue();
		int maxHits = 0;
		
		int prevPixel = -1;
		int numberOfHits=-1;
		int frameAbsoluteStart=-1;
		int frameAbsoluteEnd = -1;
		
		while(itr.hasNext()){
			
			
			int length = itr.next().intValue();
			//System.out.println(length);
			int mappedPixel = mapToInt(length,0,largestSize,graphXStart,graphXEnd);
			//System.out.println(mappedPixel);
				
			//Goes through each of the genomic coordinates and attempts to map it to a pixel
			//In the end, a pixel represents the average number of short reads that cross it
			
			if(prevPixel==-1){
				frameAbsoluteStart=length;
				frameAbsoluteEnd=length;
				numberOfHits=1;
				prevPixel=mappedPixel;
			}else if(itr.equals(distributionArrayList.get(distributionArrayList.size()-1))){//Is this the end?
				if(numberOfHits>maxHits){
					maxHits=numberOfHits;
				}
				System.out.println("THE END");
				frameAbsoluteEnd=length;
				numberOfHits++;
				distributionPlot.add(new GraphColumn(prevPixel, numberOfHits, frameAbsoluteStart, frameAbsoluteEnd));
			}else if(mappedPixel!= prevPixel){
				if(numberOfHits>maxHits){
					maxHits=numberOfHits;
				}
				distributionPlot.add(new GraphColumn(prevPixel, numberOfHits, frameAbsoluteStart, frameAbsoluteEnd));
				prevPixel=mappedPixel;
				numberOfHits=1;
				frameAbsoluteStart=length;
				frameAbsoluteEnd=length;	
			}else{	
				numberOfHits++;
				frameAbsoluteEnd=length;
			} 
			
		}
		for(GraphColumn column:distributionPlot){
			column.setScaledHeight((int)map((float)column.getAverageHits(),0,maxHits,0,readsPlotHeight));
		}
		System.out.println(distributionPlot.size());
	}
	
	/**
	 * Sets the error bars visible.
	 *
	 * @param bool the new error bars visible
	 */
	public void setErrorBarsVisible(boolean bool) {
		errorBarsVisible=bool;		
	}
	
	/**
	 * Convert color.
	 *
	 * @param color the color(AWT)
	 * @return the int(Processing)
	 */
	public int convertColor(Color color){
		return color(color.getRed(),color.getGreen(),color.getBlue());
	}
}
