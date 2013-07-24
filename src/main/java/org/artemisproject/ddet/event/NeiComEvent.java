package org.artemisproject.ddet.event;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2_X2;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.IALOAD;
import static org.objectweb.asm.Opcodes.IASTORE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.LXOR;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.SALOAD;
import static org.objectweb.asm.Opcodes.SASTORE;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;


/**
 * Component-invoked event info like this:
 * COM.ComponentName;methodName.location
 */
public class NeiComEvent extends TransactionEvent{
	
	String neiComName;
	
	public NeiComEvent( AbstractInsnNode neiCom, String event){
		an = neiCom;		
		super.event = event;
		neiComName = event.split("\\.")[1];		
	}
	public NeiComEvent(AbstractInsnNode neiCom, String event, int nextState){
		super.an = neiCom;
		super.nextState = nextState;
		super.event = event;
		neiComName = event.split("\\.")[1];		
	}
	public InsnList getInstrumentation(String ddmFullyQuaName,int localNumDdm){
		InsnList instrumentation = new InsnList();		
		instrumentation.add(new VarInsnNode(ALOAD, localNumDdm));					
		instrumentation.add(new LdcInsnNode(neiComName));
		instrumentation.add(new IntInsnNode(BIPUSH, nextState));
		instrumentation.add(new MethodInsnNode(
				INVOKEVIRTUAL,
				ddmFullyQuaName,
				"triggerCom", "(Ljava/lang/String;I)V"));
		return instrumentation;
	}
	public AbstractInsnNode getInstrumentPoint(){
		AbstractInsnNode ilPost = an.getNext();
		int op = ilPost.getOpcode();
		// save the results of the method invocation
		while ((op >= ISTORE && op <= ASTORE)
				|| (op >= IASTORE && op <= SASTORE) || op == PUTSTATIC
				|| op == PUTFIELD || op == POP || op == POP2) {
			ilPost = ilPost.getNext();
			op = ilPost.getOpcode();
		}
		int opPost = ilPost.getNext().getOpcode();
		if ((op >= IADD && op <= LXOR)
				&& ((opPost >= ISTORE && opPost <= ASTORE)
						|| (opPost >= IASTORE && opPost <= SASTORE)
						|| opPost == PUTSTATIC || opPost == PUTFIELD)) {
			return ilPost.getNext();
		} else {
			return an;
		}
	}
	public InsnList getPreInstrumentation(String ddmFullyQuaName,int localNumDdm){
		InsnList instrumentation = new InsnList();		
		instrumentation.add(new VarInsnNode(ALOAD, localNumDdm));					
		instrumentation.add(new LdcInsnNode(neiComName));		
		instrumentation.add(new MethodInsnNode(
				INVOKEVIRTUAL,
				ddmFullyQuaName,
				"triggerBeforeCom", "(Ljava/lang/String;)V"));
		return instrumentation;
	}
	public AbstractInsnNode getPreInstrumentPoint(){
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
