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


public class ReactiveTemplate implements ReactiveBehavior {

    private Random random;
    private double pPickup;
    private HashMap <String, pdState> stateMap= new HashMap<String, pdState> ();
    private HashMap <String, pdAction> actionMap= new HashMap<String, pdAction> ();
    private HashMap <String, pdAction> bestPolicyMap = new HashMap<String, pdAction>();
    private HashMap <String, Double> bestPolicyValueMap = new HashMap<String, Double>();
    private HashMap <String, pdAction> tempPolicyMap = new HashMap<String, pdAction>();
    private HashMap <String, Double> tempPolicyValueMap = new HashMap<String, Double>();
    private List<City> cityList;   
   
    private HashMap <String, Double> vectorS = new HashMap<String, Double> ();
   
    private TaskDistribution TD;
    private int initThresh = 10;   
       
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
        if (action.iftake && !state.hasPackage) {
            return 0;
        } else if(action.iftake && state.destineCity.id == nextState.currentCity.id) {
                return TD.probability(nextState.currentCity, nextState.destineCity);
        } else if (!action.iftake && action.nextCity.id == nextState.currentCity.id) {
//            System.out.println(nextState.currentCity);
//            System.out.println(nextState.destineCity);
            return TD.probability(nextState.currentCity, nextState.destineCity);
        } else return 0;
    }
   
       
    public double reward(pdState state, pdAction action ){
        if (!state.hasPackage && !action.iftake){                    //no package in the city
            if(action.nextCity.hasNeighbor(state.currentCity)) {
                return -5*state.currentCity.distanceTo(action.nextCity);
            } else return -100000000;
        } else if (state.hasPackage && !action.iftake){                    //has package but refuse
        	if(action.nextCity.hasNeighbor(state.currentCity)) {
                return -5*state.currentCity.distanceTo(action.nextCity);
            } else return -100000000;				//penalize next citx for not being a neighbor				
        } 
        else if(state.hasPackage && action.iftake) {  //has package and take 
            return TD.reward(state.currentCity, state.destineCity) - 5*state.currentCity.distanceTo(state.destineCity);
        } else return -100000000;
    }
   
    public void tempbestPolicyMapInit(){
        for (Entry<String, pdState> state : stateMap.entrySet()){
            vectorS.put(state.getKey(), 0.0);
            tempPolicyValueMap.put(state.getKey(), 0.0);
            tempPolicyMap.put(state.getKey(), null);
            bestPolicyValueMap.put(state.getKey(), 0.0);
            bestPolicyMap.put(state.getKey(), null);
        }
    }
       
   
    public boolean identical(HashMap<String, Double> best, HashMap<String, Double> temp) {
        for (String key : best.keySet()) {
            if(best.get(key) != temp.get(key)) return false;
        }
        return true;
    }
   
    public void policyInit(Topology topology) {
        double quantity = 0;
        double quantityNew = 0;
        double temp = 0;
        int count = 0;
       
        stateMapInit(cityList);
        actionMapInit(cityList);
        tempbestPolicyMapInit();
        while(true) {				
            for (Entry<String, pdState> state : stateMap.entrySet()){		//iterate all states 
                quantity = 0;
                quantityNew = 0;
                for (Entry<String, pdAction> action : actionMap.entrySet()){	// iterate all possible actions
                    temp = 0;   
                    for (Entry<String, pdState> nextState : stateMap.entrySet()){   //possibility average of T(s, a, s_next) over all s_next
                        temp = temp + probability(state.getValue(), action.getValue(), nextState.getValue()) * vectorS.get(nextState.getKey());
                    }
                    temp = temp*0.95;
                    quantityNew = reward(state.getValue(), action.getValue()) + temp;
                    if (quantityNew > quantity){					// larger accumulated reward replaces smaller one
                        quantity = quantityNew;
                        tempPolicyMap.put(state.getKey(), action.getValue());
                        tempPolicyValueMap.put(state.getKey(), quantity);
                    }
                }
                vectorS.put(state.getKey(), quantity);
            }
            if(identical(bestPolicyValueMap, tempPolicyValueMap) && count > initThresh) break;  // terminate condition
            count++;
            bestPolicyMap = tempPolicyMap;
            bestPolicyValueMap = tempPolicyValueMap;
        }
    }
   
    @Override
    public void setup(Topology topology, TaskDistribution td, Agent agent) {

        // Reads the discount factor from the agents.xml file.
        // If the property is not present it defaults to 0.95
       
        Double discount = agent.readProperty("discount-factor", Double.class,
                0.95);
        this.TD = td;
        this.random = new Random();
        this.pPickup = discount;
//        System.out.println(discount);
        this.cityList = topology.cities();
        this.policyInit(topology);
        int count = 0;
        for (Entry<String, pdAction> entry : bestPolicyMap.entrySet()) {
            System.out.println(entry.getKey() + '~' + entry.getValue().key );
            count ++;
        }
//        System.out.println(count);
    }

    @Override   
    public Action act(Vehicle vehicle, Task availableTask) {
        Action action;
        City currentCity = vehicle.getCurrentCity();
       
//        if (availableTask == null || random.nextDouble() > pPickup) {    // no task, next City = pdAction's nextCity
        if (availableTask == null) {    // no task, next City = pdAction's nextCity   
            String stateKey = new pdState(currentCity).key;
           
//            System.out.println(bestPolicyMap.get(stateKey).nextCity);
            action = new Move(bestPolicyMap.get(stateKey).nextCity);
//            System.out.println("moved");
           

        } else {   // package in city
            pdState state = new pdState(currentCity, availableTask.deliveryCity);
            String statekey1 = state.key;
            if(bestPolicyMap.get(statekey1).iftake) {
                action = new Pickup(availableTask);
            } else {
                action = new Move(bestPolicyMap.get(statekey1).nextCity);
            }
        }
        return action;
    }
}