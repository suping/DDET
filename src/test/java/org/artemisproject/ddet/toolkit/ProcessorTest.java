package org.artemisproject.ddet.toolkit;


import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2_X2;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.IALOAD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LDC;
import static org.objectweb.asm.Opcodes.LSUB;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SALOAD;


import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.artemisproject.ddet.toolkit.Processor;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;


public class ProcessorTest {
	
	Set<String> methodNeedAnalyzed = new ConcurrentSkipListSet<String>();
	public void initialNextStateConforTrader(){
		Hashtable<String, Set<String>> nestStateCon = new Hashtable<String, Set<String>>();
		Set<String> temp1 = new ConcurrentSkipListSet<String>();
		nestStateCon.put("logout(Ljava/lang/String;)V", temp1);
		Set<String> temp2 = new ConcurrentSkipListSet<String>();
		temp2.add("AccountDataBean");
		nestStateCon.put("login(Ljava/lang/String;Ljava/lang/String;)Lorg/apache/geronimo/samples/daytrader/AccountDataBean;", temp2);
		nestStateCon.put("getAccountData(Ljava/lang/String;)Lorg/apache/geronimo/samples/daytrader/AccountDataBean;",temp2);
		nestStateCon.put("register(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/math/BigDecimal;)Lorg/apache/geronimo/samples/daytrader/AccountDataBean;",temp2);
		Set<String> temp3 = new ConcurrentSkipListSet<String>();
		temp3.add("HoldingDataBean");
		nestStateCon.put("getHoldings(Ljava/lang/String;)Ljava/util/Collection;",temp3);
		
		Set<String> temp4 = new ConcurrentSkipListSet<String>();
		temp4.add("QuoteDataBean");
		nestStateCon.put("getQuote(Ljava/lang/String;)Lorg/apache/geronimo/samples/daytrader/QuoteDataBean;",temp4);
		
		Set<String> temp5 = new ConcurrentSkipListSet<String>();	
		temp5.add("AccountProfileDataBean");
		nestStateCon.put("updateAccountProfile(Lorg/apache/geronimo/samples/daytrader/AccountProfileDataBean;)Lorg/apache/geronimo/samples/daytrader/AccountProfileDataBean;",temp5);
		
		Set<String> temp6 = new ConcurrentSkipListSet<String>();
		temp6.add("QuoteDataBean");
		temp6.add("HoldingDataBean");
		temp6.add("AccountDataBean");
		temp6.add("OrderDataBean");
		temp6.add("AccountProfileDataBean");
		nestStateCon.put("buy(Ljava/lang/String;Ljava/lang/String;DI)Lorg/apache/geronimo/samples/daytrader/OrderDataBean;",temp6);
		nestStateCon.put("sell(Ljava/lang/String;Ljava/lang/Integer;I)Lorg/apache/geronimo/samples/daytrader/OrderDataBean;",temp6);
	}
	public Set<String> setAllService(){
		Set<String> allServices = new ConcurrentSkipListSet<String>();
		allServices.add("org/apache/geronimo/samples/daytrader/AccountDataBean");
		allServices.add("org/apache/geronimo/samples/daytrader/AccountProfileDataBean");
		allServices.add("org/apache/geronimo/samples/daytrader/HoldingDataBean");
		allServices.add("org/apache/geronimo/samples/daytrader/RunStatsDataBean");
		allServices.add("org/apache/geronimo/samples/daytrader/OrderDataBean");
		allServices.add("org/apache/geronimo/samples/daytrader/QuoteDataBean");
		allServices.add("org/apache/geronimo/samples/daytrader/MarketSummaryDataBean");
		return allServices;
		/////for paper test
//		allServices.add("Account");
//		allServices.add("AccountProfile");
//		allServices.add("Holdings");		
//		allServices.add("Order");
//		allServices.add("Quote");	
		////for ejb
//		allServices.add("A");
//		allServices.add("B");		
	}
	public Set<String> setMethodNeedAnalyzed(){
		Set<String> method = new ConcurrentSkipListSet<String>();
		method.add("doBuy");
		return method;
	}
	public Set<String> setDirectMethodNeedAnalyzed(){
		Set<String> method = new ConcurrentSkipListSet<String>();
		method.add("buy");
		return method;
	}
	private void setTest(){
//		methodNeedAnalyzed.add("doHome");
		methodNeedAnalyzed.add("buy");
	}
	private void setEjb(){
		methodNeedAnalyzed.add("Test");
	}
	private void SetMethodAnalyzedDacapoTrader() {
		methodNeedAnalyzed.add("doHome");
		methodNeedAnalyzed.add("doLogin");
		methodNeedAnalyzed.add("doPortfolio");
		methodNeedAnalyzed.add("doQuote");
		methodNeedAnalyzed.add("doBuy");
		methodNeedAnalyzed.add("doUpdate");
		methodNeedAnalyzed.add("doLogout");
		methodNeedAnalyzed.add("doRegister");
		methodNeedAnalyzed.add("doSell");
	}
	private void SetMethodAnalyzedDirect() {
		//dacapoTrader invoke the real implementation to complete the function
		methodNeedAnalyzed.add("buy");
		methodNeedAnalyzed.add("sell");
		methodNeedAnalyzed.add("login");
		methodNeedAnalyzed.add("logout");
		methodNeedAnalyzed.add("register");
		methodNeedAnalyzed.add("getAccountData");
		methodNeedAnalyzed.add("getHoldings");
		methodNeedAnalyzed.add("getQuote");
		methodNeedAnalyzed.add("updateAccountProfile");	
		
//		methodNeedAnalyzed.add("creditAccountBalance");
//		methodNeedAnalyzed.add("updateAccountProfile");
//		methodNeedAnalyzed.add("updateQuoteVolume");
//		methodNeedAnalyzed.add("updateQuotePriceVolumeInt");
//		methodNeedAnalyzed.add("publishQuotePriceChange");
//		methodNeedAnalyzed.add("getAccountDataFromResultSet");
//		methodNeedAnalyzed.add("getAccountProfileDataFromResultSet");
//		methodNeedAnalyzed.add("getHoldingDataFromResultSet");
//		methodNeedAnalyzed.add("getQuoteDataFromResultSet");
//		methodNeedAnalyzed.add("getOrderDataFromResultSet");
//		methodNeedAnalyzed.add("resetTrade");

	}
	private void SetMethodAnalyzedTraderSLBean() {
		methodNeedAnalyzed.add("buy");
		methodNeedAnalyzed.add("sell");
		methodNeedAnalyzed.add("getMarketSummary");
		methodNeedAnalyzed.add("completeOrder");
		methodNeedAnalyzed.add("getOrders");
		methodNeedAnalyzed.add("getClosedOrders");
		methodNeedAnalyzed.add("updateQuotePriceVolume");
		methodNeedAnalyzed.add("getHoldings");
		methodNeedAnalyzed.add("getAccountData");
		methodNeedAnalyzed.add("updateAccountProfile");
		methodNeedAnalyzed.add("login");
		methodNeedAnalyzed.add("logout");
		methodNeedAnalyzed.add("register");
		
	}
	public void addTimeCoup(ClassNode cn, String methname){
		for (MethodNode mn : (List<MethodNode>) cn.methods) {
			if (mn.name.equals(methname)) {
				InsnList insns = mn.instructions;
				int localNum = mn.localVariables.size();
				InsnList time = new InsnList();				
				time.add(new VarInsnNode(ALOAD, 0));
				time.add(new MethodInsnNode(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J"));
//				time.add(new VarInsnNode(ALOAD, 0));
				time.add(new FieldInsnNode(PUTFIELD, cn.name, "time", "J"));				
				insns.insertBefore(insns.getFirst(), time);
				Iterator<AbstractInsnNode> i = insns.iterator();
				while (i.hasNext()) {
					AbstractInsnNode i1 = i.next();
					if (i1.getOpcode() >= IRETURN && i1.getOpcode() <= RETURN) {
						AbstractInsnNode ilPre = i1.getPrevious();
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
						
						InsnList timeEnd = new InsnList();
						timeEnd.add(new VarInsnNode(ALOAD, 0));
						timeEnd.add(new MethodInsnNode(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J"));
						timeEnd.add(new VarInsnNode(ALOAD, 0));
						timeEnd.add(new FieldInsnNode(GETFIELD, cn.name, "time", "J"));
						timeEnd.add(new InsnNode(LSUB));
						timeEnd.add(new FieldInsnNode(PUTFIELD, cn.name, "time", "J"));
						timeEnd.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
						timeEnd.add(new TypeInsnNode(NEW, "java/lang/StringBuilder"));
						timeEnd.add(new InsnNode(DUP));
						timeEnd.add(new LdcInsnNode("The runTime is "));
						timeEnd.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V"));
						timeEnd.add(new VarInsnNode(ALOAD, 0));
						timeEnd.add(new FieldInsnNode(GETFIELD, cn.name, "time", "J"));
						timeEnd.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;"));
						timeEnd.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;"));
						timeEnd.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V"));						
						insns.insert(ilPre, timeEnd);
//						timeEnd.add(new MethodInsnNode(INVOKESTATIC,
//								"java/lang/System", "currentTimeMillis", "()J"));
//						timeEnd.add(new VarInsnNode(LLOAD, localNum));
//						timeEnd.add(new InsnNode(LSUB));
//						timeEnd.add(new VarInsnNode(LSTORE, localNum));
//						timeEnd.add(new FieldInsnNode(GETSTATIC,
//								"java/lang/System", "out",
//								"Ljava/io/PrintStream;"));
//						timeEnd.add(new TypeInsnNode(NEW,
//								"java/lang/StringBuilder"));
//						timeEnd.add(new InsnNode(DUP));
//						timeEnd.add(new LdcInsnNode("RunTime is "));
//						timeEnd.add(new MethodInsnNode(INVOKESPECIAL,
//								"java/lang/StringBuilder", "<init>",
//								"(Ljava/lang/String;)V"));
//						timeEnd.add(new VarInsnNode(LLOAD, localNum));
//						timeEnd.add(new MethodInsnNode(INVOKEVIRTUAL,
//								"java/lang/StringBuilder", "append",
//								"(J)Ljava/lang/StringBuilder;"));
//						timeEnd.add(new MethodInsnNode(INVOKEVIRTUAL,
//								"java/lang/StringBuilder", "toString",
//								"()Ljava/lang/String;"));
//						timeEnd.add(new MethodInsnNode(INVOKEVIRTUAL,
//								"java/io/PrintStream", "println",
//								"(Ljava/lang/String;)V"));
//						insns.insert(ilPre, timeEnd);
//
//						LabelNode last = ((LocalVariableNode) mn.localVariables.get(0)).end;
//						LabelNode start = ((LocalVariableNode) mn.localVariables.get(0)).start;
//						mn.localVariables.add(new LocalVariableNode("time",
//								"J", null, start, last, localNum));
//						mn.maxLocals = mn.maxLocals + 1;
						return;
					}
				}
			}
		}
		
	}
	
	/**
	 * insert field TxLifecycleManager and the method setTxLifecycleManager for
	 * generating transaction id; public void
	 * setTxLifecycleManager(TxLifecycleManager tlm){ txLifecycleMgr = tlm; }
	 * 
	 * @param cn
	 */
/*	public boolean addTxLifecycleManager(ClassNode cn, String FieldName) {
		for (FieldNode fn : (List<FieldNode>) cn.fields) {
			if (fieldName.equals(fn.name)) {
				LOGGER.warning("There has already a _txLifecycleMgr in the "
						+ cn.name);
				return false;
			}
		}
		cn.fields.add(new FieldNode(ACC_PRIVATE, fieldName, fieldDesc, null,
				null));
//		LOGGER.fine("Add field _txLifecycleMgr success!");
		return true;
	}
	public void  Exptransform(String inputN,String meth){
		try {

			FileInputStream input = new FileInputStream(inputN);
			ClassReader cr = new ClassReader(input);
			ClassNode cn = new ClassNode(ASM4);
			cr.accept(cn, 0);
//			this.addTime(cn);
			transform(cn);
//			this.addTimeCoup(cn, "run");		
			ClassWriter cw = new TranClassWriter(ClassWriter.COMPUTE_FRAMES);
			cn.accept(cw);
			byte[] b = cw.toByteArray();
			String out = "E:\\classchaged\\DaCapoTrader.class";
			FileOutputStream fout = new FileOutputStream(new File(meth));
			fout.write(b);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();

		}}

	public boolean addTime(ClassNode cn) {
		
		cn.fields.add(new FieldNode(0, "time", "J", null,
				null));
//		LOGGER.fine("Add field time success!");
		return true;
	}
	public static void main(String args[]){
		try {
			String inputN="E:\\needChange\\TradeDirect.class";
//			String out = "E:\\classchaged\\TradeDirect.class";
//			String inputN="E:\\needChange\\SwitchHasTimeNanoTime\\DaCapoTrader.class";			
//			String out = "E:\\classchaged\\DaCapoTrader.class";
//			String out = "E:\\classchaged\\overhead\\DaCapoTrader.class";
//			String inputN="E:\\needChange\\SwitchHasClassNanoTime\\DaCapoTrader.class";
//			String out = "E:\\classchaged\\overhead\\TradeDirect.class";
////			String out = "E:\\new\\DayTrader.class";
//			String out = "E:\\new\\TradeDirect.class";
//			String inputN="E:\\needChange\\repeat\\DaCapoTrader.class";
////			String inputN="E:\\DayTrader.class";
//			String inputN = "C:\\Users\\SuPing\\workspace\\asmtest\\bin\\InstrumentTest.class";
//			String out = "C:\\Users\\SuPing\\workspace\\asmtest\\bin\\InstrumentTest.class";
			FileInputStream input = new FileInputStream(inputN);
			ClassReader cr = new ClassReader(input);
			ClassNode cn = new ClassNode(ASM4);
			cr.accept(cn, 0);
			ExpProAna.getInstance(cn).transform(cn);
//			ClassWriter cw = new TranClassWriter(ClassWriter.COMPUTE_FRAMES);
//			cn.accept(cw);
//			byte[] b = cw.toByteArray();
//			
//			FileOutputStream fout = new FileOutputStream(new File(out));
//			fout.write(b);
//			fout.close();
		} catch (Exception e) {
			e.printStackTrace();

		}
//		ExpProAna.getInstance(cn)			
//		String filename1="E:\\needChange\\TradeDirect.class";
//		String filedst = "E:\\classchaged\\test\\TradeDirect.class";
//		String filename1="E:\\needChange\\buysell\\TradeSLSBBean.class";
//		String filedst = "E:\\classchaged\\TradeSLSBBean.class";
//		String filename1="E:\\needChange\\SwitchHasTimeNanoTime\\DaCapoTrader.class";
//		String filedst = "E:\\classchaged\\DaCapoTrader.class";
//		analyse.Exptransform(filename1, filedst);
//		showClassSource("E:\\classchaged\\DaCapoTrader.class");

//		String projectPath = "/home/nju/PortalServiceImpl.class";	
//		String projectPath = "/home/nju/localsvn/conup-sample-hello-auth/target/classes/cn/edu/nju/moon/conup/sample/auth/services/AuthServiceImpl.class";
//		String dbPath = "/home/nju/localsvn/conup-sample-db/target/classes/cn/edu/nju/moon/conup/sample/db/services/DBServiceImpl.class";
//		String authPath = "/home/nju/localsvn/conup-sample-auth/target/classes/cn/edu/nju/moon/conup/sample/auth/services/AuthServiceImpl.class";
//		String procPath = "/home/nju/localsvn/conup-sample-proc/target/classes/cn/edu/nju/moon/conup/sample/proc/services/ProcServiceImpl.class";
//		String portalPath = "/home/nju/localsvn/conup-sample-portal/target/classes/cn/edu/nju/moon/conup/sample/portal/services/PortalServiceImpl.class";
//		
//		analyse.analyzeApplication(dbPath,"");
//		analyse.analyzeApplication(authPath,"");
//		analyse.analyzeApplication(procPath,"");
//		analyse.analyzeApplication(portalPath,"");
//		String portalPath = "/home/PortalServiceImpl.class";			
//		analyse.analyzeApplication(portalPath,"");
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
//}
	
*/	
	public static void main(String args[]){
		ProcessorTest epat = new ProcessorTest();
//		File file = new File("E:\\ttp\\source\\TradeDirect.class");
		String sourcefile = "E:\\ttp\\source\\TradeDirect.class";
		
		String criteria = "tanquility";
		String outPath = "E:\\ttp";
//		String criteria = "quiescence";
		String ddm = "org/apache/geronimo/samples/daytrader/dacapo/LDDM";
		Set<String> neiComs = epat.setAllService();
//		Set<String> methods = epat.setMethodNeedAnalyzed();
//		Set<String> methods = epat.setDirectMethodNeedAnalyzed();
//		Processor pro = new Processor(criteria, "exp", methods, neiComs, ddm);
//		
//		pro.analyzeSource(sourcefile, outPath);
		
		
		String sourcefile1 = "E:\\ttp\\source\\ShoppingCartImpl.class";
		String reference = "Lorg/oasisopen/sca/annotation/Reference;";
		String conupTx = "Lcn/edu/nju/moon/conup/spi/datamodel/ConupTransaction;";
		Processor pro1 = new Processor(criteria, "conup", conupTx, reference, ddm);
		pro1.analyzeSource(sourcefile1, outPath);


	}
	
	


}

