package template;

public class pdState {
	int currentCity;
	int destineCity;
	String key;
	public pdState (int currentCity, int destineCity) {
		this.currentCity = currentCity;
		this.destineCity = destineCity;
		this.key = Integer.toString(currentCity) + ',' + Integer.toString(destineCity);
	}
}
