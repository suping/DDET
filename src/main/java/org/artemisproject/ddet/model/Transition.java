package org.artemisproject.ddet.model;


import java.util.logging.Logger;

/**
 * 
 * 
 * @author Ping Su<njupsu@gmail.com>
 */
public class Transition {
    private final static Logger LOGGER = Logger.getLogger(Transition.class.getName());
	
	public static Logger getLogger() {
		return LOGGER;
	}
	
	private int head;	
	private int tail;
	private String event;

	public Transition(int head, int tail, String event) {		
		super();
		this.head = head;
		this.tail = tail;
		this.event = event;		
		LOGGER.fine(head + "-" + event + "-" + tail);
	}

	/**
	 * @param head
	 * @param tail
	 * @param event
	 */
	public int getHead() {
		return head;
	}

	/**
	 * @param head
	 *            the head to set
	 */
	public void setHead(int head) {
		this.head = head;
	}

	/**
	 * @return the tail
	 */
	public int getTail() {
		return tail;
	}

	/**
	 * @param tail
	 *            the tail to set
	 */
	public void setTail(int tail) {
		this.tail = tail;
	}

	/**
	 * @return the event
	 */
	public String getEvent() {
		return event;
	}

	/**
	 * @param event
	 *            the event to set
	 */
	public void setEvent(String event) {
		this.event = event;
	}
	/**
	 * 
	 * @return
	 */

	public String componentEventUsed() {
		if (event.contains("COM")) {
			return event.split("\\.")[1];			
		}
		else{
			if(event.contains("NEST")){
				return event;
			}
			else
			{
				return null;
			}
		}		

	}

}
