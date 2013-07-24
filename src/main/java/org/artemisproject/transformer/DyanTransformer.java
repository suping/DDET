package org.artemisproject.transformer;


import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;


import org.artemisproject.ddet.event.TransactionEvent;
import org.artemisproject.ddet.handler.CFGDeriver;
import org.artemisproject.ddet.handler.DDADeriver;
import org.artemisproject.ddet.handler.Instrumenter;
import org.artemisproject.ddet.model.ControlFlow;
import org.artemisproject.ddet.model.DDA;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;

import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.*;

/**
 * Analyze the tuscany program
 * 
 * @author Ping Su<njupsu@gmail.com>
 * 
 */
public class DyanTransformer extends Transformer{
	
	private final static Logger LOGGER = Logger
			.getLogger(DyanTransformer.class.getName());

	public static Logger getLogger() {
		return LOGGER;
	}
	/**
	 * Each class has a ExpProAna
	 */
	private static Hashtable<ClassNode, DyanTransformer> epaes = new Hashtable<ClassNode, DyanTransformer>();
	
	/**
	 * only for DDADeriver to get start future set.
	 * @param cn
	 * @return
	 */
	public static DyanTransformer getInstance(ClassNode cn) {
		if (epaes.containsKey(cn)) {
			return epaes.get(cn);
		} else {			
			return null;
		}
	}
	/**
	 *  Entry to this class
	 * @param cn
	 * @return
	 */
	public static DyanTransformer getInstance(ClassNode cn, String choice, String ddmFullyQuaName) {
		if (epaes.containsKey(cn)) {
			return epaes.get(cn);
		} else {
			DyanTransformer instance = new DyanTransformer(cn, choice, ddmFullyQuaName);
			epaes.put(cn, instance);
			return instance;
		}
	}
	
//	public static DyanTransformer getInstance(ClassNode cn, String choice, String ddmFullyQuaName, String TxAnnotationDesc, Set<String> neiComs) {
//		if (epaes.containsKey(cn)) {
//			return epaes.get(cn);
//		} else {
//			DyanTransformer instance = new DyanTransformer(cn, choice, ddmFullyQuaName, TxAnnotationDesc, neiComs);
//			epaes.put(cn, instance);
//			return instance;
//		}
//	}
	
	/**
	 * all methods' start future set for nest invocation
	 */
	private Hashtable<String, Set<String>> nestStateCon = new Hashtable<String, Set<String>>();
	/**
	 * get a specific method's start future set.
	 * @param cn
	 * @param method
	 * @return
	 */
	public Set<String> getNestStateCon(String method){
		if(!nestStateCon.containsKey(method)){
			transform(method);		
		}
		return nestStateCon.get(method);
	}
	/**
	 * Annatation desc for conup, tusscany to find all neighbor components
	 */
	String NeiComAnnatation = "";	
	
	/**
	 * neighbor components or neighbor services
	 */
	private Set<String> allServices = null;

	
	public Set<String> getAllServices(){
		return allServices;
	}

	public void setAllServices(Set<String> services) {
		if (allServices == null) {
			this.allServices = services;
		}
	}
	private DyanTransformer(ClassNode cn, String choice, String ddmFullyQuaName){
		super(cn, choice, ddmFullyQuaName);		
	}
	
	public void setNeiComAnnatation(String ref){
		NeiComAnnatation = ref;
		if (!NeiComAnnatation.isEmpty()) {
			allServices = this.findAllServices();
			LOGGER.info("allServices : "+allServices);
		}
	}
	
	/**
	 * transform a specified method, all neighbor components, ddmQualified Name have been given, only for dependent transactions 
	 * whose analysis is triggered by its root transaction
	 * @param cn
	 * @param mn
	 */
	private void transform(String methodSig){		
		if(!(nestStateCon.containsKey(methodSig))){
			MethodNode mn = getMethodNode(methodSig);
			LOGGER.info("Begin anlyze :"+ methodSig);
			CFGDeriver cfgderiver = new CFGDeriver(cn.name, mn);
			ControlFlow cfg = cfgderiver.getCFG();
			
			DDADeriver ddaDeriver = new DDADeriver(cn, mn, cfg, allServices);
			Set<String> startFuture = ddaDeriver.constructDDAwithFuture();				
			nestStateCon.put(mn.name+mn.desc, startFuture);
			DDA dda = ddaDeriver.getDDA();
			String states = dda.statesToString();
			List<TransactionEvent> tranEves = dda.getTransactionEvent();
//			System.out.println("Transaction event size:" +tranEves.size());
			if(startFuture.size() == 0){
				Instrumenter instrumenter = new Instrumenter(cn, mn, ddmFullyQuaName, choice);
				if(tranEves == null){
					System.out.println("The object instrumenter is null!");
				}
				else{
				instrumenter.instrumentNoEvents(tranEves);}
			}
			else{				
						
			Instrumenter instrumenter = new Instrumenter(cn, mn, states, ddmFullyQuaName);
			int localNumTranId = instrumenter.instrumentTransactionID(choice);
			instrumenter.instrumentDDM(localNumTranId);
			for(TransactionEvent teve : tranEves){
				instrumenter.InstrumentTransactionEvent(teve);
			}}
}
	}
	
	
	
	/**
	 * for conup tuscany
	 * Every possible component/service has a method with annotation @reference 
	 * @param cn
	 */
	public Set<String> findAllServices() {		
		Set<String> allServices = new ConcurrentSkipListSet<String>();
		for (MethodNode mn : (List<MethodNode>) cn.methods) {
			if (mn.visibleAnnotations != null) {
				Iterator<AnnotationNode> i = mn.visibleAnnotations.iterator();
				while (i.hasNext()) {
					AnnotationNode an = i.next();					
					if (NeiComAnnatation.equals(an.desc)) {
						InsnList insns = mn.instructions;
						Iterator<AbstractInsnNode> aInsn = insns.iterator();						
						while (aInsn.hasNext()) {
							AbstractInsnNode insNode = aInsn.next();
							if(insNode instanceof FieldInsnNode){
								FieldInsnNode fieldNode = ((FieldInsnNode) insNode);							
								if(fieldNode.getOpcode() == PUTFIELD && isNeighborService(cn,fieldNode.owner,fieldNode.name,fieldNode.desc)){																		
									String serviceName = fieldNode.desc;
									//get the real class information
									while(serviceName.startsWith("[")){
										serviceName = serviceName.substring(1);										
									}
									if(serviceName.startsWith("L")){
										serviceName = serviceName.substring(1);
									}									
									if(serviceName.endsWith(";")){
										serviceName = serviceName.substring(0, serviceName.length()-1);
									}	
									if(!allServices.contains(serviceName)){
										LOGGER.info("All possible Services to be used:"+serviceName);
										allServices.add(serviceName);
									}
								}								
							}
						}
					}
				}
			}
		}
		return allServices;
	}
	/**
	 * Whether the field is a neighbor service
	 * @param cn
	 * @param fieldName
	 * @param fieldDesc
	 * @param owner
	 * @return
	 */
	public boolean isNeighborService(ClassNode cn, String owner, String fieldName,
			String fieldDesc) {
		for (FieldNode fn : (List<FieldNode>) cn.fields) {
			if (fieldName.equals(fn.name) && fieldDesc.equals(fn.desc)
					&& owner.equals(cn.name)) {
				return true;
			}
		}
		return false;
	}
	
	public void transform(){
		LOGGER.info("Begin analyzing the method: " + cn.name);
		for (MethodNode mn : (List<MethodNode>) cn.methods) {
			if (isToAnalyze(mn) &&(!(nestStateCon.containsKey(mn.name + mn.desc)))) {

				LOGGER.info("Begin analyzing the method: " + mn.name);

				CFGDeriver cfgderiver = new CFGDeriver(cn.name, mn);
				ControlFlow cfg = cfgderiver.getCFG();			
				
				DDADeriver ddaDeriver = new DDADeriver(cn, mn, cfg, allServices);
				Set<String> startFuture = ddaDeriver.constructDDAwithFuture();
				nestStateCon.put(mn.name + mn.desc, startFuture);
				DDA dda = ddaDeriver.getDDA();
				String states = dda.statesToString();
				List<TransactionEvent> tranEves = dda.getTransactionEvent();
				
				if(startFuture.size() == 0){
					Instrumenter instrumenter = new Instrumenter(cn, mn, ddmFullyQuaName, choice);
					instrumenter.instrumentNoEvents(tranEves);
				}
				else{				
							
				Instrumenter instrumenter = new Instrumenter(cn, mn, states, ddmFullyQuaName, choice);
				int localNumTranId = instrumenter.instrumentTransactionID(choice);
				instrumenter.instrumentDDM(localNumTranId);
				for(TransactionEvent teve : tranEves){
					instrumenter.InstrumentTransactionEvent(teve);
				}}
		}}
	}
	public boolean isToAnalyze(MethodNode mn){
		if(isTransaction(mn, TxAnnotationDesc)){
			return true;
		}
		else{
			if(methodToAnalyze == null){
				return false;
			}
			else{
				if(methodToAnalyze.contains(mn.name)){
					return true;
				}
				else{
					return false;
				}
			}
		}		
	}

	

}

			

