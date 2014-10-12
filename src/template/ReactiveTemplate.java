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


public class ReactiveTemplate implements ReactiveBehavior {

	private Random random;
	private double pPickup;
	private HashMap <String, pdState> stateMap= new HashMap<String, pdState> ();
	private HashMap <String, pdAction> actionMap= new HashMap<String, pdAction> ();
	private List<City> cityList;
	
	public void stateMapInit (List<City> cityList) {
		for(City current : cityList) {
			pdState noPackage = new pdState(current.id, 0);
			stateMap.put(noPackage.key, noPackage);
			for(City destine : cityList) {
				if (destine.id != current.id) {
					pdState state = new pdState(current.id, destine.id);
					
				}
			}
		}
	}
	
	public void actionMapInit (List<City> cityList) {
		for(City city : cityList) {
			pdAction actionA = new pdAction(0, city.id);
			pdAction actionB = new pdAction(1, city.id);
			actionMap.put(actionA.key, actionA);
			actionMap.put(actionA.key, actionB);
		}
	}
			
	public int Reward (pdState s, pdAction a) {
		return 1;
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
