package org.artemisproject.transformer;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class Transformer {
	
	ClassNode cn;
	
	
	/**
	 * decide the way to generate transaction id,  how to instrument invalid transactions with on invocations
	 * "conup",  txlifeManager.createID, instrument StartEvents and EndEvents
	 */
	String choice;
	public void setChoice(String choice){
		this.choice = choice;
	}
	
	String ddmFullyQuaName;
	public void setDdmFullyQuaName(String dfq){
		ddmFullyQuaName = dfq;
	}
	
	String TxAnnotationDesc ="";	
	
	Set<String> methodToAnalyze = null;
	
	public Transformer(ClassNode cn, String choice, String ddmFullyQuaName){
		this.choice = choice;
		this.ddmFullyQuaName = ddmFullyQuaName;		
		this.cn = cn;
	}
	
	public Transformer(ClassNode cn, String choice, String ddmFullyQuaName, String TxAnnotationDesc){
		this.choice = choice;
		this.ddmFullyQuaName = ddmFullyQuaName;
		this.TxAnnotationDesc = TxAnnotationDesc;
		this.cn = cn;
	}
	public Transformer(ClassNode cn, String choice, String ddmFullyQuaName, Set<String> methodToAnalyze){
		this.choice = choice;
		this.ddmFullyQuaName = ddmFullyQuaName;
		this.methodToAnalyze = methodToAnalyze;
		this.cn = cn;
	}
	
	public void setTxAnnotationDesc(String tad){
		TxAnnotationDesc = tad;
	}
	public void setMethodToAnalyze(Set<String> mta){
		methodToAnalyze = mta;
	}
	/**
	 * whether user sepcifies the methods to be analyzed
	 * @return
	 */
	public boolean getMethodstoAnalyzed(){
		return methodToAnalyze.isEmpty();
	}
	
	/**
	 * whether a class has a transaction (according to its annotation)
	 * @param cn
	 * @param conupTx
	 *            default
	 * @return
	 */	
	public boolean whetherToAnalyze() {
		for (MethodNode mn : (List<MethodNode>) cn.methods) {
			if (mn.visibleAnnotations != null) {
				Iterator<AnnotationNode> i = mn.visibleAnnotations.iterator();
				while (i.hasNext()) {
					AnnotationNode an = i.next();
					if (TxAnnotationDesc.equals(an.desc)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Whether the method has the specified annotation
	 * 
	 * @param mn
	 * @param annotationDesc
	 * @return
	 */
	public boolean isTransaction(MethodNode mn, String annotationDesc) {
		if (!annotationDesc.isEmpty()) {
			if (mn.visibleAnnotations != null) {
				Iterator<AnnotationNode> i = mn.visibleAnnotations.iterator();
				while (i.hasNext()) {
					AnnotationNode an = i.next();
					if (annotationDesc.equals(an.desc)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public MethodNode getMethodNode(String methodSig){
		for (MethodNode mn : (List<MethodNode>) cn.methods) {
			if(methodSig.equals(mn.name +mn.desc)){
				return mn;
			}
		}
		return null;
	}
	
	public abstract void transform();


}
