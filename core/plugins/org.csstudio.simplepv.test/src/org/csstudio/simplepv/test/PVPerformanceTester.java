/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.csstudio.simplepv.test;

import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.csstudio.simplepv.IPV;
import org.csstudio.simplepv.IPVListener;
import org.csstudio.simplepv.SimplePVLayer;

/**
 * Test SimplePV performance by creating and closing thousands of PVs rapidly.
 * 
 * @author Xihui Chen
 * 
 */
public class PVPerformanceTester {

	private int totalPVs = 1000;
	private String pvFactoryId;
	private AtomicInteger updates;
	private PVNameProvider pvNameProvider;

	/**
	 * Create a tester.
	 * 
	 * @param pvFactoryId
	 *            pv factory id.
	 * @param totalPVs 
	 * 			  Total number of PVs to be tested.
	 * @throws Exception
	 */
	public PVPerformanceTester(String pvFactoryId, int totalPVs, PVNameProvider pvNameProvider) throws Exception {
		this.pvFactoryId = pvFactoryId;
		updates = new AtomicInteger(0);
		this.totalPVs = totalPVs;
		this.pvNameProvider = pvNameProvider;
	}

	public void testAll() throws Exception {
		testOpenClose();
	}

	protected void testOpenClose() throws Exception {
		IPV[] pvs = new IPV[totalPVs];
		final CountDownLatch latch = new CountDownLatch(totalPVs);
		long startTime = Calendar.getInstance().getTimeInMillis();
		for (int i = 1; i <= totalPVs; i++) {
			IPV pv = SimplePVLayer.getPVFactory(pvFactoryId).createPV(
					pvNameProvider.getPVName(i));
			pvs[i-1] = pv;
			pv.start();
			pv.addListener(new IPVListener.Stub(){
				@Override
				public void valueChanged(IPV pv) {
					updates.incrementAndGet();
				}
				
				@Override
				public void connectionChanged(IPV pv) {
					if(pv.isConnected()){
						latch.countDown();
					}
				}
			});			
		}
		long stopTime = Calendar.getInstance().getTimeInMillis();
		System.out.println("It took " + (stopTime - startTime) + " ms to create " + totalPVs + " pvs." );
		
		if(!latch.await(20, TimeUnit.SECONDS)){			
			Assert.fail(""+ latch.getCount() + " pvs cannot connect in 20 seconds.");
		}

		for(int i=0; i<totalPVs; i++){
			pvs[i].stop();
		}
			
	}
	
	public interface PVNameProvider{
		/**Get PVName based on the index.
		 * @param index
		 * @return the pv name.
		 */
		public String getPVName(int index);
	}

}
