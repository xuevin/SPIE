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
		
		
	}
	public Color getExonColor(){
		return colorScheme.get("Exon");
	}
	public Color getConstitutiveColor(){
		return colorScheme.get("Constitutive");
	}
	public Color getCDSColor(){
		return colorScheme.get("CDS");
	}
	public Color getColorOf(int input){
		if(colorScheme.get(""+input)==null){
			return new Color(0,0,0);
		}else {
			return colorScheme.get(""+input);
		}
	}
	public void setColor(String input,Color color){
		colorScheme.put(input,color);
	}
	public DefaultListModel getListOfKeys() {
		DefaultListModel list = new DefaultListModel();
		for(String string : colorScheme.keySet()){
			list.addElement(string);
		}
		return list;
	}
	public Color getStartStopColor() {
		return colorScheme.get("StartStop");
	}
}
