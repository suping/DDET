package org.artemisproject.ddet.runtime;

import static org.junit.Assert.*;
import org.junit.Test;


public class LDDMTest {

	@Test
	public void testDEF(){
//		for(int i=0;i<20;i++){
	
		String DDA = "AccountDataBean,QuoteDataBean,HoldingDataBean,OrderDataBean,AccountProfileDataBean;QuoteDataBean,AccountDataBean,HoldingDataBean,OrderDataBean,AccountProfileDataBean;QuoteDataBean,AccountDataBean,HoldingDataBean,OrderDataBean,AccountProfileDataBean;QuoteDataBean,OrderDataBean,AccountDataBean,AccountProfileDataBean,HoldingDataBean;OrderDataBean,AccountDataBean,AccountProfileDataBean,HoldingDataBean;AccountDataBean,OrderDataBean,AccountProfileDataBean,HoldingDataBean;OrderDataBean,AccountProfileDataBean,HoldingDataBean;OrderDataBean,AccountProfileDataBean,HoldingDataBean;OrderDataBean;OrderDataBean;_E";
		String txId = "1234";
		String cnMnInf = "TradeDirect:buy";		
		LDDM lddm = LDDM.getInstance(txId,DDA,cnMnInf);
		lddm.triggerStart();
		System.out.println("Future : "+lddm.getFuture()+" , Past : "+lddm.getPast());
		lddm.triggerBeforeCom("QuoteDataBean");
		lddm.triggerCom("QuoteDataBean", 4);
		System.out.println("Future : "+lddm.getFuture()+" , Past : "+lddm.getPast());
		lddm.triggerBeforeCom("OrderDataBean");
		lddm.triggerCom("OrderDataBean", 5);
		System.out.println("Future : "+lddm.getFuture()+" , Past : "+lddm.getPast());
		lddm.triggerCom("dd", 6);
		System.out.println("Future : "+lddm.getFuture()+" , Past : "+lddm.getPast());
		lddm.nextState(8);
		System.out.println("Future : "+lddm.getFuture()+" , Past : "+lddm.getPast());
		lddm.triggerCom("OrderDataBean", 9);
		System.out.println("Future : "+lddm.getFuture()+" , Past : "+lddm.getPast());		
		lddm.triggerEnd();			
		
	}

}
