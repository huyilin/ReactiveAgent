package template;

import java.util.List;
import java.util.HashMap;		
import java.util.Map.Entry;
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
	private HashMap <String, pdAction> bestPolicyMap = new HashMap<String, pdAction>();
	private HashMap <String, Double> bestPolicyValueMap = new HashMap<String, Double>();
	private HashMap <String, pdAction> tempPolicyMap = new HashMap<String, pdAction>();
	private HashMap <String, Double> tempPolicyValueMap = new HashMap<String, Double>();
	private HashMap <String, Double> rewardMap = new HashMap<String, Double>();
	private int cityNum;
	private List<City> cityList;
	private TaskDistribution TD;
	
	public void stateMapInit (List<City> cityList) {
		for (City current : cityList){
			pdState noPackage = new pdState(current);
			stateMap.put(noPackage.key, noPackage);
			for (City destine : cityList){
				if (destine.id != current.id){	
					pdState state = new pdState(current, destine);
					stateMap.put(state.key, state);
				}
			}
		}
	}
	
	public void actionMapInit (List<City> cityList) {
		for(City city : cityList) {
			pdAction actionA = new pdAction();
			pdAction actionB = new pdAction(city);
			actionMap.put(actionA.key, actionA);
			actionMap.put(actionB.key, actionB);
		}
	}
	
	public double probability(pdState state, pdAction action, pdState nextState) {
		if (action.iftake && state.destineCity == null) {
			return 0;
		} else if(action.iftake && state.destineCity.id == nextState.currentCity.id) {
				return TD.probability(nextState.currentCity, nextState.destineCity);
		} else if (!action.iftake && action.nextCity.id == nextState.currentCity.id) {
			return TD.probability(nextState.currentCity, nextState.destineCity);
		} else return 0;		
	}
	
		
	public void reward(pdState state, pdAction action ){
		double reward;
		for (Entry<String, pdState> state : stateMap.entrySet()){
			for (Entry<String, pdAction> action : actionMap.entrySet()){
				rewardKey = state.getKey() + ',' + action.getKey();
				if (state.getValue().destineCity == null){                     //no package in the city
					reward = 0;
					rewardKey = state.getKey() + ',' + action.getKey();
					rewardMap.put(rewardKey, reward);
				}
				else if ( (state.getValue().destineCity != null) && (action.getValue().nextCity == null) ){    
					//package in currentCity and decide to take it
					reward = (double) TD.reward(state.getValue().currentCity, action.getValue().nextCity);
					rewardKey = state.getKey() + ',' + action.getKey();
					rewardMap.put(rewardKey, reward);
				}
				else{        //package in currentCit and decide to ignore it
					reward = 0;
					rewardKey = state.getKey() + ',' + action.getKey();
					rewardMap.put(rewardKey, reward);
				}
			}
		}
	}
	
	public void tempbestPolicyMapInit(){
		for (Entry<String, pdState> state : stateMap.entrySet()){
			for (Entry<String, pdAction> action : actionMap.entrySet()){
				tempPolicyValueMap.put(state.getKey(), 0.0);
				tempPolicyMap.put(state.getKey(), null);
				bestPolicyValueMap.put(state.getKey(), 0.0);
				bestPolicyMap.put(state.getKey(), null);
			}
		}
	}
		
	public void tempPolicyValueInit(){
		
	}
	
	public void policyInit(Topology topology) {
		double quantity = 0;
		double quantityNew = 0;
		double temp = 0;
		stateMapInit(cityList);
		actionMapInit(cityList);
		tempbestPolicyMapInit();
		rewardInit();
	
		for (Entry<String, pdState> state : stateMap.entrySet()){
			quantity = 0;
			quantityNew = 0;
			for (Entry<String, pdAction> action : actionMap.entrySet()){
				temp = 0;
				for (Entry<String, pdState> nextState : stateMap.entrySet()){
					temp = temp + probability(state.getValue(), action.getValue(), nextState.getValue()) * vFunction(nextState.getKey());
				}
				temp = temp*pPickup;
				quantityNew = reward(state.getKey(), action.getKey()) + temp;
				if (quantityNew > quantity){
					quantity = quantityNew;
					tempPolicyMap.put(state.getKey(), action.getValue());
					tempPolicyValueMap.put(state.getKey(), quantity);
				}			
			}
		}
		bestPolicyMap = tempPolicyMap;
		bestPolicyValueMap = tempPolicyValueMap;
		for (Entry<String, Double> a : tempPolicyValueMap.entrySet()){ 
			System.out.println(a.getKey()+ a.getValue().toString());
		}
		
	}
	
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);

		this.random = new Random();
		this.pPickup = discount;
		System.out.println(discount);
		this.cityNum = topology.cities().size();
		this.cityList = topology.cities();
		this.policyInit(topology);
		TD = td;
	}
	
	public double vFunction(String s){     // get the value of V(s')
		double value = 0;
  		String[] keyArray = s.split(",");  // s is in the form of "state + action + nextState"
		String key = keyArray[2];          // extract s'
		for (Entry<String, Double> tempPolicyValue : tempPolicyValueMap.entrySet()){
			if (tempPolicyValue.getKey() == key){
				value = tempPolicyValue.getValue();
			}
		}
		return value;
	}	

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;
		City currentCity = vehicle.getCurrentCity();		
		
		if (availableTask == null || random.nextDouble() > pPickup) {
			//City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}
		return action;
	}
}
