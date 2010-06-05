package genomeBrowser;

import gffParser.CDS;
import gffParser.Exon;
import gffParser.Gene;
import gffParser.MRNA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;

import org.w3c.dom.css.Rect;

import processing.core.*;

public class ProcessingApplet extends PApplet{
	private int width;
	private int height;
	private int lengthOfGene;
	private boolean isoformsVisible,tightIsoformsVisible, isCodingStrand;
	private Collection<MRNA> isoforms;
	private TreeSet<Integer> allScaledEndCoords;
	private int startOfGene;
	
	
	public ProcessingApplet(int iWidth, int iHeight){
		width=iWidth;
		height=iHeight;
		isoformsVisible = true;
		tightIsoformsVisible = false;
		lengthOfGene = 0;
		isCodingStrand=true;
		
	
	}
	public void setup(){
		//Subtract the title bar height
		int titleBarHeight = getBounds().y;
	    size(width, height-titleBarHeight);
	    
	    smooth();
	}
	public void draw(){
		background(0);
		stroke(255);
		line(0, 0, this.mouseX, this.mouseY);
		line(width, 0, this.mouseX, this.mouseY);
		drawIsoforms();
		drawTightIsoforms();
		
	}
	public void setNewSize(int iHeight, int iWidth){
		width=iWidth;
		height=iHeight;
		int titleBarHeight = getBounds().y;
	    size(width, height-titleBarHeight);
	}
	
	/**
	 * Draw isoforms. It iterates through the isoforms.
	 */
	private void drawIsoforms(){
		if(isoformsVisible && isoforms!=null){
			int yPosition = 40;
			
			
			for(MRNA isoform: isoforms){
				fill(0,0,255, 100); //Blue
				for(Exon exons :isoform.getExon().values()){
					int startScaled = (int) map(exons.getStart()-startOfGene,0,lengthOfGene,0,800);
					int lengthScaled=(int) map(exons.getLength(),0,lengthOfGene,0,800);
					// This flips the 
					if(isCodingStrand){
						rect(startScaled,yPosition+5,lengthScaled,10);
					}else{
						rect(800-(startScaled+lengthScaled),yPosition+5,lengthScaled,10);
					}
							
				}
				fill(255,0,0); //Red
				for(CDS cds :isoform.getCDS().values()){
					int startScaled =(int) map(cds.getStart()-startOfGene, 0,lengthOfGene,0,800);
					int lengthScaled=(int) map(cds.getLength(),0,lengthOfGene,0,800);
					if(isCodingStrand){
						rect(startScaled,yPosition,lengthScaled,20);
					}else{
						rect(800-(startScaled+lengthScaled),yPosition,lengthScaled,20);
					}
						
				}
				fill(0,255,0, 100);//Green
				if(isoform.getStartCodon()!=null){
					int startScaled = (int) map(isoform.getStartCodon().getStart()-startOfGene,0,lengthOfGene,0,800);
					int lengthScaled=(int) map(isoform.getStartCodon().getLength(),0,lengthOfGene,0,800);
					if(isCodingStrand){
						rect(startScaled,yPosition,lengthScaled,20);
					}else{
						rect(800-(startScaled+lengthScaled),yPosition,lengthScaled,20);
					}
				}
				if(isoform.getStopCodon()!=null){
					int startScaled = (int) map(isoform.getStopCodon().getStart()-startOfGene,0,lengthOfGene,0,800);
					int lengthScaled=(int) map(isoform.getStopCodon().getLength(),0,lengthOfGene,0,800);
					if(isCodingStrand){
						rect(startScaled,yPosition,lengthScaled,20);
					}else{
						rect(800-(startScaled+lengthScaled),yPosition,lengthScaled,20);
					}
				}
				
				yPosition +=30;
			}
		}
	}
	
	/**
	 * Allows the User to View the Reverse Strand
	 * @param b 
	 */
	public void setCoding(boolean b){
		isCodingStrand=b;
	}
	/**
	 * Show new isoform.
	 * 
	 * @param draws the gene you want to see
	 */
	public void loadIsoform(Gene gene) {
		isoforms = gene.getMRNA().values();
		lengthOfGene = gene.getLength();
		startOfGene=gene.getStart();
		
	}
	private void drawShortReads(){
			
	}
	
	/**
	 * Disable introns.
	 * 
	 * @param boolean - true if you want to disable introns
	 */
	public void disableIntrons(boolean b) {
		//Fill a TreeMap Once with all the pieces.
		//Then when you are drawing,subtract until you reach the largest piece...
		if(b){
			isoformsVisible=false;
			tightIsoformsVisible=true;
			allScaledEndCoords = new TreeSet<Integer>();
			for(MRNA mrna:isoforms){
				for(CDS cds:mrna.getCDS().values()){
					if(mrna.getStrand()){
						allScaledEndCoords.add((int) map(cds.getEnd()-startOfGene,0,lengthOfGene,0,800));	
					}else{
						allScaledEndCoords.add((int) map(cds.getEnd()-startOfGene,0,lengthOfGene,0,800));
					}
					
				}
				for(Exon exon:mrna.getExon().values()){
					allScaledEndCoords.add((int) map(exon.getEnd()-startOfGene,0,lengthOfGene,0,800));	
				}
				allScaledEndCoords.add((int) map(mrna.getStartCodon().getEnd()-startOfGene,0,lengthOfGene,0,800));
				allScaledEndCoords.add((int) map(mrna.getStopCodon().getEnd()-startOfGene,0,lengthOfGene,0,800));	
			}
		}else{
			isoformsVisible=true;
			tightIsoformsVisible=false;
		}
	}
	private void drawTightIsoforms(){
		if(tightIsoformsVisible && allScaledEndCoords!=null){
			for(Integer currentEnd:allScaledEndCoords){
				line(800-currentEnd,0,800-currentEnd,100);
			}
			int yPosition = 40;
			for(MRNA isoform: isoforms){
				fill(0,0,255, 100); //Blue
				for(Exon exons :isoform.getExon().values()){
					int startScaled = (int) map(exons.getStart()-startOfGene,0,lengthOfGene,0,800);
					int lengthScaled=(int) map(exons.getLength(),0,lengthOfGene,0,800);
					
					if(isoform.getStrand()){
						rect(startScaled,yPosition+5,lengthScaled,10);
					}else{
						rect(800-(startScaled+lengthScaled),yPosition+5,lengthScaled,10);
					}
							
				}
				fill(255,0,0); //Red
				for(CDS cds :isoform.getCDS().values()){
						int startScaled =(int) map(cds.getStart()-startOfGene, 0,lengthOfGene,0,800);
						int lengthScaled=(int) map(cds.getLength(),0,lengthOfGene,0,800);
						if(isoform.getStrand()){
							rect(startScaled,yPosition,lengthScaled,20);
						}else{
							rect(800-(startScaled+lengthScaled),yPosition,lengthScaled,20);
						}
						
				}
				fill(0,255,0, 100);//Green
				if(isoform.getStartCodon()!=null){
					int startScaled = (int) map(isoform.getStartCodon().getStart()-startOfGene,0,lengthOfGene,0,800);
					int lengthScaled=(int) map(isoform.getStartCodon().getLength(),0,lengthOfGene,0,800);
					if(isoform.getStrand()){
						rect(startScaled,yPosition,lengthScaled,20);
					}else{
						rect(800-(startScaled+lengthScaled),yPosition,lengthScaled,20);
					}
				}
				if(isoform.getStopCodon()!=null){
					int startScaled = (int) map(isoform.getStopCodon().getStart()-startOfGene,0,lengthOfGene,0,800);
					int lengthScaled=(int) map(isoform.getStopCodon().getLength(),0,lengthOfGene,0,800);
					if(isoform.getStrand()){
						rect(startScaled,yPosition,lengthScaled,20);
					}else{
						rect(800-(startScaled+lengthScaled),yPosition,lengthScaled,20);
					}
				}
				
				yPosition +=30;
			}
		}	
	}
	public void flip() {
		isCodingStrand=!isCodingStrand;
		
	}
	
}
