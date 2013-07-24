package org.artemisproject.ddet.event;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2_X2;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.IALOAD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.LDC;
import static org.objectweb.asm.Opcodes.SALOAD;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class EndEvent extends TransactionEvent{
	
	public EndEvent(AbstractInsnNode end){
		an = end;		
	}
	
	public InsnList getInstrumentation(String ddmFullyQuaName,int localNumDdm){
		InsnList instrumentation = new InsnList();		
		instrumentation.add(new VarInsnNode(ALOAD, localNumDdm));				
		instrumentation.add(new MethodInsnNode(
				INVOKEVIRTUAL,
				ddmFullyQuaName,
				"triggerEnd", "()V"));	
		return instrumentation;
	}
	 public AbstractInsnNode getPreInstruPoint(){
		 AbstractInsnNode ilPre = an.getPrevious();
			int op = ilPre.getOpcode();

			// get the parameters of the return
			while ((op >= ACONST_NULL && op <= LDC)
					|| (op <= ALOAD && op >= ILOAD)
					|| (op <= IALOAD && op >= SALOAD)
					|| op == GETSTATIC || op == GETFIELD
					|| (op <= DUP2_X2 && op >= DUP)) {
				ilPre = ilPre.getPrevious();
				op = ilPre.getOpcode();
			}
			return ilPre;
	 }
}