package org.artemisproject.ddet.handler;


import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.RETURN;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

import org.artemisproject.ddet.event.BranchEvent;
import org.artemisproject.ddet.event.EndEvent;
import org.artemisproject.ddet.event.NeiComEvent;
import org.artemisproject.ddet.event.SelfComEvent;
import org.artemisproject.ddet.event.TransactionEvent;
import org.artemisproject.ddet.model.ControlFlow;
import org.artemisproject.ddet.model.DDA;
import org.artemisproject.ddet.model.State;
import org.artemisproject.ddet.model.Transition;
import org.artemisproject.transformer.DyanTransformer;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;



public class DDADeriver {
	private final static Logger LOGGER = Logger.getLogger(DDADeriver.class
			.getName());

	public static Logger getLogger() {
		return LOGGER;
	}
	
	ClassNode cn;
	
	MethodNode mn;

	/**
	 * DDA infor including states and transitions, like: si-ei-sj
	 */
	private DDA dda = new DDA();

	/**
	 * CFG of the method
	 */
	private ControlFlow controlflow;
	
	/**
	 * All neighbor components will use potentially
	 */
	private Set<String> com;
	
	/**
	 * Invoke component service firstly. Note: can not use Set, because
	 * methodInsNode don't implement comparable
	 */
	private List<AbstractInsnNode> firstCall = new LinkedList<AbstractInsnNode>();
	
	private List<AbstractInsnNode> NeiComEveNodes = new LinkedList<AbstractInsnNode>();
	/**
	 * the nestee has no events
	 */
	private List<String> validNestEvents = new LinkedList<String>();
	/**
	 * no use jump events, in other words, they the previous and post state content is the same.
	 */
	private List<String> validBranchEvents = new LinkedList<String>();	
	/**
	 * for connecting the states correctly
	 */
	private Set<Integer> end = new ConcurrentSkipListSet<Integer>();
	/**
	 * whether the bytecode is analyzed;
	 */
	private int[] isAnalyze;

	public DDADeriver(Set<String> neiComs) {
		com = neiComs;
	}

	
	public DDADeriver(ClassNode cn, MethodNode mn, ControlFlow cfg, Set<String> neiComs) {
		this.cn = cn;
		this.mn = mn;
		controlflow = cfg;
		com = neiComs;
	}
	

	/**
	 * 
	 * @param n
	 *            the number of bytecodes in the program to be analyzed
	 */
	public void initIsAnalyze(int n) {
		isAnalyze = new int[n];
		for (int i = 0; i < n; i++) {
			isAnalyze[i] = 0;
		}
	}

	public int[] getIsAnalyze() {
		return isAnalyze;
	}

	public void printIsAnalyzed(int n) {
		for (int i = 0; i < n; i++) {
			System.out.println(i + ": " + isAnalyze[i]);
		}
	}
	
	public DDA getDDA() {
		return dda;
	}
	
	public List<AbstractInsnNode> getFirstCallNodes() {
		return firstCall;
	}

	/**
	 * Set the components or services the method may use.
	 * 
	 * @param c
	 */
	public void setCom(Set<String> neiComs) {
		com = neiComs;
	}

	public String getServiceName(String desc) {
//		System.out.println(desc + " Servic Name is " + desc);
		String[] fields = desc.split("/");
		String serviceName = fields[fields.length - 1];
//		System.out.println(desc + " Servic Name is " + serviceName);
		return serviceName;
	}
	
	
	/**
	 * Find the meeting node(have multi-source nodes)
	 * 
	 * @param srcNum
	 *            : number of every node's source nodes in insns
	 * @param insns
	 * @return
	 */

	public List<AbstractInsnNode> getMeetingNodes(int[] srcNum, InsnList insns) {
		int n = insns.size();
		List<AbstractInsnNode> multiSrcNode = new LinkedList<AbstractInsnNode>();
		for (int i = 0; i < n; i++) {
			if (srcNum[i] > 1) {
				multiSrcNode.add(insns.get(i));
			}
		}
		return multiSrcNode;
	}
	
	public Set<String> constructDDAwithFuture(){
		InsnList insns = mn.instructions;
		int insnNum = mn.instructions.size();
		initIsAnalyze(insnNum);
		int[] srcNum = new int[insnNum];
		for (int i = 0; i < insnNum; i++) {
			srcNum[i] = 0;
		}
		constructDDASkeleton(0, dda.getStart(), mn.instructions, srcNum, cn.name);
		correctState();
		List<AbstractInsnNode> meetingNodes = this.getMeetingNodes(srcNum,
				insns);
		// remove the empty and end events
		List<State> states = null;
		pruneEmptyandInvalidEvents(states);
		Set<String>[] future = this.calculateFuture();
		// get all the no use nest event
		states = setStates(cn, future);
		State startState = this.getState(dda.getStart(), states);
		// When the start Future set is null, we won't insert anything.
		if (startState.getFuture().size() == 0) {
			// LOGGER.info("-------- "+mn.name+" need not insert");
			System.out.println("-------- " + mn.name + " need not insert!!");			
			return new ConcurrentSkipListSet<String>();
		} else {
			LOGGER.info("************ " + mn.name + "'s DDA info: ");
			//remove valid nest events
			pruneEmptyandInvalidEvents(states);
			this.findValidBranchEvent(states);
			startState = this.getState(dda.getStart(), states);
			if (states.indexOf(startState) != 0) {
				states.remove(startState);
				states.add(0, startState);
			}	
			dda.setStatesDDA(states);
			removeInvalidTransactionEvent();
//			System.out.println("States size: "+states.size()+", transition size: "+ );			
			setTransactionEventNextStateIndex(states);
			setBranchDstIsMeetingNode(meetingNodes);
			return startState.getFuture();
		}
	}
	
	public int getStateIndexByEvent(String Sevent,List<State> states){
		Transition tran = dda.getEvent(Sevent);
		if(tran == null){
			System.out.println("There is no the event:"+ Sevent);
			return -1;
		}
		else{
		int tailLoc = tran.getTail();
		return this.getStateIndex(tailLoc, states);}
		
	}
	
	
	public void setTransactionEventNextStateIndex(List<State> states){
		for(TransactionEvent teve : dda.getTransactionEvent()){
			if(teve instanceof NeiComEvent || teve instanceof SelfComEvent || teve instanceof BranchEvent){
				String event = teve.getEvent();
				int nextSta = getStateIndexByEvent(event,states);
				teve.setNextState(nextSta);
			}
		}
	}
	
	public void setBranchDstIsMeetingNode(List<AbstractInsnNode> meetingNodes){
		for(TransactionEvent teve : dda.getTransactionEvent()){
			if(teve instanceof BranchEvent){
				AbstractInsnNode dstNode = ((BranchEvent) teve).getDstNode();
				if(meetingNodes.contains(dstNode)){
					((BranchEvent) teve).setIsMeetingNode(true);
				}
				else{
					((BranchEvent) teve).setIsMeetingNode(false);
				}
			}
		}
	}

	/**
	 * whether the given service is a component inovoked event
	 * 
	 * @param service
	 * @return component/service
	 */
	@Deprecated
	public String isCom(String service) {
		for (String s : com) {
			if (s.split("/")[1].equals(service)) {
				return s;
			}
		}
		return null;
	}

	/**
	 * recognize all the state of the method for tuscany application
	 * 
	 * @param src
	 * @param last_state
	 * @param insns
	 * @param srcNum
	 *            number of source nodes of the src
	 */
	public void constructDDASkeleton(int src, int last_state, InsnList insns,
			int[] srcNum,String cnName) {
		srcNum[src]++;
		if (last_state == dda.getStart()&&!dda.getStates().contains(last_state)) {
			dda.addState(last_state);			
		}
		if (dda.getStates().contains(src) && (src != last_state)) {

		}

		if (isAnalyze[src] == 0) {
			AbstractInsnNode an = insns.get(src);
			if ((an.getOpcode() >= IRETURN && an.getOpcode() <= RETURN)
					|| an.getOpcode() == ATHROW) {
				dda.addState(src);
				isAnalyze[src] = 1;	
				dda.addEvent(new Transition(last_state, src, "end"));				
				end.add(src);
				EndEvent endeve = new EndEvent(an);
				dda.addTransactionEvent(endeve);
			} else {
				if (controlflow.getDstSize(src) == 1) {					
					int next = controlflow.getOneDst(src);
					if (an instanceof MethodInsnNode) {
						isAnalyze[src] = 1;						
						MethodInsnNode method = ((MethodInsnNode) an);
						String methodOwner = method.owner;
						if(methodOwner.equals(cnName)){
//							 System.out.println(an.toString()+"'owner is "+methodOwner+", method name is "+method.name+", method desc is "+method.desc);
							//nest invocation
							String e = "NEST." + method.name +method.desc+ "." + src;							
							
							SelfComEvent sce = new SelfComEvent(an,e);
							dda.addTransactionEvent(sce);
							if (!dda.getStates().contains(src)) {
								dda.addState(src);
							}
							if (src != last_state) {
								dda.addEvent(new Transition(last_state,
										src, ""));
							}
							if (!dda.getStates().contains(next)) {
								dda.addState(next);
							}
							dda.addEvent(new Transition(src, next, e));

							constructDDASkeleton(next, next, insns, srcNum, cnName);
						}else{				
							//component invoked event						
						String serName = getServiceName(methodOwner);
//						System.out.println(an.toString()+"'owner is "+method.owner+" , "+serName);
						if (com.contains(methodOwner)) {
							// System.out.println(an.toString()+"-------------owner is "+method.owner);							
							String e = "COM." + serName + "." + src;							
							NeiComEveNodes.add(an);
							NeiComEvent nce = new NeiComEvent(an,e);
							dda.addTransactionEvent(nce);
							if (!dda.getStates().contains(src)) {
								dda.addState(src);
							}
							if (src != last_state) {
								dda.addEvent(new Transition(last_state,
										src, ""));
							}
							if (!dda.getStates().contains(next)) {
								dda.addState(next);
							}
							dda.addEvent(new Transition(src, next, e));

							constructDDASkeleton(next, next, insns, srcNum, cnName);
						} else {
							constructDDASkeleton(next, last_state, insns, srcNum, cnName);
						}
//						}
						}}
					else {
						if (an.getOpcode() == GOTO) {							
							isAnalyze[src] = 1;
							if (!dda.getStates().contains(src)) {
								dda.addState(src);
							}
							if (src != last_state) {
								dda.addEvent(new Transition(last_state,
										src, ""));
							}

							if (!dda.getStates().contains(next)) {
								dda.addState(next);
							}
							dda.addEvent(new Transition(src, next, ""));
							constructDDASkeleton(next, next, insns, srcNum, cnName);
						} else {
							isAnalyze[src] = 1;
							constructDDASkeleton(next, last_state, insns, srcNum, cnName);
						}
					}
					
				} else {
					int dst = -1;
					int k = -1;
					isAnalyze[src] = 1;
					
					// bytecode not a goto, but a jumpInsnNode
					if (!dda.getStates().contains(src)) {
						dda.addState(src);
					}
					if (src != last_state) {
						dda.addEvent(new Transition(last_state, src, ""));
					}
					Iterator<Integer> iter = controlflow.getDst(src).iterator();
					while(iter.hasNext()){
						dst = iter.next();
						k++;
						AbstractInsnNode dstNode = insns.get(dst);
						if (!dda.getStates().contains(dst)) {
							dda.addState(dst);
						}
						dda.addEvent(new Transition(src, dst, "branch." + k
								+ "." + src));
						BranchEvent beve = new BranchEvent(an,dstNode,"branch." + k
								+ "." + src);
						dda.addTransactionEvent(beve);						
						constructDDASkeleton(dst, dst, insns, srcNum, cnName);
					}
				}
			}
		}
	}
	
	/**
	 * 
	 */
	public void correctState(){
		Set<Integer> srcSet = new ConcurrentSkipListSet<Integer>();
		Set<Integer> dstSet = new ConcurrentSkipListSet<Integer>();
		for (int i = 0; i < dda.getEvents().size(); i++) {
			Transition event = dda.getEvents().get(i);
			srcSet.add(event.getHead());
			dstSet.add(event.getTail());
		}
		dstSet.removeAll(end);
		dstSet.removeAll(srcSet);
		LOGGER.info("The set need to corret: " + dstSet);
		if (!dstSet.isEmpty()) {
			for (Integer cor : dstSet) {
				//Should not meet branch node 	
				    LOGGER.info("we are correcting: " + cor);				    
					int dstTemp = controlflow.getOneDst(cor);
					while(!dda.getStates().contains(dstTemp)){						
						dstTemp = controlflow.getOneDst(dstTemp);
					}
					if(dda.getStates().contains(dstTemp)){
						dda.addEvent(new Transition(cor, dstTemp, ""));
					}
					else{
						LOGGER.warning("There is some thing wrong in" + cor);
					}
				
			}
		}
	}

	/**
	 * find the first request service node in the method. There may be none, one
	 * or two...
	 * 
	 * @param src
	 * @param insns
	 * @param branch
	 */
	public void getFirstRequestService(int src, InsnList insns, int branch) {
		if (NeiComEveNodes.size() > 0) {
			AbstractInsnNode an = insns.get(src);
			if ((an.getOpcode() >= IRETURN && an.getOpcode() <= RETURN)
					|| an.getOpcode() == ATHROW) {
				return;
			} else {
				if (NeiComEveNodes.contains(an)) {
					if (!firstCall.contains(an)) {
						firstCall.add(an);
					}
					return;
				} else {
					int branchNum = controlflow.getDstSize(src);
					if (branchNum > 1) {
						Set<Integer> dstSet = controlflow.getDst(src);
						int k = -1;
						for(int dst : dstSet){						
							getFirstRequestService(dst, insns, k);
						}
					} else {
						int dst = controlflow.getOneDst(src);
						getFirstRequestService(dst, insns, branch);
					}
				}
			}
		}

	}

	/**
	 * merge the state event is empty or the end, or the valid nest events
	 */
	public void pruneEmptyandInvalidEvents(List<State> states) {
		// dda.getStates().add(0, dda.getStart());
		boolean change = true;
		while (change) {
			change = false;
			List<Transition> e = new LinkedList<Transition>();
			e.addAll(dda.getEvents());
			for(Transition event: e) {							
				String eventCon = event.getEvent();	
				String nestMethod = null;
				if(eventCon.startsWith("NEST")){
					nestMethod = eventCon.split("\\.")[1];
				}
				if (eventCon.isEmpty()
						|| eventCon.contains("end")||(nestMethod != null&&validNestEvents.contains(nestMethod))) {
					int src = event.getHead();
					int dst = event.getTail();					
					dda.mergeStates(src, dst);
					dda.getEvents().remove(event);
					
					if(nestMethod != null && states != null){
						State head = getState(src, states);
						states.remove(head);
					}
						
					change = true;
				}				
			}
		}
	}
	
	/**
	 * remove valid events for instrumentation
	 */
	public void removeInvalidTransactionEvent(){
		List<TransactionEvent> traneve = dda.getTransactionEvent();
		List<TransactionEvent> invalidTraneve = new LinkedList<TransactionEvent>();
		String event = "";
		//remove no use nest events
		for(TransactionEvent teve : traneve){
			if(teve instanceof SelfComEvent){
				event = ((SelfComEvent) teve).getEvent();
				String nestmethod = event.split("\\.")[1];
				if(validNestEvents.contains(nestmethod)){					
					invalidTraneve.add(teve);
					LOGGER.fine("#####Remove no use nest event: "+event);
				}
			}
			else{
				if(teve instanceof BranchEvent){
					event = ((BranchEvent) teve).getEvent();
					if(validBranchEvents.contains(event))
					{
						invalidTraneve.add(teve);
						LOGGER.fine("#####Remove no use jump event: "+event);
					}
				}
			}
		}
		traneve.removeAll(invalidTraneve);
		
	}

	/**
	 * get future and past components in every state of DDA
	 */
	public Set<String>[] calculateFuture() {

		int states_count = dda.getStatesCount();
		List<Integer> s = dda.getStates();
		List<Transition> e = dda.getEvents();

		for (int i = 0; i < states_count; i++) {
			LOGGER.fine(i + ":" + s.get(i));
		}
		
		Set<String>[] future = new ConcurrentSkipListSet[states_count];
		for (int i = 0; i < states_count; i++)
			future[i] = new ConcurrentSkipListSet<String>();
		boolean changed = true;
		while (changed) {
			changed = false;
			for (int i = e.size() - 1; i >= 0; i--) {
				Transition event = (Transition) e.get(i);
				int head = event.getHead();
				int tail = event.getTail();
				String port = event.componentEventUsed();
				// LOGGER.fine(port);
				int headindex = s.indexOf(head);
				int tailindex = s.indexOf(tail);
				if (port != null && !future[headindex].contains(port)) {
					future[headindex].add(port);
					changed = true;
				}
				if (!dda.getEnd().contains(tail)){
					for(String temp : future[tailindex]){
						if (!future[headindex].contains(temp)) {
							future[headindex].add(temp);
							changed = true;
						}
					}
				}}
		}
		return future;

	}

	/**
	 * put the states content in every state
	 */
	public List<State> setStates(ClassNode cn, Set<String>[] future) {
		List<State> states = new LinkedList<State>();
		int states_count = dda.getStatesCount();
		for (int i = 0; i < states_count; i++) {
			//state location
			int stateLocation = dda.getStates().get(i);
			State state = new State(stateLocation);
			if (future[i].size() > 0) {
				Set<String> tempFuture = new ConcurrentSkipListSet<String>();
				for (String futu : future[i]) {
					if (futu.contains("NEST")) {
						//all nest or the TNest
						//get nest method's start state's future set
						String nestMethod = futu.split("\\.")[1];
						Set<String> realFuture = DyanTransformer.getInstance(cn)
								.getNestStateCon(nestMethod);
						if(realFuture.size() == 0){	
//							System.out.println("^^^^^^^no use event: "+ futu);
							if (!this.validNestEvents.contains(nestMethod)) {
								this.validNestEvents.add(nestMethod);
//								System.out.println("%%%% no events nest method: " + nestMethod);
							}					
						}
						else{
						for (String fu : realFuture) {
							if (!tempFuture.contains(fu)) {
								tempFuture.add(fu);
							}
						}}						
					} else {
						if (!tempFuture.contains(futu)) {
							tempFuture.add(futu);
						}
					}
				}
				state.setFuture(tempFuture);
			} else {
				state.setFuture(future[i]);
			}
			if(stateLocation == dda.getStart()){
				states.add(0, state);
			}
			else{
				states.add(state);
			}
//			LOGGER.info(i + ":" + future[i]);
		}
		return states;
	}

	/**
	 * whether two states are equal:the elements are equal ignore the order
	 * 
	 * @param si
	 * @param sj
	 * @return
	 */
	
	public boolean equalStateFuture(State si, State sj) {

		Set<String> futurei = si.getFuture();
		Set<String> futurej = sj.getFuture();
		return futurei.equals(futurej);
	}
/**
 * according to the given location of the state, get the State in the List<State>
 * @param i
 * @param states
 * @return
 */
	public State getState(int i,List<State> states) {
		if (states != null) {
			Iterator<State> s = states.iterator();
			while (s.hasNext()) {
				State state = s.next();
				if (state.getLoc() == i) {
					return state;
				}
			}
			return null;
		}
		return null;

	}
	/**
	 * return the state index according to the location of the state
	 * @param i
	 * @param states
	 * @return
	 */
	public int getStateIndex(int i,List<State> states) {
		if (states != null) {
			Iterator<State> s = states.iterator();
			while (s.hasNext()) {
				State state = s.next();
				if (state.getLoc() == i) {
					return states.indexOf(state);
				}
			}
			System.out.println("This Location: "+ i +" can't find!");
			return -1;
		}
		System.out.println("States is empty!");
		return -1;

	}


	/**
	 * merge the same state, the result states is the real states
	 */

	public void findValidBranchEvent(List<State> states) {
		List<Transition> e = new LinkedList<Transition>();
		e.addAll(dda.getEvents());
		// List<Integer> state = dda.getStates();		
		for(Transition event: e) {			
			String eventinf = event.getEvent();
			if (eventinf.startsWith("branch")) {
//				System.out.println("We are analyzing: "+ eventinf);
				int headindex = event.getHead();
				int tailindex = event.getTail();
				if (headindex == tailindex) {
					dda.getEvents().remove(event);
					validBranchEvents.add(eventinf);
//					System.out.println("&&&&&&&&&&&" + eventinf);
				} else {
					State head = getState(headindex, states);
					State tail = getState(tailindex, states);
					if (this.equalStateFuture(head, tail)) {
						LOGGER.fine("merge the same state(no use branch): "+headindex + " = " + tailindex);						
						dda.getEvents().remove(event);
						dda.mergeStates(headindex, tailindex);
						states.remove(head);
						validBranchEvents.add(eventinf);
//						System.out.println("&&&&&&&&&&&deleting: " + eventinf+" , "+headindex);
					}
				}
			}
		}

	}

	/**
	 * Opotimization: if a state's all next events has the same next state, we merge the state. Often the branch event.
	 */
	@Deprecated
	public List<String> mergeMultiEventHasSameNext(){
		List<String> noUseBranch = new LinkedList<String>();
		List<Integer> allstateLoc;
		boolean change = true;
		while(change){
			change = false;
			allstateLoc = new LinkedList<Integer>();
			allstateLoc.addAll(dda.getStates());
			for(int aStateloc : allstateLoc){
				System.out.println("Now analyzing: "+ aStateloc);
				List<Transition> nextEvents = dda.getNextEvent(aStateloc);
				Set<Integer> nextState = dda.getNextStateLocation(aStateloc);
				//all the next states are the same, but next events is multiple
				if(nextEvents.size() > 1 && nextState.size() == 1){
					
					change = true;
					Iterator<Integer> ns = nextState.iterator();
					int nsl = ns.next();
					dda.mergeStates(aStateloc, nsl);
					System.out.println("The same state need to collapse is "+ aStateloc+" , "+nsl);
					for(Transition e: nextEvents){
						dda.deleteEvent(e);
						LOGGER.fine("=========delete same event: "+e.getEvent());
						noUseBranch.add(e.getEvent());
					}
			}	
		}
		}
		return noUseBranch;
	}
	
}
