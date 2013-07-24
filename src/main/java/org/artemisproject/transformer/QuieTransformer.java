package org.artemisproject.transformer;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.artemisproject.ddet.handler.Instrumenter;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class QuieTransformer extends Transformer{

	private final static Logger LOGGER = Logger
			.getLogger(QuieTransformer.class.getName());

	public static Logger getLogger() {
		return LOGGER;
	}
	
	public QuieTransformer(ClassNode cn, String choice, String ddmFullyQuaName){
		super(cn, choice, ddmFullyQuaName);
	}
	public QuieTransformer(ClassNode cn, String choice, String ddmFullyQuaName, String TxAnnotationDesc){
		super(cn, choice, ddmFullyQuaName, TxAnnotationDesc);
	}
	public QuieTransformer(ClassNode cn, String choice, String ddmFullyQuaName, Set<String> methodToAnalyze){
		super(cn, choice, ddmFullyQuaName, methodToAnalyze);
	}
	
	public void transform(){
		if (this.methodToAnalyze != null) {
			for (MethodNode mn : (List<MethodNode>) cn.methods) {
				if (methodToAnalyze.contains(mn.name)) {
					LOGGER.info("Begin analyzing the method: " + mn.name);
					Instrumenter instrumenter = new Instrumenter(cn, mn,
							ddmFullyQuaName, choice);
					instrumenter.QuieInstrument();
				}
			}
		} else {
			if (!TxAnnotationDesc.isEmpty()) {
				for (MethodNode mn : (List<MethodNode>) cn.methods) {
					if (isTransaction(mn, TxAnnotationDesc)) {
						Instrumenter instrumenter = new Instrumenter(cn, mn,
								ddmFullyQuaName, choice);
						instrumenter.QuieInstrument();
					}
				}
			}
		}

	}
}
