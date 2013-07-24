package org.artemisproject.ddet.event;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2_X2;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.IALOAD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.SALOAD;


import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class SelfComEvent extends TransactionEvent{
	
	String dependentTranId;
	
	public SelfComEvent(AbstractInsnNode selfCom, String event, int nextState){
		super.an = selfCom;
		super.nextState = nextState;
		super.event = event;
		dependentTranId = event.split("\\.")[1];
		
	}
	public SelfComEvent(AbstractInsnNode selfcom, String event) {		
		an = selfcom;		
		super.event = event;
		dependentTranId = event.split("\\.")[1];
	}
	
	public String getEvent(){
		return event;
	}
	public InsnList getInstrumentation(String ddmFullyQuaName,int localNumDdm){		
		InsnList instrumentation = new InsnList();		
		instrumentation.add(new VarInsnNode(ALOAD, localNumDdm));				
		instrumentation.add(new LdcInsnNode(dependentTranId));
		instrumentation.add(new IntInsnNode(BIPUSH,nextState));
		instrumentation.add(new MethodInsnNode(
				INVOKEVIRTUAL,
				ddmFullyQuaName,
				"nestStart", "(Ljava/lang/String;I)V"));
		return instrumentation;
	}
	public AbstractInsnNode getInstrumentPoint(){
		AbstractInsnNode ilPre = an.getPrevious();
		int op = ilPre.getOpcode();
		// get the parameters of the method invocation
		while ((op <= ALOAD && op >= ILOAD)
				|| (op <= IALOAD && op >= SALOAD)
				|| op == GETSTATIC || op == GETFIELD
				|| (op <= DUP2_X2 && op >= DUP)) {
			ilPre = ilPre.getPrevious();
			op = ilPre.getOpcode();
		}
		return ilPre;
	}

}
