package template;
import logist.topology.Topology.City;

public class pdState {
	City currentCity;
	City destineCity;
	String key;
	public pdState (City currentCity, City destineCity) {
		this.currentCity = currentCity;
		this.destineCity = destineCity;
		this.key = Integer.toString(currentCity) + ',' + Integer.toString(destineCity);
	}
}
