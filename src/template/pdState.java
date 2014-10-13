package template;
import logist.topology.Topology.City;

public class pdState {
	City currentCity;
	City destineCity;
	boolean hasPackage;
	String key;
	public pdState (City currentCity, City destineCity) {
		this.currentCity = currentCity;
		this.destineCity = destineCity;
		this.key = Integer.toString(currentCity.id) + ',' + Integer.toString(destineCity.id);
		this.hasPackage = true;
	}
	
	public pdState (City currentCity) {
		this.currentCity = currentCity;
		this.destineCity = null;
		this.hasPackage = false;
		this.key = Integer.toString(currentCity.id) + ',' + Integer.toString(-1);
	}
}
