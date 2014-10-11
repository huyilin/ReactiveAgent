package template;

public class pdAction {
	int iftake;
	int nextCity;
	String key;
	public pdAction(int ifTake, int nextCity) {
		this.iftake = ifTake;
		this.nextCity = nextCity;
		this.key = Integer.toString(iftake) + ',' + Integer.toString(nextCity);
	}
}