package genomeBrowser;

import gffParser.CDS;
import gffParser.Exon;
import gffParser.MRNA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import org.w3c.dom.css.Rect;

import processing.core.*;

public class ProcessingApplet extends PApplet{
	int width;
	int height;
	int lengthOfGene;
	boolean isoformsVisible;
	Collection<MRNA> isoforms;
	int startOfGene;
	
	public ProcessingApplet(int iWidth, int iHeight){
		width=iWidth;
		height=iHeight;
		isoformsVisible = true;
		lengthOfGene = 0;
		
	
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
				fill(255,0,0, 100); //Red
				for(CDS cds :isoform.getCDS().values()){
						int startScaled =(int) map(cds.getStart()-startOfGene, 0,lengthOfGene,0,800);
						int lengthScaled=(int) map(cds.getLength(),0,lengthOfGene,0,800);
						rect(startScaled,yPosition,lengthScaled,20);
				}
				fill(0,255,0, 100);//Green
				if(isoform.getStartCodon()!=null){
					int startScaled = (int) map(isoform.getStartCodon().getStart()-startOfGene,0,lengthOfGene,0,800);
					int lengthScaled=(int) map(isoform.getStartCodon().getLength(),0,lengthOfGene,0,800);
					rect(startScaled,yPosition,lengthScaled,15);
				}
				if(isoform.getStopCodon()!=null){
					int startScaled = (int) map(isoform.getStopCodon().getStart()-startOfGene,0,lengthOfGene,0,800);
					int lengthScaled=(int) map(isoform.getStopCodon().getLength(),0,lengthOfGene,0,800);
					rect(startScaled,yPosition,lengthScaled,15);
				}
				fill(0,0,255, 100); //Blue
				for(Exon exons :isoform.getExon().values()){
					int startScaled = (int) map(exons.getStart()-startOfGene,0,isoform.getEnd(),0,800);
					int lengthScaled=(int) map(exons.getLength(),0,isoform.getLength(),0,800);
					rect(startScaled,yPosition,lengthScaled,20);
				}
				yPosition +=30;
			}
		}
	}
	
	/**
	 * Loads new isoform.
	 * 
	 * @param collection the mRNA
	 * @param iLengthOfGene the length of gene
	 */
	public void showNewIsoform(Collection<MRNA> collection,int iLengthOfGene,int iStartOfGene){
			isoforms = collection;
			lengthOfGene = iLengthOfGene;
			startOfGene = iStartOfGene;
	}
	
}
