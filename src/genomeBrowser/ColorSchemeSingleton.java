package genomeBrowser;

public class ColorSchemeSingleton {
	static ColorSchemeSingleton instance;
	protected ColorSchemeSingleton(){
		
	}
	public static ColorSchemeSingleton getInstance() {
		if(instance == null) {
			instance = new ColorSchemeSingleton();
		}
		return instance;
	}
	

}
