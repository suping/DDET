package org.artemisproject.ddet.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
/**
 * 
 * 
 * @author Ping Su<njupsu@gmail.com>
 */
public class ControlFlow {
	private final static Logger LOGGER = Logger.getLogger(ControlFlow.class.getName());
	
	public static Logger getLogger() {
		return LOGGER;
	}

	private List<NodeAndOutarcs> con = new LinkedList<NodeAndOutarcs>();
	public ControlFlow(){
		
	}
	public ControlFlow(List<NodeAndOutarcs> cons){
		con = cons;
	}
	public List<NodeAndOutarcs> getFlow(){
		return con;
	}

	public NodeAndOutarcs getFlow(int src) {
		Iterator<NodeAndOutarcs> f = con.iterator();
		while (f.hasNext()) {
			NodeAndOutarcs fn = f.next();
			if (fn.getSrc()== src) {
				return fn;
			}
		}
		return null;
	}

	public void addFlow(int src, int dst) {
		NodeAndOutarcs fl = getFlow(src);
		if (fl != null) {
			Set<Integer> flDst = fl.getDst();
			for(int d : flDst){
				if(d == dst){
//					LOGGER.warning("The flow "+src+"->"+dst+"exists!");
					return;
				}
			}
			fl.setDst(dst);
		} else {			
			con.add(new NodeAndOutarcs(src, dst));
		}
		LOGGER.fine(src+"->"+dst);
	}

	public int getDstSize(int src) {
		return getFlow(src).getDst().size();
	}

	public Set<Integer> getDst(int src) {
		return getFlow(src).getDst();
	}
	public int getOneDst(int src){
		for(int d : getDst(src)){
			return d;
		}
		//it has no dest node
		return -1;
	}

	public Set<AbstractInsnNode> getDstNode(MethodNode mn, int src) {
		Set<AbstractInsnNode> dstnode = new ConcurrentSkipListSet<AbstractInsnNode>();
		Set<Integer> d = getDst(src);
		for (int temp : d) {
			dstnode.add((AbstractInsnNode) mn.instructions
					.get(temp));
		}
		return dstnode;

	}

	public void showControlFlow() {
		Iterator<NodeAndOutarcs> f = con.iterator();
		while (f.hasNext()) {
			NodeAndOutarcs fn = f.next();
			System.out.print(fn.getSrc() + "->");
			Set<Integer> d = getDst(fn.getSrc());
			for (int temp : d) {
				System.out.print(temp + ",");
			}
			System.out.println();
		}

	}	

}

class NodeAndOutarcs {
	private int src;	
	private Set<Integer> dst = new ConcurrentSkipListSet<Integer>();

	public NodeAndOutarcs(int src, int dst) {
		this.src = src;
		this.dst.add(dst);		
	}

	public void setSrc(int s) {
		src = s;
	}

	public void setDst(int d) {
		dst.add(d);
	}

	public int getSrc() {
		return src;
	}

	public Set<Integer> getDst() {
		return dst;
	}

}