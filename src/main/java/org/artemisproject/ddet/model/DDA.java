package org.artemisproject.ddet.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;
import org.artemisproject.ddet.event.TransactionEvent;


/**
 * 
 * 
 * @author Ping Su<njupsu@gmail.com>
 */
public class DDA {

	private final static Logger LOGGER = Logger.getLogger(DDA.class.getName());

	public static Logger getLogger() {
		return LOGGER;
	}
	
	private int start;
	
//	private int end;
	private Set<Integer> end = new ConcurrentSkipListSet<Integer>();
	
	private List<Integer> states = new LinkedList<Integer>();
	
	private List<Transition> transitions = new LinkedList<Transition>();
	
	private List<TransactionEvent> traneve = new LinkedList<TransactionEvent>();
	
	private List<State> statesDDA = new LinkedList<State>();

	
	public DDA() {
		start = -1;
	}
	public DDA(List<Integer> states,List<Transition> events) {		
		this.states = states;
		this.transitions = events;
		start = -1;
	}

	/**
	 * @return the start
	 */
	public int getStart() {
		return start;
	}

	/**
	 * @param start
	 *            the start to set
	 */
	public void setStart(int start) {
		this.start = start;
	}

	/**
	 * @return the end
	 */
	public Set<Integer> getEnd() {
		return end;
	}

	/**
	 * @param end
	 *            the end to set
	 */
	public void setEnd(Set<Integer> end) {
		this.end = end;
	}

	/**
	 * @return the states
	 */
	public List<Integer> getStates() {
		return states;
	}

	/**
	 * @param states
	 *            the states to set
	 */
	public void setStates(List<Integer> states) {
		this.states = states;
	}
	
	public List<TransactionEvent> getTransactionEvent(){
		return traneve;
	}
	
	public void addTransactionEvent(TransactionEvent teve){
		traneve.add(teve);
		
//		if(!(teve instanceof EndEvent)){
//		System.out.println("Add transaction event : "+ teve.getEvent());
//		}
	}
	
	public void setStatesDDA(List<State> statesDDA){
		this.statesDDA = statesDDA;
	}
	@Deprecated
	public int getstate(int head, String event) {
		for (int i = 0; i < transitions.size(); i++) {
			Transition e = transitions.get(i);
			if (e.getHead() == head && e.getEvent().equals(event))
				return e.getTail();
		}
		return -1;
	}
	@Deprecated
	public int stateindex(int index) {
		for (int i = 0; i < states.size(); i++) {
			if ((Integer) states.get(i) == index)
				return i;
		}
		return -1;

	}

	public void addState(int state) {
		states.add(state);
	}

	public void deleteState(int state) {
		for (int i = 0; i < states.size(); i++) {
			if ((Integer) states.get(i) == state)
				states.remove(i);
		}
	}
	/**
	 * merge the two states into one(the latter one)
	 * @param s1
	 *  state location
	 * @param s2
	 *  state location
	 */
	public void mergeStates(int s1, int s2) {
		//delete s1, change to s2 where there is s1
		if(s1 == start){
			this.setStart(s2);
//			System.out.println("$$$$$$$ change start to: "+s2);
		}
		if(states.contains(s1)){
			int state_index = states.indexOf(s1);
			states.remove(state_index);
		}
		else{
			System.out.println("there is some wrong!");
		}
		for (int i = 0; i < transitions.size(); i++) {
			Transition e = transitions.get(i);
			if (e.getTail() == s1) {
				e.setTail(s2);
			}
			if(e.getHead() == s1)
				e.setHead(s2);
		}
	}

	public int getStatesCount() {

		return states.size();
	}

	public void addEvent(Transition event) {
		transitions.add(event);
	}

	public void deleteEvent(Transition event) {
		for (int i = 0; i < transitions.size(); i++) {
			if (event.getEvent().equals(transitions.get(i).getEvent()))
				transitions.remove(i);
		}
	}

	public List<Transition> getEvents() {
		return transitions;
	}
	
	public Transition getEvent(String eve){
		for(Transition e : transitions){
			if(e.getEvent().equals(eve)){
				return e;
			}
		}
		return null;
	}
	
	/**
	 * the next state from the current state
	 * @param srcState
	 * @return
	 */
	public Set<Integer> getNextStateLocation(int srcState){
		Set<Integer> nextState = new HashSet<Integer>();
		for(Transition e: transitions){
			if(e.getHead() == srcState){
				nextState.add(e.getTail());
			}
		}
		return nextState;
	}
	public List<Transition> getNextEvent(int srcState){
		List<Transition> nextEvent = new LinkedList<Transition>();
		for(Transition e: transitions){
			if(e.getHead() == srcState){
				nextEvent.add(e);
			}
		}
		return nextEvent;
	}
	
	/**
	 * for instrumenting the state information in transaction annotation
	 */
	public List<String> statesToList() {

		List<String> stateall = new LinkedList<String>();
		for (int i = 0; i < statesDDA.size(); i++) {
			State s = statesDDA.get(i);
			Set<String> fu = s.getFuture();
			LOGGER.info("" + fu);
			String sall = "";
			if (fu.size() > 0) {
				for (String afu : fu) {
					sall = sall + afu + ",";
				}
				sall = sall.substring(0, sall.length() - 1);
			}
			stateall.add(sall);
		}
		return stateall;
	}
	/**
	 * for instrumenting the state information as a method of the trigger method
	 */
	public String statesToString() {
		String stateString = "";
		List<String> stateall = statesToList();
		if (stateall.size() == 0) {
			stateString = "_E";
		} else {
			for (String s : stateall) {
				if (s.isEmpty()) {
					s = "_E";
				}
				stateString = stateString + s + ";";
			}
			stateString = stateString.substring(0, stateString.length() - 1);
		}
		LOGGER.info("stateString = " + stateString);
		return stateString;

	}
	/**
	 * for instrumenting the transition information as a method of the trigger method
	 */
	public String transitionsToString(List<String> next) {
		String nextString ="";
		for (String s : next) {
			if (s.isEmpty()) {
				s = "_E";
			}
			nextString = nextString + s + ":";
		}
		nextString = nextString.substring(0, nextString.length() - 1);
		LOGGER.info("nextString = " + nextString);
		return nextString;

	}
	/**
	 * set the trigger event and the next state for every state
	 */
	public List<String> transitionToList() {
		List<String> next = new LinkedList<String>();		
		// List state = stateMachine.getStates();
		for (int i = 0; i < statesDDA.size(); i++) {
			String nexts = "";
			int sn = statesDDA.get(i).getLoc();
			int j;
			for (j = 0; j < transitions.size(); j++) {
				if (sn == transitions.get(j).getHead()) {
					State tail = getState(transitions.get(j).getTail());
					nexts = nexts + transitions.get(j).getEvent()+ "-"
							+ statesDDA.indexOf(tail) + ",";
				}
			}
			if (nexts.length() == 0) {
				next.add(nexts);
			} else
				next.add((String) nexts.subSequence(0, nexts.length() - 1));
			LOGGER.info(i + ": "+sn+", next:" + nexts);
		}
		return next;
	}
	/**
	 * according to the given location of the state, get the State in the List<State>
	 * @param i
	 * @param states
	 * @return
	 */
		public State getState(int loc) {
			if (statesDDA != null) {
				Iterator<State> s = statesDDA.iterator();
				while (s.hasNext()) {
					State state = s.next();
					if (state.getLoc() == loc) {
						return state;
					}
				}
				return null;
			}
			return null;

		}


}
