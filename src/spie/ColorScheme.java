package spie;

import java.awt.Color;

import java.util.LinkedHashMap;

import javax.swing.DefaultListModel;


public class ColorScheme {
	private LinkedHashMap<String,Color> colorScheme;
	public ColorScheme(){
		colorScheme=new LinkedHashMap<String, Color>();
		colorScheme.put("Constitutive",new Color(0,0,255));
		colorScheme.put("Exon",new Color(0,0,0));
		colorScheme.put("CDS",new Color(100,100,100));
		colorScheme.put("StartStop", new Color(0,255,0));
		colorScheme.put("0",new Color(255,0,0));
		colorScheme.put("1",new Color(0,255,0));
		colorScheme.put("2",new Color(255,0,0));
		colorScheme.put("3",new Color(0,255,0));
		colorScheme.put("4",new Color(0,255,0));
		colorScheme.put("5",new Color(0,255,0));
		colorScheme.put("6",new Color(0,255,0));
		colorScheme.put("7",new Color(0,255,0));
		colorScheme.put("8",new Color(0,255,0));
		colorScheme.put("9",new Color(0,255,0));
		colorScheme.put("10",new Color(0,255,0));
		
		
	}
	
	/**
	 * Gets the exon color.
	 *
	 * @return the exon color
	 */
	public Color getExonColor(){
		return colorScheme.get("Exon");
	}
	
	/**
	 * Gets the constitutive color.
	 *
	 * @return the constitutive color
	 */
	public Color getConstitutiveColor(){
		return colorScheme.get("Constitutive");
	}
	
	/**
	 * Gets the cDS color.
	 *
	 * @return the cDS color
	 */
	public Color getCDSColor(){
		return colorScheme.get("CDS");
	}
	
	/**
	 * Gets the color of the input value (which row)
	 *
	 * @param input the input
	 * @return the color
	 */
	public Color getColorOf(int input){
		if(colorScheme.get(""+input)==null){
			return new Color(0,0,0);
		}else {
			return colorScheme.get(""+input);
		}
	}
	
	/**
	 * Sets the color to a custom value
	 *
	 * @param input the input
	 * @param color the color
	 */
	public void setColor(String input,Color color){
		colorScheme.put(input,color);
	}
	
	/**
	 * Gets the list of keys.
	 *
	 * @return the list of keys
	 */
	public DefaultListModel getListOfKeys() {
		DefaultListModel list = new DefaultListModel();
		for(String string : colorScheme.keySet()){
			list.addElement(string);
		}
		return list;
	}
	
	/**
	 * Gets the start and stop codon colors.
	 *
	 * @return the start and stop codon colors.
	 */
	public Color getStartStopColor() {
		return colorScheme.get("StartStop");
	}
}
