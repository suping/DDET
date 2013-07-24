package org.artemisproject.ddet.handler;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.artemisproject.ddet.handler.DDADeriver;
import org.artemisproject.ddet.model.State;
import org.junit.Before;
import org.junit.Test;


public class DDADeriverTest {

	@Before
	public void setUp() throws Exception {		
		
		
	}

	@Test
	public void TestStatesToString() {
		Set<String> coms = new ConcurrentSkipListSet<String>();
		Set<String> future1 = new ConcurrentSkipListSet<String>();
		future1.add("a");
		future1.add("b");
		State state1 =new State(1,future1);
		
		Set<String> future2 = new ConcurrentSkipListSet<String>();
		future2.add("e");
		future2.add("f");
		State state2 =new State(15,future2);
		
		Set<String> future3 = new ConcurrentSkipListSet<String>();
		future3.add("e");
		future3.add("f");
		State state3 =new State(4,future3);
		
		Set<String> future4 = new ConcurrentSkipListSet<String>();
		future4.add("e");	
		State state4 =new State(4,future4);
		
		DDADeriver ddad = new DDADeriver(coms);
		assertFalse(ddad.equalStateFuture(state1, state2));	
		assertTrue(ddad.equalStateFuture(state2, state3));	
		assertFalse(ddad.equalStateFuture(state3, state4));	
	}

}
