package org.artemisproject.ddet.event;


import org.objectweb.asm.tree.*;

public abstract class TransactionEvent {
	
	AbstractInsnNode an;	
	String event;
	int nextState;
	
	
	public void setAbstractNode(AbstractInsnNode ain){
		an = ain;
	}
	public AbstractInsnNode getAbstractNode(){
		return an;
	}	
	 
	public void setNextState(int nextSta){
		nextState = nextSta;
	}
	public String getEvent(){
		return event;
	}
	
	abstract InsnList getInstrumentation(String ddmFullyQuaName,int localNumDdm);


}
