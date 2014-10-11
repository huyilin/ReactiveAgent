package template;

import java.util.List;
import java.util.HashMap;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

import java.util.HashSet;

public class ReactiveTemplate implements ReactiveBehavior {

	private Random random;
	private double pPickup;
	private HashMap <String, pdState> stateMap= new HashMap<String, pdState> ();
	private HashMap <String, pdAction> actionMap= new HashMap<String, pdAction> ();
	private int cityNum;
	private List<City> cityList;
	
	public void stateMapInit (List<City> cityList) {
		for(City city : cityList) {
			city.id;
		}
	}
	
	public void actionMapInit (List<City> cityList) {
		for(City city : cityList) {
			
		}
	}
			
	public int Reward (pdState s, pdAction a) {
		
	}
	
	
	public void policyInit(Topology topology) {
		stateMapInit(cityList);
		actionMapInit(cityList);
		
		
	}
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);

		this.random = new Random();
		this.pPickup = discount;
		this.cityNum = topology.cities().size();
		this.policyInit(topology);
		this.cityList = topology.cities();
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}
		return action;
	}
}
