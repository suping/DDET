package org.artemisproject.ddet.event;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import org.objectweb.asm.tree.*;

public class StartEvent extends TransactionEvent{
	
	
	public StartEvent(){
		nextState = 0;		
	}	
	
	public InsnList getInstrumentation(String ddmFullyQuaName,int localNumDdm){
		InsnList instrumentation = new InsnList();				
		// insert start inf
		instrumentation.add(new VarInsnNode(ALOAD, localNumDdm));	
		instrumentation
				.add(new MethodInsnNode(
						INVOKEVIRTUAL,
						ddmFullyQuaName,
						"triggerStart", "()V"));
		return instrumentation;
	}

}
