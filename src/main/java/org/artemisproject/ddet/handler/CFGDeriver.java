package org.artemisproject.ddet.handler;


import org.artemisproject.ddet.model.ControlFlow;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;


public class CFGDeriver {
	
	private MethodNode mn;
	private String owner;
	private ControlFlow cfg = new ControlFlow();	
	
	public CFGDeriver(String owner, MethodNode mn){
		this.owner = owner;
		this.mn = mn;
		try {
			ExtractControlFlow();
		} catch (AnalyzerException ignored) {
		}
	}	
	
	
	public ControlFlow getCFG(){
		return cfg;
	}
	
	
	/**
	 * Extract CFG of the method
	 * 
	 * @param owner
	 * @param mn
	 * @throws AnalyzerException
	 */
	public void ExtractControlFlow() throws AnalyzerException {

		Analyzer a = new Analyzer(new BasicInterpreter()) {

			@Override
			protected void newControlFlowEdge(int src, int dst) {
				cfg.addFlow(src, dst);
			}
		};
		a.analyze(owner, mn);
	}
	
}
