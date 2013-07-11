/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.simplepv.pvmanager;

import static org.csstudio.utility.test.HamcrestMatchers.greaterThanOrEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.csstudio.simplepv.AbstractPVFactory;
import org.csstudio.simplepv.IPV;
import org.csstudio.simplepv.IPVListener;
import org.epics.pvmanager.CompositeDataSource;
import org.epics.pvmanager.PVManager;
import org.epics.pvmanager.sim.SimulationDataSource;
import org.junit.BeforeClass;
import org.junit.Test;

/** Plain JUnit test for reading with PVManagerPVFactory
 *  @author Kay Kasemir
 */
public class PVManagerReadUnitTest implements IPVListener
{
    final private AtomicInteger connections = new AtomicInteger();
    final private AtomicInteger values = new AtomicInteger();
    private volatile Exception error = null;
    
    @BeforeClass
    public static void setup()
    {
        CompositeDataSource sources = new CompositeDataSource();
        sources.putDataSource("sim", new SimulationDataSource());
        sources.setDefaultDataSource("sim");
        PVManager.setDefaultDataSource(sources);
    }
    
    @Override
    public void connectionChanged(final IPV pv)
    {
        System.out.println(pv.getName() + (pv.isConnected() ? " connected" : " disconnected"));
        connections.incrementAndGet();
    }

    @Override
    public void exceptionOccurred(final IPV pv, final Exception exception)
    {
        error = exception;
        error.printStackTrace();
    }

    @Override
    public void valueChanged(final IPV pv)
    {
        System.out.println(pv.getName() + " = " + pv.getValue());
        values.incrementAndGet();
    }

    @Override
    public void writeFinished(final IPV pv, final boolean isWriteSucceeded)
    {
        error = new Exception("Received write 'finish'");
    }

    @Override
    public void writePermissionChanged(final IPV pv)
    {
        error = new Exception("Received write permission change");
    }

    @Test
    public void testPV() throws Exception
    {
        final ExecutorService THREAD = Executors.newSingleThreadExecutor();
        
        final AbstractPVFactory factory = new PVManagerPVFactory();
        final IPV pv = factory.createPV("sim://ramp", true, 10, false, THREAD, null);
        pv.addListener(this);
        pv.start();
        // Expect about 1 update per second
        for (int count=0;  count < 10;  ++count)
        {
            if (values.get() > 5)
                break;
            else
                TimeUnit.SECONDS.sleep(1);
        }
        assertThat(pv.isConnected(), equalTo(true));
        assertThat(values.get(), greaterThanOrEqualTo(5));
        pv.stop();

        // Wait for disconnect
        for (int count=0;  count < 10;  ++count)
        {
            if (pv.isConnected())
                TimeUnit.SECONDS.sleep(1);
            else
                break;
        }
        assertThat(pv.isConnected(), equalTo(false));

        // Should not see error from sim:// channel
        assertThat(error, is(nullValue()));
        
        THREAD.awaitTermination(1, TimeUnit.SECONDS);
    }
}
