package org.artemisproject.ddet.handler;
import static org.objectweb.asm.Opcodes.*;

import org.artemisproject.ddet.event.BranchEvent;
import org.artemisproject.ddet.event.EndEvent;
import org.artemisproject.ddet.event.NeiComEvent;
import org.artemisproject.ddet.event.SelfComEvent;
import org.artemisproject.ddet.event.StartEvent;
import org.artemisproject.ddet.event.TransactionEvent;
import org.objectweb.asm.tree.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import java.util.logging.Logger;
public class Instrumenter {
	
	private ClassNode cn;
	private MethodNode mn;
	private InsnList insns;
	private int localNum;
	private String choice = "exp";
	private String states = "";
	private AbstractInsnNode AttributeInsertPoint;
	private String ddmFullyQuaName;
	private int localNumDdm;
	
	
	private final static Logger LOGGER = Logger.getLogger(Instrumenter.class
			.getName());

	public static Logger getLogger() {
		return LOGGER;
	}
	public Instrumenter(){		
	}
	public Instrumenter(ClassNode cn){
		this.cn = cn;			
	}
	public Instrumenter(ClassNode cn, MethodNode mn, String states, String ddmFullyQuaName, String choice){
		this.cn = cn;
		this.mn = mn;
		insns = mn.instructions;
		localNum = mn.maxLocals;		
		this.states = states;
		AttributeInsertPoint = insns.getFirst();
		this.ddmFullyQuaName = ddmFullyQuaName;
		this.choice = choice;
		
	}
	public Instrumenter(ClassNode cn, MethodNode mn, String ddmFullyQuaName, String choice){
		this.cn = cn;
		this.mn = mn;
		insns = mn.instructions;
		localNum = mn.maxLocals;
		AttributeInsertPoint = insns.getFirst();
		this.ddmFullyQuaName = ddmFullyQuaName;
		this.choice = choice;
	}
	
	public void setDdmFullyQuaName(String dfqn){
		ddmFullyQuaName = dfqn;
	}
	/**
	 * insert field in a class
	 * @param cn
	 */
	public boolean instrumentClassField(String fieldName,String fieldDesc) {
		for (FieldNode fn : (List<FieldNode>) cn.fields) {
			if (fieldName.equals(fn.name)) {
				LOGGER.warning("There has already a " + fieldName + " in the "
						+ cn.name);
				return false;
			}
		}
		cn.fields.add(new FieldNode(ACC_PRIVATE, fieldName, fieldDesc, null,
				null));
		LOGGER.fine("Add field  " + fieldName + " success!");
		return true;
	}
	/**
	 * insert transaction id---a method field in a method
	 * @param mn
	 * @param choice
	 */
	public int instrumentTransactionID(String choice) {			
		if (choice.equals("exp")) {
			InsnList trigstart = new InsnList();
			// insert mn.name.thread id
			trigstart.add(new TypeInsnNode(NEW, "java/lang/Integer"));
			trigstart.add(new InsnNode(DUP));
			trigstart.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Thread",
					"currentThread", "()Ljava/lang/Thread;"));
			trigstart.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object",
					"hashCode", "()I"));
			trigstart.add(new MethodInsnNode(INVOKESPECIAL,
					"java/lang/Integer", "<init>", "(I)V"));
			trigstart.add(new MethodInsnNode(INVOKEVIRTUAL,
					"java/lang/Integer", "toString", "()Ljava/lang/String;"));
			trigstart.add(new VarInsnNode(ASTORE, localNum));
			trigstart.add(new TypeInsnNode(NEW, "java/lang/StringBuilder"));
			trigstart.add(new InsnNode(DUP));
			trigstart.add(new LdcInsnNode(cn.name + ":" + mn.name + mn.desc
					+ ":"));
			// ///////////////
			trigstart.add(new MethodInsnNode(INVOKESPECIAL,
					"java/lang/StringBuilder", "<init>",
					"(Ljava/lang/String;)V"));
			trigstart.add(new VarInsnNode(ALOAD, localNum));
			trigstart.add(new MethodInsnNode(INVOKEVIRTUAL,
					"java/lang/StringBuilder", "append",
					"(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
			trigstart.add(new LdcInsnNode(":"));
			trigstart.add(new MethodInsnNode(INVOKEVIRTUAL,
					"java/lang/StringBuilder", "append",
					"(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
			trigstart.add(new TypeInsnNode(NEW, "java/util/Random"));
			trigstart.add(new InsnNode(DUP));
			trigstart.add(new MethodInsnNode(INVOKESPECIAL, "java/util/Random",
					"<init>", "()V"));
			trigstart.add(new IntInsnNode(SIPUSH, 10000));
			trigstart.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/Random",
					"nextInt", "(I)I"));
			trigstart.add(new MethodInsnNode(INVOKEVIRTUAL,
					"java/lang/StringBuilder", "append",
					"(I)Ljava/lang/StringBuilder;"));
			trigstart.add(new MethodInsnNode(INVOKEVIRTUAL,
					"java/lang/StringBuilder", "toString",
					"()Ljava/lang/String;"));
			trigstart.add(new VarInsnNode(ASTORE, localNum));
			insns.insertBefore(AttributeInsertPoint, trigstart);
			localNum++;
			mn.maxLocals++;
			return localNum - 1;
		} else {
			if (choice.equals("conup")) {
				String fieldName = "_txLifecycleMgr";
				String fieldDesc = "Lcn/edu/nju/moon/conup/spi/datamodel/TxLifecycleManager;";
				this.instrumentClassField(fieldName, fieldDesc);
				this.instrumentCreateTransactionID(fieldName, fieldDesc, localNum, AttributeInsertPoint);
				localNum++;
				mn.maxLocals++;
				return localNum - 1;
			} else {
				LOGGER.warning("The transaction id is not generated successfully!");
				return -1;
			}
		}
		
	}
	public void instrumentDDM (int localNumTranId){
		InsnList instrumentation = new InsnList();		
		//get its lddm object
		instrumentation.add(new VarInsnNode(ALOAD, localNumTranId));
		instrumentation.add(new LdcInsnNode(states));
		instrumentation.add(new LdcInsnNode(cn.name+":"+mn.name));
		instrumentation
		.add(new MethodInsnNode(INVOKESTATIC, ddmFullyQuaName, "getInstance", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)L"+ddmFullyQuaName+";"));
		instrumentation.add(new VarInsnNode(ASTORE, localNum));
		insns.insertBefore(AttributeInsertPoint, instrumentation);
		this.localNumDdm = localNum;
		localNum++;		
		mn.maxLocals++;
	}
	
	public void QuieinstrumentDDM (int localNumTranId){
		InsnList instrumentation = new InsnList();		
		//get its lddm object
		instrumentation.add(new VarInsnNode(ALOAD, localNumTranId));		
		instrumentation
		.add(new MethodInsnNode(INVOKESTATIC, ddmFullyQuaName, "getInstance", "(Ljava/lang/String;)L"+ddmFullyQuaName+";"));
		instrumentation.add(new VarInsnNode(ASTORE, localNum));
		insns.insertBefore(AttributeInsertPoint, instrumentation);
		this.localNumDdm = localNum;
		localNum++;		
		mn.maxLocals++;
	}
	public void InstrumentTransactionEvent(TransactionEvent teve){
		
		InsnList instruments;
		
		if(teve instanceof StartEvent){
			//insert start event at the begin of the transaction
			instruments = ((StartEvent) teve).getInstrumentation(ddmFullyQuaName, localNumDdm);
			insns.insertBefore(AttributeInsertPoint, instruments);			
		}
		else{
			if(teve instanceof NeiComEvent){
				//insert BefNeiComEvent before the event starts, for precise past set
				instruments = ((NeiComEvent) teve).getPreInstrumentation(ddmFullyQuaName, localNumDdm);
				insns.insert(((NeiComEvent) teve).getPreInstrumentPoint(), instruments);
				
				//insert NeiComEvent after the events, for precise future set
				instruments = ((NeiComEvent) teve).getInstrumentation(ddmFullyQuaName, localNumDdm);
				insns.insert(((NeiComEvent) teve).getInstrumentPoint(), instruments);
			}
			else{
				if((teve instanceof SelfComEvent)){
					//insert SelfComEvent before the events, for associate the two transactions
					instruments = ((SelfComEvent) teve).getInstrumentation(ddmFullyQuaName, localNumDdm);
					insns.insert(((SelfComEvent) teve).getInstrumentPoint(), instruments);
				}
				else{
					if(teve instanceof EndEvent){
						//insert EndEvent before the end events
						instruments = ((EndEvent) teve).getInstrumentation(ddmFullyQuaName, localNumDdm);
						insns.insert(((EndEvent) teve).getPreInstruPoint(), instruments);
					}
					else{
						if(teve instanceof BranchEvent){
							// complex
							this.instrumentBranchEvent((BranchEvent) teve, ((BranchEvent) teve).getIsMeetingNode());
						}
					}
				}
			}
		}
		
	}
	/**
	 * 
	 * @param TxlcMgr
	 * 		TxLifecycleManager name for creating transaction id.
	 * @param TxlcMgrDesc
	 * 		TxLifecycleManager desc for creating transaction id.
	 * @param LocalNumTranId
	 * @param an
	 * 		insert point
	 */
	public void instrumentCreateTransactionID(String TxlcMgr, String TxlcMgrDesc,int LocalNumTranId, AbstractInsnNode an) {
		InsnList trigstart = new InsnList();
		this.instrumentClassField(TxlcMgr, TxlcMgrDesc);
		trigstart.add(new VarInsnNode(ALOAD, 0));
		trigstart
				.add(new FieldInsnNode(GETFIELD, cn.name, TxlcMgr, TxlcMgrDesc));
		trigstart.add(new MethodInsnNode(INVOKEVIRTUAL, TxlcMgrDesc.substring(
				1, TxlcMgrDesc.length() - 1), "createID",
				"()Ljava/lang/String;"));
		// System.out.println("TxlcMgrDesc.substring(1,TxlcMgrDesc.length()-1) = "+TxlcMgrDesc.substring(1,TxlcMgrDesc.length()-1));
		trigstart.add(new VarInsnNode(ASTORE, LocalNumTranId));
		insns.insertBefore(an, trigstart);
	}
	
	/**
	 * 
	 * @param branEve
	 * @param meetingNode
	 *  whether the dst node is a meeting node
	 */
	public void instrumentBranchEvent(BranchEvent branEve,boolean meetingNode){
		AbstractInsnNode src = branEve.getAbstractNode();
		AbstractInsnNode dst = branEve.getDstNode();
		InsnList instruments = branEve.getInstrumentation(ddmFullyQuaName, localNumDdm);
		if (meetingNode) {
				// it is a meeting note
				if (src instanceof JumpInsnNode) {
					//if the jump to label is just the dst
					if (((JumpInsnNode) src).label
							.equals(dst)) {
						LabelNode nextOne = changeJumpLabel(branEve,
								(LabelNode) dst);
						((JumpInsnNode) src).label = nextOne;						
					} else {						
						insns.insert(src, instruments);
					}
				} else {
					if (src instanceof TableSwitchInsnNode) {
						LabelNode dflt = ((TableSwitchInsnNode) src).dflt;
						if (dflt.equals(dst)) {
							LabelNode nextOne = changeJumpLabel(branEve,
									dflt);
							((TableSwitchInsnNode) src).dflt = nextOne;
						} else {
							List<LabelNode> allLabel = ((TableSwitchInsnNode) src).labels;
							List<LabelNode> newLabels = new LinkedList<LabelNode>();
							for (LabelNode label : allLabel) {
								if (label.equals(dst)) {
									LabelNode nextOne = changeJumpLabel(branEve, label);
									newLabels.add(nextOne);
									LOGGER.fine("TableSwitchInsnNode labels changed!");
								} else {
									newLabels.add(label);
								}
							}
							((TableSwitchInsnNode) src).labels = newLabels;
						}
					} else {
						if (src instanceof LookupSwitchInsnNode) {
							LabelNode dflt = ((LookupSwitchInsnNode) src).dflt;
							if (dflt.equals(dst)) {
								LabelNode nextOne = changeJumpLabel(branEve,
										dflt);
								((LookupSwitchInsnNode) src).dflt = nextOne;
							} else {
								List<LabelNode> allLabel = ((LookupSwitchInsnNode) src).labels;
								List<LabelNode> newLabels = new LinkedList<LabelNode>();
								for (LabelNode label : allLabel) {
									if (label.equals(dst)) {
										LabelNode nextOne = changeJumpLabel(branEve, label);
										newLabels.add(nextOne);
									} else {
										newLabels.add(label);
									}
								}
								((LookupSwitchInsnNode) src).labels = newLabels;
							}
						} else {
							LOGGER.info("Other jump info we have to analyze further!");
						}
					}
				}
			} else {
				// it has only one previous node in CFG
				while (dst instanceof LabelNode
						|| dst instanceof LineNumberNode) {
					dst = dst.getNext();
				}
				insns.insertBefore(dst, instruments);		
			}	
	}
	
	public LabelNode changeJumpLabel(BranchEvent beve, LabelNode sourceLabel) {
			InsnList intru;
			LabelNode nextOne = new LabelNode();
			//if its previous node is a goto statement, a new label is produced firstly and then jump to this new label
			if (sourceLabel.getPrevious().getOpcode() == GOTO) {
				intru = beve.getInstrumentationAfterGOTO(nextOne, ddmFullyQuaName, localNumDdm);
				insns.insertBefore(sourceLabel, intru);
			} else {
				//if it runs from its previous node, then the instrumentation should not be executed
				intru = beve.getInstrumentationMeetingNode(sourceLabel, nextOne,ddmFullyQuaName, localNumDdm);
				insns.insertBefore(sourceLabel, intru);
			}
			return nextOne;
		}
			
	
/**
 * only insert startEvent and endEvent
 * @param cn
 * @param insns
 * @param localNum
 */
	public void instrumentNoEvents(List<TransactionEvent> traneve){
		
		if (choice.equals("conup")) {
			
			// insert transaction id, this id is generated according to the user
			int localNumTranId = instrumentTransactionID(choice);
			QuieinstrumentDDM(localNumTranId);
			
			// insert startEvent
			StartEvent se = new StartEvent();
			insns.insertBefore(AttributeInsertPoint,
					se.getInstrumentation(ddmFullyQuaName, localNumDdm));
			// insert EndEvent
			for (TransactionEvent atranEve : traneve) {
				if (atranEve instanceof EndEvent)
					insns.insert(((EndEvent) atranEve).getPreInstruPoint(),
							((EndEvent) atranEve).getInstrumentation(
									ddmFullyQuaName, localNumDdm));
			}
		}
		
	}
	/**
	 * insert methodname, states, transitions as attributes of the annotation
	 * @param annaDesc
	 *  annatation desc, e.g.,"Ljavax/aejb/Transaction;"
	 */
	public void instrumentDDAinAnnatation(String annaDesc, List<String> stateList, List<String> nextList){
		
		Iterator<AnnotationNode> iter = mn.visibleAnnotations.iterator();
		while (iter.hasNext()) {
			AnnotationNode an = iter.next();
			if (an.desc.equals(annaDesc)) {
				List<Object> valuenew = new LinkedList<Object>();
				valuenew.add("name");
				valuenew.add(mn.name);
				valuenew.add("states");
				valuenew.add(stateList);
				valuenew.add("next");
				valuenew.add(nextList);
				an.values = valuenew;
//				System.out.println(an.values);
			}
		}
	}
	
	/**
	 * only instrument state event and end event, for quiescence
	 */
	public void QuieInstrument(){
		// insert transaction id, this id is generated according to the user
		int localNumTranId = instrumentTransactionID(choice);
		QuieinstrumentDDM(localNumTranId);
		// insert startEvent
		StartEvent se = new StartEvent();
		insns.insertBefore(AttributeInsertPoint,
				se.getInstrumentation(ddmFullyQuaName, localNumDdm));
		// insert EndEvent
		Iterator<AbstractInsnNode> i = insns.iterator();
		while(i.hasNext()){
			AbstractInsnNode node = i.next();
			if ((node.getOpcode() >= IRETURN && node.getOpcode() <= RETURN)
					|| node.getOpcode() == ATHROW){
				EndEvent endeve = new EndEvent(node);
				InsnList instruments = endeve.getInstrumentation(ddmFullyQuaName, localNumDdm);
				insns.insert(endeve.getPreInstruPoint(), instruments);
			}
		}
		LOGGER.info("Instrumentation for quiescence has been done successfully!");
	}
	


}
