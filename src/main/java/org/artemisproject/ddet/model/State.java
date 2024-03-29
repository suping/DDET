package org.artemisproject.ddet.model;


import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
/**
 * 
 * @author Ping Su <njupsu@gmail.com>
 *
 */
public class State {
	
	private int loc;
	private Set<String> future = new ConcurrentSkipListSet<String>();
	/**
	 * past is not used now, it maybe used in future.
	 */
	private Set<String> past = new ConcurrentSkipListSet<String>();

	public State(){
		
	}
	
	public State(int l) {
		this.loc = l;		
	}
	public State(int l, Set<String> fut){
		loc = l;
		future = fut;
		
	}

	public void setLoc(int l) {
		this.loc = l;
	}
	public int getLoc() {
		return loc;
	}
	public void setFuture(Set<String> future) {
		this.future = future;
	}

	public void setPast(Set<String> past) {
		this.past = past;
	}

	public Set<String> getFuture() {
		return future;
	}

	public Set<String> getPast() {
		return past;
	}	

	public void addFuture(String f){
		future.add(f);
	}
	
	public void addPast(String p){
		past.add(p);
	}
	
}