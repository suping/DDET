package org.artemisproject.ddet.handler;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.artemisproject.ddet.event.StartEvent;
import org.artemisproject.ddet.handler.Instrumenter;
import org.artemisproject.ddet.model.State;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;


public class InstrumenterTest {

	@Before
	public void setUp() throws Exception {		
		
		
	}

	@Test
	public void TestStatesToString() {
		List<State> states = new LinkedList<State>();
		Set<String> future1 = new ConcurrentSkipListSet<String>();
		future1.add("a");
		future1.add("b");
		State state1 =new State(1,future1);
		states.add(state1);
		Set<String> future2 = new ConcurrentSkipListSet<String>();
		future2.add("e");
		future2.add("f");
		State state2 =new State(15,future2);
		states.add(state2);
		Instrumenter inter = new Instrumenter();
//		String stateString = inter.statesToString(inter.statesToList(states));
//		assertEquals(stateString,"a,b;e,f");		
		
	}
	public static void main(String args[]){
		try {
			String inputPath = "E:\\ttp\\source\\TradeDirect.class";
			String ddm = "org/apache/geronimo/samples/daytrader/dacapo/LDDM";
			FileInputStream input = new FileInputStream(inputPath);
			ClassReader cr = new ClassReader(input);
			ClassNode cn = new ClassNode();
			cr.accept(cn, 0);
			MethodNode buynode = InstrumenterTest.getMethodNode(cn, "buy");
			Instrumenter intr = new Instrumenter(cn,buynode,ddm,"conup");
			
			int i = intr.instrumentTransactionID("conup");
			intr.instrumentDDM(i);
			StartEvent se = new StartEvent();
			intr.InstrumentTransactionEvent(se);
//			StartEvent se = new StartEvent();
//			String fieldName = "_txLifecycleMgr";
//			String fieldDesc = "Lcn/edu/nju/moon/conup/spi/datamodel/TxLifecycleManager;";
//			intr.instrumentClassField(fieldName, fieldDesc);
			ClassWriter cw = new TranClassWriter(ClassWriter.COMPUTE_FRAMES);
			cn.accept(cw);
			byte[] b = cw.toByteArray();
			String out = "E:\\ttp\\TradeDirect.class";
			FileOutputStream fout = new FileOutputStream(new File(out));
			fout.write(b);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();

		}
	}
	public static MethodNode getMethodNode(ClassNode cn, String methodSig){
		for (MethodNode mn : (List<MethodNode>) cn.methods) {
			if(methodSig.equals(mn.name)){
				return mn;
			}
		}
		return null;
	}

}

class TranClassWriter extends ClassWriter {
	// private ClassLoader loader;
	public TranClassWriter(final int flags) {
		super(flags);
	}

	@Override
	protected String getCommonSuperClass(final String type1, final String type2) {
		Class<?> c, d;
		try {
			c = Class.forName(type1.replace('/', '.'));
			d = Class.forName(type2.replace('/', '.'));
		} catch (Exception e) {
			// System.out.println("!!!!!!Exception, Type: " + type1 + "," +
			// type2);
			// System.out.println("!!!!!!Exception, "+e.toString());
			return "java/lang/Object";
			// throw new RuntimeException(e.toString());
		}
		if (c.isAssignableFrom(d)) {
			return type1;
		}
		if (d.isAssignableFrom(c)) {
			return type2;
		}
		if (c.isInterface() || d.isInterface()) {
			return "java/lang/Object";
		} else {
			do {
				c = c.getSuperclass();
			} while (!c.isAssignableFrom(d));
			return c.getName().replace('.', '/');
		}
	}

}

