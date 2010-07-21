package genomeBrowser;

import genomeBrowser.ShortRead;
import genomeBrowser.Statistics;

import gffParser.CDS;
import gffParser.Exon;
import gffParser.MRNA;


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

public class ProcessingApplet extends PApplet{
	public enum View{
		COLLAPSED_UNWEIGHTED,
		COLLAPSED_WEIGHTED,
		UNCOLLAPSED_WEIGHTED,
		UNCOLLAPSED_UNWEIGHTED
	}
	
	private abstract class Weighted{
			protected ArrayList<Junction> junctionList;
			protected ArrayList<Rectangle_Weighted> weightedIsoforms;
			protected ArrayList<Rectangle_Weighted> weightedConstitutiveRectangles;
			protected ArrayList<ErrorBar> errorBars;
			protected ArrayList<Rectangle_Unweighted>referenceIsoforms;
			protected ArrayList<Label> referenceLabels;
			
			public void animatedLoad(ProcessingApplet pApplet,ArrayList<SAMRecord> newShortReads, MRNA isoform) {
				junctionList.clear();
				errorBars.clear();
				weightedConstitutiveRectangles.clear();
				loadShortReads(newShortReads);
				ArrayList<ShortRead> compatibleShortReads = Statistics.getCompatibleShortReads(isoform, newShortReads);
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
					currentAnimation = new Thread(new UpdateAnimation(pApplet,isoform,compatibleShortReads,weightedIsoforms));
					currentAnimation.start();	
				}	
				
			}
	
			public synchronized void clear() {
				weightedConstitutiveRectangles.clear();
				
			}
	
			public synchronized void draw(){
				drawWeightedIsoforms();
			}
	
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

			public synchronized void loadNewArrayOfWeightedIsoforms(Collection<MRNA> listOfMRNA){
			
				//Under the condition that the loadShortReads function has already been called
				if(geneSAMRecords!=null){
					//Make new arrays
					weightedIsoforms.clear(); 
					referenceIsoforms.clear();
					referenceLabels.clear();
					errorBars.clear();
					weightedConstitutiveRectangles.clear();
					junctionList.clear();
					
					ArrayList<Interval> listOfConstitutiveIntervals = getConstitutiveIntervals(tempConstitutiveUnscaledPositions);
					
					//Get a list of the large consitutitive exons
					ArrayList<Interval> listOfIntrons = getLargeConstitutiveIntrons(listOfMRNA);
					
					
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
							fillJunctionList(junctionList,mrna);
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
							float average = Statistics.getWeightOfExon(exon,compatibleShortReads,endExon);
							exonCountPosition++;
							
							//Scale the length and start
							float lengthScaled= scaleLength(exon.getLength(), listOfIntrons);
							float startScaled = scalePosition(exon.getStart(), listOfIntrons); 
							
							Rectangle_Weighted temp = new Rectangle_Weighted(startScaled, lengthScaled,10,
									exon.getStart(),exon.getEnd(),mrna.getId(),average,exonColor);
							weightedIsoforms.add(temp);
							errorBars.add(new ErrorBar(average,
									Statistics.getStandardDeviation(exon.getStart(),exon.getEnd(),compatibleShortReads),
									temp,mrna.getId(),(startScaled+lengthScaled/2)));
							
							//Fill in array that draws the reference isoforms
							Rectangle_Unweighted temp2 = new Rectangle_Unweighted(startScaled, yRefStart, lengthScaled,10,exon.getStart(),
									exon.getEnd(),mrna.getId(),exonColor);
							referenceIsoforms.add(temp2);
							
			
							//Fill in array that draws the scaled constitutive lines
							//The difference here is that that the first YStartPosition represents the absolute height
							//It still needs to be scaled (look at drawWeightedConstitutiveLines)
							for(Interval interval :listOfConstitutiveIntervals){
								if(interval.getStartCoord()>=exon.getStart()&&interval.getEndCoord()<=exon.getEnd()){
									int color = color(0,0,255);
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
							float lengthScaled= scaleLength(cds.getLength(),listOfIntrons);
							float startScaled = scalePosition(cds.getStart(), listOfIntrons);
							
							//Get the rectangle that is nearest to the xCoordinate who has the same MRNA
							//to get the appropriate height
							Rectangle_Weighted exon_Rect=getRectangleNearestToXCoord(weightedIsoforms,mrna.getId(),startScaled);
							float weight = exon_Rect.getWeight();
							Rectangle_Weighted temp = new Rectangle_Weighted(startScaled,lengthScaled,20,
									cds.getStart(),cds.getEnd(),mrna.getId(),weight,cdsColor);
							weightedIsoforms.add(temp);
							Rectangle_Unweighted temp2 = new Rectangle_Unweighted(startScaled, yRefStart-5, lengthScaled,20,
									cds.getStart(),cds.getEnd(),mrna.getId(),cdsColor);
							referenceIsoforms.add(temp2);
							
							//Fill in array that draws the scaled constitutive lines
							//The difference here is that that the first YStartPosition represents the absolute height
							//It still needs to be scaled (look at drawWeightedConstitutiveLines)
							for(Interval interval :listOfConstitutiveIntervals){
								if(interval.getStartCoord()>=cds.getStart()&&interval.getEndCoord()<=cds.getEnd()){
									int color = color(0,0,255);
									weightedConstitutiveRectangles.add(new Rectangle_Weighted(	scalePosition(interval.getStartCoord(),listOfIntrons), 
																								scaleLength(interval.getLength(),listOfIntrons),
																								20, 
																								interval.getStartCoord(), 
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
					System.err.println("A call was made to loadWeightedIsoforms but there was no shortReadData");
				}
				
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
	
			private void drawHoverInfo(){
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
	
			private void drawJunctionLines() {
						if(junctionList!=null){
							synchronized (junctionList) {
								stroke(255,0,0);
								strokeWeight(3);
								for(Junction junction:junctionList){
									int yPos=graphYStart;
			//						for(int i=0;i<junction.getHits();i++){
			//							line(junction.getLeftScaled(),yPos,junction.getRightScaled(),yPos);
			//							yPos-=yScale;
			//						}
									int height = (int) (yPos-(yScale*junction.getWeight()));
									line(junction.getLeftScaled(),height,junction.getRightScaled(),height);
									text(junction.getWeight(),junction.getRightScaled(),(int)height);
								}
								strokeWeight(1);
								stroke(1);
							}
						}
						
						
					}
	
			/**
			 * Draws a label that describes the height at each xCoordinate.
			 */
			//FIXME should contain info about both sets of short read
			private void drawLabelForHeight(){
				if(shortReads_Set_U1!=null){
					if(mouseX>=graphXStart && mouseX<=graphXEnd){
						text("Average Height "+ getShortReadDensityHeightAt(mouseX) + " @ "+ 
								"\n"+getAbsoluteCoordinates(mouseX), 350, 650);
						line(mouseX,shortReadPlotYStart,400,620);
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
	
			private void drawWeightedConstitutiveRectangles(){
				if(constitutiveRegionsVisible){
					synchronized (weightedConstitutiveRectangles) {
							for(Rectangle_Weighted rectangle:weightedConstitutiveRectangles){
								fill(rectangle.getColor());
								float newScaledYCoord = graphYStart-(yScale*rectangle.getWeight()); 
									//graphYStart-((graphYStart-rectangle.getScaledYCoord())*yScale);
								//Because the rectangle is drawn from the corner, subtract half its height;
								newScaledYCoord=newScaledYCoord-(rectangle.getScaledHeight()/2);
								colorMode(HSB, 450,100,50);
								rect(rectangle.getScaledXCoord(),newScaledYCoord,rectangle.getScaledLength(),rectangle.getScaledHeight());
								colorMode(RGB,255);
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
				drawWeightedConstitutiveRectangles();
				drawJunctionLines();
				drawLabelForHeight();
				drawHoverInfo();
			}
	
			private void fillJunctionList(ArrayList<Junction> iJunctionList,MRNA isoform) {
				iJunctionList.clear();
				ArrayList<MRNA> temp = new ArrayList<MRNA>();
				temp.add(isoform);
				ArrayList<Interval> listOfIntrons = getLargeConstitutiveIntrons(temp);
				
				for(ShortRead shortRead:Statistics.getCompatibleShortReads(isoform, geneSAMRecords)){
					if(shortRead.isJunctionRead()){
						float startScaled = scalePosition(shortRead.getFirstExonEnd()+1,listOfIntrons);
						float endScaled = scalePosition(shortRead.getLastExonBeginning()-1,listOfIntrons);				
						
						boolean found =false;
						for(Junction junction:iJunctionList){
							if(junction.getLeftScaled()==startScaled && junction.getRightScaled()==endScaled){
								junction.incrementCount(1);
								found=true;
							}					
						}
						if(!found){
							iJunctionList.add(new Junction(startScaled,endScaled,1));
						}		
					}	
				}
				for(Junction junction:iJunctionList){
					junction.setWeight(Statistics.getWeightOfJunction(junction));
				}
			}
			protected abstract float scaleLength(float length,ArrayList<Interval> listOfIntrons);
			protected abstract float scalePosition(float position,ArrayList<Interval> listOfIntrons);
			
	//		protected boolean isARepeat(Rectangle_Weighted inputRect){
	//			for(Rectangle_Weighted rect:weightedIsoforms){
	//				float lengthScaled= map(exon.getLength(),0,absoluteLengthOfGene-sum+(listOfIntrons.size()*200),0,graphXEnd-graphXStart);
			//float startScaled = scaleAbsoluteCoord_Collapsed(exon.getStart(), listOfIntrons);
	//			}
	//		}
		}
	private abstract class Unweighted{
			protected ArrayList<Rectangle_Unweighted> unweightedIsoforms;
			protected ArrayList<Rectangle_Unweighted> unweightedConstitutiveRectangle;
			protected ArrayList<Line> spliceLines;
			protected ArrayList<Label> unweightedIsoformLabels;
			
			public synchronized void draw() {
				drawUnweightedIsoforms();
			}
			public synchronized void clear() {
				unweightedConstitutiveRectangle.clear();	
			}
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
			private void drawUnweightedIsoformLabels() {
				synchronized (unweightedIsoformLabels) {
					for(Label label: unweightedIsoformLabels){
						text(label.getText(),label.getXScaled(),label.getYScaled());
					}	
				}
			}
	public synchronized void loadNewArrayOfUnweightedIsoforms(Collection<MRNA> isoforms){
				
				//Clear Existing Arrays
				unweightedIsoforms.clear(); 
				spliceLines.clear();
				unweightedIsoformLabels.clear(); 
				unweightedConstitutiveRectangle.clear(); 
				
				ArrayList<Interval> listOfConstitutiveIntervals = getConstitutiveIntervals(tempConstitutiveUnscaledPositions);
				
				
				//Get a list of the large consitutitive exons
				ArrayList<Interval> listOfIntrons = getLargeConstitutiveIntrons(isoforms);
				
				//Plot the shortened isoforms
				int yPosition=graphYEnd;
				
				for(MRNA mrna : isoforms){
					ArrayList<Rectangle_Unweighted> sortedRectangles = new ArrayList<Rectangle_Unweighted>();
					
					// Exons
					int cdsColor = color(150);
					int exonColor = color(40);
					for(Exon exon:mrna.getExons().values()){
						float scaledLength= scaleLength(exon.getLength(),listOfIntrons);
						float scaledStart = scalePosition(exon.getStart(), listOfIntrons);
						 
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
								int color = color(0,0,255);
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
						float scaledLength= scaleLength(cds.getLength(),listOfIntrons);
						float scaledStart = scalePosition(cds.getStart(),listOfIntrons);
						
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
							if(interval.getStartCoord()>=cds.getStart()&&interval.getEndCoord()<=cds.getEnd()){
								int color = color(0,0,255);
								unweightedConstitutiveRectangle.add(
										new Rectangle_Unweighted(	scalePosition(interval.getStartCoord(),listOfIntrons),
																	yPosition, 
																	scaleLength(interval.getLength(),listOfIntrons), 
																	20, 
																	interval.getStartCoord(), 
																	interval.getEndCoord(), 
																	mrna.getId(), 
																	color));
							}
						}
					}
					
					//Make Splice lines
					spliceLines.addAll(getSpliceLines(sortedRectangles, yPosition));
					
					
					//Load Start and Stop Codons
					int color = color(0,255,0, 100);//Green
					if(mrna.getStartCodon()!=null){
						float scaledLength= scaleLength(mrna.getStartCodon().getLength(),listOfIntrons);
						float scaledStart = scalePosition(mrna.getStartCodon().getStart(),listOfIntrons);
						
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
						float scaledLength= scaleLength(mrna.getStopCodon().getLength(),listOfIntrons);
						float scaledStart = scalePosition(mrna.getStopCodon().getStart(),listOfIntrons);
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
			protected abstract float scaleLength(float length,ArrayList<Interval> listOfIntrons);
			protected abstract float scalePosition(float position,ArrayList<Interval> listOfIntrons);
		}
	private class Uncollapsed_Weighted extends Weighted{
		
		public Uncollapsed_Weighted() {
			weightedIsoforms=new ArrayList<Rectangle_Weighted>();
			weightedConstitutiveRectangles = new ArrayList<Rectangle_Weighted>();
			referenceIsoforms = new ArrayList<Rectangle_Unweighted>();
			referenceLabels= new ArrayList<Label>();
			errorBars = new ArrayList<ErrorBar>();
			junctionList = new ArrayList<Junction>();
		}
	
		@Override
		protected float scaleLength(float length,
				ArrayList<Interval> listOfIntrons) {
			return map(length,0,absoluteLengthOfGene,0,graphXEnd-graphXStart);
		}
	
		@Override
		protected float scalePosition(float position,
				ArrayList<Interval> listOfIntrons) {
			return scaleAbsoluteCoord_Uncollapsed(position);
		}
	}
	private class Collapsed_Weighted extends Weighted{
		public Collapsed_Weighted() {
			weightedIsoforms=new ArrayList<Rectangle_Weighted>();
			weightedConstitutiveRectangles = new ArrayList<Rectangle_Weighted>();
			referenceIsoforms = new ArrayList<Rectangle_Unweighted>();
			referenceLabels= new ArrayList<Label>();
			errorBars = new ArrayList<ErrorBar>();
			junctionList = new ArrayList<Junction>();
		}
		@Override
		protected float scaleLength(float length,
				ArrayList<Interval> listOfIntrons) {
			int sum=0;
			for(Interval interval:listOfIntrons){
				sum+=interval.getLength();
			}	
			return map(length,0,absoluteLengthOfGene-sum+(listOfIntrons.size()*200),0,graphXEnd-graphXStart);
		}
	
		@Override
		protected float scalePosition(float position,
				ArrayList<Interval> listOfIntrons) {
			return scaleAbsoluteCoord_Collapsed(position, listOfIntrons);
		}
	}
	private class Collapsed_Unweighted extends Unweighted{
	
		public Collapsed_Unweighted(){
			unweightedConstitutiveRectangle = new ArrayList<Rectangle_Unweighted>();
			spliceLines=new ArrayList<Line>();
			unweightedIsoforms=new ArrayList<Rectangle_Unweighted>();
			unweightedIsoformLabels = new ArrayList<Label>();
		}
		
		
	
		@Override
		protected float scaleLength(float length,
				ArrayList<Interval> listOfIntrons) {
			int sum=0;
			for(Interval interval:listOfIntrons){
				sum+=interval.getLength();
			}
			return map(length,0,absoluteLengthOfGene-sum+(listOfIntrons.size()*200),0,graphXEnd-graphXStart);
		}
	
		@Override
		protected float scalePosition(float position,
				ArrayList<Interval> listOfIntrons) {
			return scaleAbsoluteCoord_Collapsed(position, listOfIntrons);
		}
	}
	private class Uncollapsed_Unweighted extends Unweighted{
		
		public Uncollapsed_Unweighted(){
			spliceLines = new ArrayList<Line>();
			unweightedIsoforms=new ArrayList<Rectangle_Unweighted>();
			unweightedConstitutiveRectangle=new ArrayList<Rectangle_Unweighted>();
			unweightedIsoformLabels = new ArrayList<Label>();
			
		}
			
		@Override
		protected float scaleLength(float length,
				ArrayList<Interval> listOfIntrons) {
			return map(length,0,absoluteLengthOfGene,0,graphXEnd-graphXStart);
		}
		@Override
		protected float scalePosition(float position,
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
	
	private boolean shortReadsPlotVisible; 
	private boolean isCodingStrand;
	private boolean splicingLinesVisible;
	private boolean gridLinesVisible;
	
	private ArrayList<MRNA> currentlyViewingIsoforms;
	private ArrayList<MRNA> customListOfIsoforms;
	
	private ArrayList<Integer> tempConstitutiveUnscaledPositions,customConstitutiveUnscaledPositions;
	private HashMap<Integer, GraphColumn> shortReads_Set_U2;
	private HashMap<Integer,GraphColumn>shortReads_Set_U1;
	private HashMap<Integer,GraphColumn>shortReads_Set_C1;
	private HashMap<Integer,GraphColumn>shortReads_Set_C2;
	
	private int yScale;
	private int graphYStart,graphXEnd,graphXStart,graphYEnd,shortReadPlotYStart;
	
	
	//Data Specific to GENE (Viwer can at most view one gene at a time)
	private int absoluteStartOfGene;
	private int absoluteLengthOfGene;
	private int absoluteEndOfGene;
	private ArrayList<SAMRecord> geneSAMRecords; //Different per gene
	private Thread currentAnimation;
	private HashMap<String,MRNA> hashOfIsoforms; //Key is the MRNA ID
	private View view;
	private boolean constitutiveRegionsVisible;
	
	/**
	 * Instantiates a new processing applet.
	 * 
	 * @param iWidth the width of the applet
	 * @param iHeight the height of the applet
	 */
	public ProcessingApplet(int iWidth, int iHeight){
		view = View.UNCOLLAPSED_UNWEIGHTED;
		width=iWidth;
		height=iHeight;
		isCodingStrand=true;
		
		//Global to all views
		hashOfIsoforms=new HashMap<String, MRNA>();
		currentlyViewingIsoforms= new ArrayList<MRNA>();
		customListOfIsoforms = new ArrayList<MRNA>();
		shortReads_Set_U1 = new HashMap<Integer, GraphColumn>();
		shortReads_Set_U2 = new HashMap<Integer,GraphColumn>();
		shortReads_Set_C1 = new HashMap<Integer, GraphColumn>();
		shortReads_Set_C2 = new HashMap<Integer,GraphColumn>();
		shortReadsPlotVisible=true;
		splicingLinesVisible=true;
		gridLinesVisible=true;
		constitutiveRegionsVisible=true;
		
		tempConstitutiveUnscaledPositions=new ArrayList<Integer>();
		customConstitutiveUnscaledPositions = new ArrayList<Integer>();
		
		//----
		
		
		geneSAMRecords=null;
		
		graphYStart = 500;
		graphYEnd = 80;
		graphXStart = 200;
		graphXEnd = 900;
		shortReadPlotYStart =600;
	}
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
		background(255);
		text(frameRate,20,20);
			switch(view){
			case UNCOLLAPSED_UNWEIGHTED:uncollapsed_Unweighted.draw();drawUncollapsed_ShortReads();break;
			case UNCOLLAPSED_WEIGHTED:uncollapsed_Weighted.draw();drawUncollapsed_ShortReads();break;
			case COLLAPSED_UNWEIGHTED:collapsed_Unweighted.draw();drawCollapsed_ShortReads();break;
			case COLLAPSED_WEIGHTED:collapsed_Weighted.draw();drawCollapsed_ShortReads();break;
		}	
	}

	;
	
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
	/**
	 * Draws the short read plot under the condition that densityPlot is not null and shortReadsVisible is true.
	 */
	private void drawUncollapsed_ShortReads(){
		if(shortReadsPlotVisible && shortReads_Set_U2!=null&&shortReads_Set_U1!=null){
			synchronized (shortReads_Set_U1) {
				if(shortReads_Set_U1.size()!=0){
					text("Min",graphXStart-25,shortReadPlotYStart);
					text("Max",graphXStart-25,shortReadPlotYStart-75);	
				}
				if(shortReads_Set_U2.size()==0){
					stroke(0,100);	
				}else{
					stroke(255,0,0,100);
				}
				for(GraphColumn column:shortReads_Set_U1.values()){
					line(column.getScaledX(),shortReadPlotYStart,column.getScaledX(),shortReadPlotYStart-column.getScaledHeight());
				}	
			}
			synchronized (shortReads_Set_U2) {
				if(shortReads_Set_U2.size()!=0){
					text("Min",graphXEnd,shortReadPlotYStart);
					text("Max",graphXEnd,shortReadPlotYStart-75);
				}
				stroke(0,255,0,100);
				for(GraphColumn column:shortReads_Set_U2.values()){
					line(column.getScaledX(),shortReadPlotYStart,column.getScaledX(),shortReadPlotYStart-column.getScaledHeight());
				}
			}
			stroke(0);
		}
	}
	private void drawCollapsed_ShortReads() {
		if(shortReadsPlotVisible){
			synchronized (shortReads_Set_C1) {
				if(shortReads_Set_C1.size()!=0){
					text("Min",graphXStart-25,shortReadPlotYStart);
					text("Max",graphXStart-25,shortReadPlotYStart-75);	
				}
				//Color depending on whether or not ShortReads_Set_C2 exists
				if(shortReads_Set_C2.size()==0){
					stroke(0,100);	
				}else{
					stroke(255,0,0,100);
				}
				
				for(GraphColumn column:shortReads_Set_C1.values()){
					line(column.getScaledX(),shortReadPlotYStart,column.getScaledX(),shortReadPlotYStart-column.getScaledHeight());
				}	
			}
			synchronized (shortReads_Set_C2) {
				if(shortReads_Set_C2.size()!=0){
					text("Min",graphXEnd,shortReadPlotYStart);
					text("Max",graphXEnd,shortReadPlotYStart-75);
				}
				stroke(0,255,0,100);
				for(GraphColumn column:shortReads_Set_C2.values()){
					line(column.getScaledX(),shortReadPlotYStart,column.getScaledX(),shortReadPlotYStart-column.getScaledHeight());
				}
			}
			stroke(0);
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
		for(GraphColumn column:shortReads_Set_U1.values()){
			column.setScaledX(reverse(column.getScaledX()));
		}
		for(GraphColumn column:shortReads_Set_U2.values()){
			column.setScaledX(reverse(column.getScaledX()));
		}
		for(GraphColumn column:shortReads_Set_C1.values()){
			column.setScaledX(reverse(column.getScaledX()));
		}
		for(GraphColumn column:shortReads_Set_C2.values()){
			column.setScaledX(reverse(column.getScaledX()));
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
	 * Sets the splice lines visible or invisible
	 * 
	 * @param bool the boolean (true means that the splicelines are visible)
	 */
	public void setSpliceLinesVisible(boolean bool) {
		splicingLinesVisible=bool;
	}
	public void loadArrayOfIsoforms(ArrayList<MRNA> listOfMRNA){
		loadNewArrayOfIsoforms(	listOfMRNA, absoluteStartOfGene, absoluteLengthOfGene, isCodingStrand);
	}
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
			customConstitutiveUnscaledPositions=identifyConstituitiveBases(temp);
			customListOfIsoforms=temp;
		}		
		isCodingStrand=strand;
		
		//Identify the genomic coordinates which are constitutive for the all the isoforms
		//IT WILL CRASH WHEN U ARE VIEWING 1 and load a need isoform
		if(isoforms.size()==1){
			tempConstitutiveUnscaledPositions=identifyConstituitiveBases(customListOfIsoforms);
		}else{
			tempConstitutiveUnscaledPositions=identifyConstituitiveBases(isoforms);	
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
	public synchronized void loadShortReads(ArrayList<SAMRecord> iSamRecords){
		geneSAMRecords = iSamRecords;
		switch(view){
			case UNCOLLAPSED_UNWEIGHTED:loadUncollapsed_ShortReads(iSamRecords);break;
			case UNCOLLAPSED_WEIGHTED:loadUncollapsed_ShortReads(iSamRecords);break;
			case COLLAPSED_UNWEIGHTED:loadCollapsed_ShortReads(iSamRecords);break;
			case COLLAPSED_WEIGHTED:loadCollapsed_ShortReads(iSamRecords);break;
		}
	}
	
	public synchronized void loadUncollapsed_TwoShortReadSamples(ArrayList<SAMRecord> sample1,int sample1Size,ArrayList<SAMRecord> sample2,int sample2Size){
		HashMap<Integer, Integer> sample1Map=Statistics.getDensityMap(absoluteStartOfGene, absoluteEndOfGene, sample1);
		HashMap<Integer, Integer> sample2Map=Statistics.getDensityMap(absoluteStartOfGene, absoluteEndOfGene, sample2);
		
		//Iterate through both samples and find the max for each
		float sample1Max=0;
		for (Integer i:sample1Map.values()){
			if(i.intValue()>sample1Max){
				sample1Max=i.intValue();
			}
		}
		float sample2Max=0;
		for (Integer i:sample2Map.values()){
			if(i.intValue()>sample2Max){
				sample2Max=i.intValue();
			}
		}
		
		//Determine the largest ratio
		float largestRatio=0;
		if((sample2Max/sample2Size) > (sample1Max/sample1Size)){
			largestRatio=sample2Max/sample2Size;
		}else{
			largestRatio=sample1Max/sample1Size;
		}
		
		//It is acceptable to use the average of number of hits per pixel 
		//because the absolute genomic coordinate will map to it anyways.
		//Once the average is calculated it is acceptable to divide by 
		//total number of reads in bam file as order does not change where each maps to.
		
		//Scale sample1 plot
		fillUncollapsed_ScaledShortReadsPlot(shortReads_Set_U1, sample1Map);
		for(GraphColumn graphColumn:shortReads_Set_U1.values()){
			float value = graphColumn.getAverageHits()/sample1Size;
			System.out.println(value);
			graphColumn.setScaledHeight(map(value,0,largestRatio,0,80));	
		}
		//Scale sample2 plot
		fillUncollapsed_ScaledShortReadsPlot(shortReads_Set_U2, sample1Map);
		for(GraphColumn graphColumn:shortReads_Set_U2.values()){
			float value = graphColumn.getAverageHits()/sample2Size;
			System.out.println(value);
			graphColumn.setScaledHeight(map(value,0,largestRatio,0,80));	
		}
		
		//If the graph is not coding, reverse it
		if(!isCodingStrand){
			for(GraphColumn column:shortReads_Set_U1.values()){
				column.setScaledX(reverse(column.getScaledX()));
			}
			for(GraphColumn column:shortReads_Set_U2.values()){
				column.setScaledX(reverse(column.getScaledX()));
			}
		}
	}
	public void loadCollapsed_ShortReads(ArrayList<SAMRecord> iSamRecords){
		HashMap<Integer, Integer> absoluteDensityMap = Statistics.getDensityMap(absoluteStartOfGene, absoluteEndOfGene, geneSAMRecords);
		//TODO - NOt sure if this is okay (currentlyViewingIsoforms) 
		//It would mean that the gene must be loaded before the bam
		fillCollapsed_ScaledShortReadsPlot(shortReads_Set_C1,absoluteDensityMap,currentlyViewingIsoforms);
		if(!isCodingStrand){
			for(GraphColumn column:shortReads_Set_C1.values()){
				column.setScaledX(reverse(column.getScaledX()));
			}
		}
	}
	public synchronized void loadCurrentlyViewingIsoforms(){
				loadNewArrayOfIsoforms(	currentlyViewingIsoforms,
										absoluteStartOfGene,
										absoluteLengthOfGene,
										isCodingStrand);
	}
	public void loadCurrentlyViewingShortReads() {
		if(geneSAMRecords!=null){
			loadShortReads(geneSAMRecords);	
		}
				
	}
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
	 * Loads Short Read data but also animates the exons so that they float in the direction of the
	 * new short reads.
	 *
	 * @param newShortReads the new short reads
	 * @param isoform the isoform you want to see animated
	 */
	public synchronized void animatedLoadShortReads(ArrayList<SAMRecord> newShortReads,MRNA isoform) {
		switch(view){
			case UNCOLLAPSED_UNWEIGHTED: loadShortReads(newShortReads); break;//Do Nothing
			case UNCOLLAPSED_WEIGHTED: uncollapsed_Weighted.animatedLoad(this,newShortReads,isoform);break;
			case COLLAPSED_UNWEIGHTED: loadShortReads(newShortReads); break;//Do Nothing
			case COLLAPSED_WEIGHTED:collapsed_Weighted.animatedLoad(this, newShortReads, isoform);break;
		}		
		
		
		
	}
	public void changeMethod(int totalNumberOfReads,int method,int overhang) {
		
		Statistics.setTotalNumberOfReads(geneSAMRecords.get(0).getCigar().getReadLength());
		
		Statistics.setTotalNumberOfReads(totalNumberOfReads);
		Statistics.setOverhang(overhang);
		if(method==0){
			gridLinesVisible=true;
			Statistics.setMethod(Statistics.Method.COVERAGEPEREXON);
		}else if(method==1){
			gridLinesVisible=true;
			Statistics.setMethod(Statistics.Method.RPK);
		}else if(method==2){
			gridLinesVisible=false;
			Statistics.setMethod(Statistics.Method.RPKM);
		}else{
			Statistics.setMethod(Statistics.Method.COVERAGEPEREXON);
		}
	}
	public float getRPKM(ArrayList<SAMRecord> samRecords,int totalNumberOfReads) {
		return Statistics.getRPKM(samRecords, getConstitutiveIntervals(customConstitutiveUnscaledPositions),totalNumberOfReads);
		
	}
	public ArrayList<Interval> getConstitutiveIntervals(ArrayList<Integer> inputArrayOfConstitutivePositions){
		ArrayList<Interval> listOfIntervals = new ArrayList<Interval>();
		int start = -1;
		int currentLength=-1;
		
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
	private ArrayList<Line> getSpliceLines(ArrayList<Rectangle_Unweighted> exonsToSort,int yPosition){
		
		ArrayList<Line> spliceLineArray = new ArrayList<Line>();
		
		Collections.sort(exonsToSort,new RectangleComparator());
		
		//THis is sorted on the start position of the scaled xPositions;
		float scaledLastExonEnd=0;
		float scaledLastExonY=0;
		
		for(int i =0;i<exonsToSort.size();i++){
			//System.out.println(sortedRectangles.get(i).getXCoord());
			Rectangle_Unweighted rect = exonsToSort.get(i);
			
			if(i!=0){
					float midPoint = (scaledLastExonEnd+rect.getScaledXCoord())/2;
					spliceLineArray.add(new Line(scaledLastExonEnd,scaledLastExonY,midPoint,yPosition-10));	
					spliceLineArray.add(new Line(midPoint,yPosition-10,rect.getScaledXCoord(),rect.getScaledYCoord()));
			}
			scaledLastExonEnd = rect.getScaledXCoord()+rect.getScaledLength();
			scaledLastExonY=rect.getScaledYCoord();
		}
		return spliceLineArray;		
	}
	
	//This is how you will do zooming
	private void drawWindow(int startCoord, int endCoord){
		
	}
		
	/**
	 * Gets the short read density height at the specified xPixel
	 * 
	 * @param pixelPosition is the pixel you want the height of
	 * 
	 * @return the short read density height at
	 */
	private float getShortReadDensityHeightAt(int pixelPosition){
		if(shortReads_Set_U1!=null){
			if(shortReads_Set_U1.get(pixelPosition)==null){
				System.err.println("A request for short read density was made but the pixel was not found" +
						"\nYou may have to call loadShortReads again if the dimensions have changed");
				return 0;
			}
			if(pixelPosition == shortReads_Set_U1.get(pixelPosition).getScaledX()){
				return shortReads_Set_U1.get(pixelPosition).getAverageHits();
			}else{
				return shortReads_Set_U1.get((int)reverse(pixelPosition)).getAverageHits();
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
		if(shortReads_Set_U1!=null){
			if(pixelPosition == shortReads_Set_U1.get(pixelPosition).getScaledX()){
				return shortReads_Set_U1.get(pixelPosition).getAbsoluteXStart()+"-"+ shortReads_Set_U1.get(pixelPosition).getAbsoluteXEnd();
			}else{
				return shortReads_Set_U1.get((int)reverse(pixelPosition)).getAbsoluteXStart()+"-"+ shortReads_Set_U1.get((int)reverse(pixelPosition)).getAbsoluteXEnd();
			}
		}else{
			System.err.println("An attempt was made to fetch the span of the coordinate but scaledDensityPlot was null");
			return "Empty";
		}
	}
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
	private float reverse(float position){
		return (graphXEnd-position)+graphXStart;
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
					int prev = allPositions.get(y);
					prev++;
					allPositions.put(y,prev);
				}
			}
		}
		ArrayList<Integer> newConstitutiveUnscaledPositions = new ArrayList<Integer>();
		if(mrnaList.size()>1){
			for(int i = absoluteStartOfGene;i<=absoluteEndOfGene;i++){
				//Those sites with a tally equal to the size of the MRNA list are constitutive
				if(allPositions.get(i)==mrnaList.size()){
					newConstitutiveUnscaledPositions.add(i);
				}
			}	
		}
		return newConstitutiveUnscaledPositions;
	}
	private float scaleAbsoluteCoord_Uncollapsed(float inCoord){
		return map(inCoord,absoluteStartOfGene,absoluteEndOfGene,
				graphXStart,graphXEnd);
	}
	private float scaleAbsoluteCoord_Collapsed(float value,ArrayList<Interval> intervalsToExclude){
		//Find the sum of the lengths of the introns that are less than the position.
		int sum=0;
		int totalSum=0;
		int count=0;
		
		boolean valueIsInIntron = false;
		float relativePosition=0;
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
				200*((value-interval.getStartCoord())/interval.getLength());//Relative position to the intron
			}
		}
		float newHigh1=(absoluteEndOfGene-totalSum)+(intervalsToExclude.size()*200);
		
		
		if(valueIsInIntron){
			float newValue = (relativePosition-sum)+(count*200);
			return map(newValue,absoluteStartOfGene,newHigh1,graphXStart,graphXEnd);
		}else{
			//All the regions longer than 500 are shortened to 200
			float newValue = (value-sum)+(count*200);
			return map(newValue,absoluteStartOfGene,newHigh1,graphXStart,graphXEnd);
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
	private synchronized void fillUncollapsed_ScaledShortReadsPlot(HashMap<Integer,GraphColumn> shortReadsMap,
				HashMap<Integer,Integer> densityMap){
			shortReadsMap.clear();
			//Make a line for every Pixel
			for(int i =graphXStart;i<=graphXEnd;i++){
				shortReadsMap.put(i,(new GraphColumn(i, 0, 0, 0)));		
			}
			int prevPixel = -1;
			int currentSum=-1;
			int numberOfShortReads=-1;
			int frameAbsoluteStart=-1;
			int frameAbsoluteEnd = -1;
			
			//Goes through each of the genomic coordinates and attempts to map it to a pixel
			//In the end, a pixel represents the average number of short reads that cross it
			float maxAverage =0;
			for(int i = absoluteStartOfGene;i<=absoluteEndOfGene;i++){
				int mappedPixel = (int) map(i,absoluteStartOfGene,absoluteEndOfGene,graphXStart,graphXEnd);
				//System.out.println(mappedPixel +"---Abs" + iAbsoluteDensity.get(i));
				if(prevPixel==-1){
					frameAbsoluteStart=i;
					frameAbsoluteEnd=i;
					numberOfShortReads=1;
					currentSum=densityMap.get(i);
					prevPixel=mappedPixel;
				}else if(mappedPixel!= prevPixel){
					float average = (float)currentSum/numberOfShortReads;
					if(average>maxAverage){
						maxAverage=average;
					}
					shortReadsMap.get(prevPixel).incrementHeight(average);
					shortReadsMap.get(prevPixel).setAbsoluteXCoords(frameAbsoluteStart,frameAbsoluteEnd);
					
					prevPixel=mappedPixel;
					numberOfShortReads=1;
					currentSum=densityMap.get(i);
					frameAbsoluteStart=i;
					frameAbsoluteEnd=i;
				}else if(i==absoluteEndOfGene){
					frameAbsoluteEnd=i;
					numberOfShortReads++;
					shortReadsMap.get(prevPixel).incrementHeight((int)((double)currentSum/numberOfShortReads));
					shortReadsMap.get(prevPixel).setAbsoluteXCoords(frameAbsoluteStart,frameAbsoluteEnd);
				}else{	
					currentSum+=densityMap.get(i);
					frameAbsoluteEnd=i;
					numberOfShortReads++;
				} 
	//			System.out.println(i + "----"  + iAbsoluteDensity.get(i));
			}
			for(GraphColumn column:shortReadsMap.values()){
				column.setScaledHeight((int) map(column.getAverageHits(),0,maxAverage,0,80));
			}
		}
	private synchronized void fillCollapsed_ScaledShortReadsPlot(HashMap<Integer,GraphColumn> shortReadsMap,
				HashMap<Integer,Integer> densityMap,ArrayList<MRNA> isoforms){
			shortReadsMap.clear();
			//Make a line for every Pixel
			for(int i =graphXStart;i<=graphXEnd;i++){
				shortReadsMap.put(i,(new GraphColumn(i, 0, 0, 0)));		
			}
			
			//Get a list of the large consitutitive introns
			ArrayList<Interval> listOfIntrons = getLargeConstitutiveIntrons(isoforms);
					
			int prevPixel = -1;
			int currentSum=-1;
			int numberOfShortReads=-1;
			int frameAbsoluteStart=-1;
			int frameAbsoluteEnd = -1;
			
			//Goes through each of the genomic coordinates and attempts to map it to a pixel
			//In the end, a pixel represents the average number of short reads that cross it
			float maxAverage =0;
			for(int i = absoluteStartOfGene;i<=absoluteEndOfGene;i++){
				int mappedPixel = (int) scaleAbsoluteCoord_Collapsed(i,listOfIntrons);
				if(mappedPixel<graphXStart||mappedPixel>graphXEnd){
					System.err.println(mappedPixel);
					return;
				}
				//System.out.println(i + "-->" + mappedPixel);// +"---Abs" + iAbsoluteDensity.get(i));
				if(prevPixel==-1){
					frameAbsoluteStart=i;
					frameAbsoluteEnd=i;
					numberOfShortReads=1;
					currentSum=densityMap.get(i);
					prevPixel=mappedPixel;
				}else if(mappedPixel!= prevPixel){
					float average = (float)currentSum/numberOfShortReads;
					if(average>maxAverage){
						maxAverage=average;
					}
					shortReadsMap.get(prevPixel).incrementHeight(average);
					shortReadsMap.get(prevPixel).setAbsoluteXCoords(frameAbsoluteStart,frameAbsoluteEnd);
					
					prevPixel=mappedPixel;
					numberOfShortReads=1;
					currentSum=densityMap.get(i);
					frameAbsoluteStart=i;
					frameAbsoluteEnd=i;
				}else if(i==absoluteEndOfGene){
					frameAbsoluteEnd=i;
					numberOfShortReads++;
					shortReadsMap.get(prevPixel).incrementHeight((int)((double)currentSum/numberOfShortReads));
					shortReadsMap.get(prevPixel).setAbsoluteXCoords(frameAbsoluteStart,frameAbsoluteEnd);
				}else{	
					currentSum+=densityMap.get(i);
					frameAbsoluteEnd=i;
					numberOfShortReads++;
				} 
	//			System.out.println(i + "----"  + iAbsoluteDensity.get(i));
			}
			for(GraphColumn column:shortReadsMap.values()){
				column.setScaledHeight((int) map(column.getAverageHits(),0,maxAverage,0,80));
			}
		}
	/**
	 * Load short reads.
	 *
	 * @param iShortReads the i short reads
	 */
	private synchronized void loadUncollapsed_ShortReads(ArrayList<SAMRecord> iSamRecords){
		
		HashMap<Integer, Integer> absoluteDensityMap = Statistics.getDensityMap(absoluteStartOfGene, absoluteEndOfGene, geneSAMRecords);
		fillUncollapsed_ScaledShortReadsPlot(shortReads_Set_U1,absoluteDensityMap);
		if(!isCodingStrand){
			for(GraphColumn column:shortReads_Set_U1.values()){
				column.setScaledX(reverse(column.getScaledX()));
			}
		}
	}
}
