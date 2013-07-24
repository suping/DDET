package org.artemisproject.ddet.event;


import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;


import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;

import org.objectweb.asm.tree.VarInsnNode;

/**
 * Branch event info like this: BranchType(if, or while).branchNum.location
 */
public class BranchEvent extends TransactionEvent{
	AbstractInsnNode dst;
	boolean isMeetingNode;
	
	public BranchEvent(AbstractInsnNode src, AbstractInsnNode dst,String event,int nextSta){
		super.an = src;
		super.event = event;
		super.nextState = nextSta;
		this.dst = dst;
		
	}
	public BranchEvent(AbstractInsnNode src, AbstractInsnNode dst,
			String event) {	
		super.an = src;
		super.event = event;		
		this.dst = dst;
	}


	public void setDstNode(AbstractInsnNode ain){
		dst = ain;
	}
	public AbstractInsnNode getDstNode(){
		return dst;
	}
	public void setIsMeetingNode(boolean dstMeetingNode){
		isMeetingNode = dstMeetingNode;
	}
	public boolean getIsMeetingNode(){
		return isMeetingNode;
	}
	
	public InsnList getInstrumentation(String ddmFullyQuaName,int localNumDdm){
		InsnList instrumentation = new InsnList();		
		instrumentation.add(new VarInsnNode(ALOAD, localNumDdm));			
		instrumentation.add(new IntInsnNode(BIPUSH, nextState));
		instrumentation.add(new MethodInsnNode(
				INVOKEVIRTUAL,
				ddmFullyQuaName,
				"nextState", "(I)V"));	
		return instrumentation;
	}
	public InsnList getInstrumentationAfterGOTO(LabelNode nextOne,String ddmFullyQuaName,int localNumDdm){
		InsnList instrumentation = new InsnList();	
		instrumentation.add(nextOne);
		instrumentation.add(new VarInsnNode(ALOAD, localNumDdm));			
		instrumentation.add(new IntInsnNode(BIPUSH, nextState));
		instrumentation.add(new MethodInsnNode(
				INVOKEVIRTUAL,
				ddmFullyQuaName,
				"nextState", "(I)V"));	
		return instrumentation;
	}
	public InsnList getInstrumentationMeetingNode(LabelNode soulab,LabelNode nextOne,String ddmFullyQuaName,int localNumDdm){
		InsnList instrumentation = new InsnList();	
		instrumentation.add(new JumpInsnNode(GOTO, soulab));
		instrumentation.add(nextOne);
		instrumentation.add(new VarInsnNode(ALOAD, localNumDdm));			
		instrumentation.add(new IntInsnNode(BIPUSH, nextState));
		instrumentation.add(new MethodInsnNode(
				INVOKEVIRTUAL,
				ddmFullyQuaName,
				"nextState", "(I)V"));	
		return instrumentation;
	}
	public String getEvent() {
		// TODO Auto-generated method stub
		return event;
	}
	 
}
