package org.artemisproject.ddet.runtime;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import org.artemisproject.ddet.model.State;




/**
 * A {@link LDDM} can manage dynamic dependences of a transaction, 
 * that is, get its components to be used in future called future and the components 
 * having been used in the past called real past. It can be used alone, or when events 
 * happen, such as, transaction start, end, first invoke component, its dynamic dependences
 * changed, it will notify you.
 * 
 * @author Ping Su<njupsu@gmail.com>
 */

public class LDDM {
	
	/**
	 * Unique identifier of a transaction runs.
	 */
	private String transactionID;
	
	
	public static Hashtable<String, LDDM> ddes = new Hashtable<String, LDDM>();
	/**
	 * Whether a transaction is in a response of the given method of the given component.
	 * the identifier of the method like this : componentName; methodName 
	 */
	public Set<String> past = null;
	/**
	 * All states in the transaction's Dynamic Dependency Automaton
	 */
	private List<State> states = new LinkedList<State>();

	/**
	 * The current state of the transaction in its Dynamic Dependency Automaton
	 */
	public int currentState = -1;
	/**
	 * A transaction id only corresponds to a unique nest method.
	 */
	private static Hashtable<String, String> nests = new Hashtable<String, String>();
	
//	private String cnName;
	/**
	 *The key for associating the current transaction id and the dependent tx id, like cnName+":"+mnInf
	 */
	private String cnMnInf;
	
	private String cnName;
	/**
	 * the nest method invoker transaction id
	 */
	private String nester;	

	public String statesDDA;
	/**
	 * the root transaction of the current transaction
	 */
	String realNester;


	/**TxDepMonitor
	 * This is for java implementation to get its LDDM to manage its dynamic dependences
	 * @param namemonitor
	 * 			transaction id
	 * @param states	 
	 * @param nexts
	 * @return
	 */
	public static LDDM getInstance(String transactionID, String statesDDAStr, String cnMn) {
		if (ddes.containsKey(transactionID)) {
			return ddes.get(transactionID);
		} else {
			LDDM instance = new LDDM(transactionID, statesDDAStr, cnMn);
			ddes.put(transactionID, instance);
			return instance;
		}
	}
	/**
	 * This is for Tusscany to get transaction's dynamic dependences, also for quiescence
	 * @param transactionID
	 * @return
	 */
	public static LDDM getInstance(String transactionID) {
		if (ddes.containsKey(transactionID)) {
			return ddes.get(transactionID);
		} else {			
			LDDM instance = new LDDM(transactionID);
			ddes.put(transactionID, instance);
			return instance;
		}
	}
	
	/**
	 * for tranquility and version-consistency
	 * @param name
	 *            transactionID
	 */
	private LDDM(String transactionID,String statesDDA,String cnMn) {
		this.transactionID = transactionID;
		this.cnMnInf = cnMn;
		this.cnName = cnMnInf.split(":")[0];
		this.nester = this.findNester();
		if (nester != null) {
			nests.put(this.nester, transactionID);
			// System.out.println("----------------Nest begins: "+this.transactionID);
		}
		
		String[] stateAnno = statesDDA.split(";");
		for (int i = 0; i < stateAnno.length; i++) {
			String[] si = stateAnno[i].split(",");
			Set<String> stateFuture = new ConcurrentSkipListSet<String>();
			for (int j = 0; j < si.length; j++) {
				if (!si[j].equals("_E")) {
					stateFuture.add(si[j]);
				}
			}
			State state = new State();
			state.setFuture(stateFuture);
			states.add(state);
		}
	}
	/**
	 * for Quiescence
	 * @param transactionID
	 */
	private LDDM(String transactionID) {
		this.transactionID = transactionID;
	}
	
	/**
	 * according to the temp nestee key, find the nester
	 * @return
	 */
	private String findNester(){
		Enumeration<String> nesters = nests.keys();
		while (nesters.hasMoreElements()) {
			String tempNester = nesters.nextElement();
			if(nests.get(tempNester).equals(cnMnInf)){
				return tempNester;
			}
		}
		return null;

	}
	public String getNester(){
		return nester;
	}
	public int getCurrentState(){
		return currentState;
	}
	public void setCurrentState(int cur){
		currentState = cur;
	}
	/**
	 * if the transaction is the invoker, we will find the runtime transaction id (nestee tx id)
	 * @return
	 */
	public String getRealTxID(){
		String realTxID = transactionID;
		while(nests.containsKey(realTxID)){			
			String tempRealTxID = nests.get(realTxID);
			if(!ddes.containsKey(tempRealTxID)){
				return realTxID;
			}
			else{
				realTxID = nests.get(realTxID);
			}
		}
		return realTxID;
	}
	public String getRealNester(){
		String realNester = transactionID;
		while(LDDM.getInstance(realNester).getNester() != null){
			realNester = LDDM.getInstance(realNester).getNester();
		}
		return realNester;
	}
	/**
	 * Get future set in in the current DDA (consider nest invocation)
	 * @return
	 *
	 */
	public Set<String> getFuture() {
		//if it's a nester
		Set<String> nesterFuture = new ConcurrentSkipListSet<String>();
		nesterFuture.addAll(getTempFuture());
		String realTxID = transactionID;
		if(nests.containsKey(realTxID)){
			String tempRealTxID = nests.get(realTxID);
			if(ddes.containsKey(tempRealTxID)){				
				nesterFuture.addAll(LDDM.getInstance(tempRealTxID).getFuture());
			}
		}
		return nesterFuture;
	}
	/**
	 * Get future set in in the current DDA (not consider nest invocation)
	 * @return
	 *
	 */
	public Set<String> getTempFuture(){
		if(states.isEmpty()){
			return new ConcurrentSkipListSet<String>();
		}
		else{
			return states.get(currentState).getFuture();
		}		
	}

	/**
	 * get services have been used
	 * @return
	 */
	public Set<String> getPast() {
		Set<String> nesterPast = past;		
		if(nests.containsKey(transactionID)){
			String realTxID = nests.get(transactionID);
			if(ddes.containsKey(realTxID)){				
				nesterPast.addAll(LDDM.getInstance(realTxID).getPast());
			}
		}
		return nesterPast;	
	}

	public void nextState(int sta){		
		this.currentState = sta;
	}
	public void nestStart(String nestee,int nextSta){
		this.currentState = nextSta;		
		nests.put(transactionID, cnName+":"+nestee);
	}	
	
	public void triggerCom(String component,int nextSta){
		this.currentState = nextSta;
		past.add(component);
	}
	/**
	 * for test, get precise dynamic dependences
	 * @param component
	 */
	public void triggerBeforeCom(String component){
		past.add(component);
	}
	public void triggerStart(){
		this.currentState = 0;		
		past = new ConcurrentSkipListSet<String>();	
		//trigger the root transaction change
//		realNester = this.getRealNester();
//		evatime = EvaTime.getInstance(realNester);
//		if(realNester.equals(transactionID)){
//			evatime.compute("Start");				
//		}
//		else{
//			evatime.compute("Nest handling");				
//		}
	}
	public void triggerEnd(){
		//delete the nester from the nest table
		if(nester != null){
			nests.remove(nester);
			this.nester = null;
		}
		ddes.remove(transactionID);
//		System.out.println("Transaction: " + transactionID +" end. Current state: "+currentState);
	}

}
