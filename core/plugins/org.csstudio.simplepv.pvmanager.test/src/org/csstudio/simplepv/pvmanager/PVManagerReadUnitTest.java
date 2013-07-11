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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.csstudio.simplepv.AbstractPVFactory;
import org.csstudio.simplepv.IPV;
import org.csstudio.simplepv.IPVListener;
import org.epics.pvmanager.CompositeDataSource;
import org.epics.pvmanager.PVManager;
import org.epics.pvmanager.sim.SimulationDataSource;
import org.epics.vtype.VType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Plain JUnit test for reading with PVManagerPVFactory
 *  @author Kay Kasemir
 */
public class PVManagerReadUnitTest
{
    final private ExecutorService THREAD = Executors.newSingleThreadExecutor();
    final private AbstractPVFactory factory = new PVManagerPVFactory();
        
    final private AtomicInteger connections = new AtomicInteger();
    final private AtomicInteger changes = new AtomicInteger();
    private volatile Exception error = null;
    
    @Before
    public void setup()
    {
        final CompositeDataSource sources = new CompositeDataSource();
        sources.putDataSource("sim", new SimulationDataSource());
        sources.setDefaultDataSource("sim");
        PVManager.setDefaultDataSource(sources);
    }

    @After
    public void shutdown() throws Exception
    {
        THREAD.shutdown();
        THREAD.awaitTermination(1, TimeUnit.SECONDS);
    }
    
    @Test
    public void testBasicReading() throws Exception
    {
        final boolean readonly = true;
        final boolean buffer = false;
        final IPV pv = factory.createPV("sim://ramp", readonly, 10, buffer, THREAD, null);
        pv.addListener(new IPVListener()
        {
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
                final VType value = pv.getValue();
                System.out.println(pv.getName() + " = " + value);
                if (value != null)
                    changes.incrementAndGet();
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
        });
        
        assertThat(pv.isStarted(), equalTo(false));
        pv.start();
        assertThat(pv.isStarted(), equalTo(true));
        // Expect about 1 update per second
        for (int count=0;  count < 10;  ++count)
        {
            if (changes.get() > 5)
                break;
            else
                TimeUnit.SECONDS.sleep(1);
        }
        assertThat(pv.isConnected(), equalTo(true));
        assertThat(changes.get(), greaterThanOrEqualTo(5));
        pv.stop();
        assertThat(pv.isStarted(), equalTo(false));

        // Wait for disconnect
        for (int count=0;  count < 10;  ++count)
        {
            if (pv.isConnected())
                TimeUnit.MILLISECONDS.sleep(10);
            else
            {
                System.out.println("Disconnect takes " + count*10 + " ms");
                break;
            }
        }
        assertThat(pv.isConnected(), equalTo(false));

        // Should not see error from sim:// channel
        assertThat(error, is(nullValue()));
    }

    @Test
    public void testBufferedReading() throws Exception
    {
        final boolean readonly = true;
        final boolean buffer = true;
        final IPV pv = factory.createPV("sim://ramp", readonly, (int)TimeUnit.SECONDS.toMillis(2), buffer, THREAD, null);
        
        final AtomicBoolean got_multiples = new AtomicBoolean();
        
        final List<VType> values = new ArrayList<>();
        pv.addListener(new IPVListener()
        {
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
                final List<VType> new_values = pv.getAllBufferedValues();
                System.out.println(pv.getName() + " = " + new_values);
                if (new_values != null)
                {
                    if (new_values.size() > 1)
                        got_multiples.set(true);
                    synchronized (new_values)
                    {
                        values.addAll(new_values);
                    }
                }
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
        });

        
        pv.start();
        assertThat(pv.isStarted(), equalTo(true));

        // Expect about 1 update per second, so wait for ~5 values
        TimeUnit.SECONDS.sleep(5);
        
        assertThat(pv.isConnected(), equalTo(true));

        // Should see a bunch of values...
        synchronized (values)
        {
            System.out.println(values);
            assertThat(values.size(), greaterThanOrEqualTo(1));
        }
        // ..AND they should have arrived with at least some multiples
        // (PV updates at 1Hz, we use a 2 sec update period)
        assertThat(got_multiples.get(), equalTo(true));

        pv.stop();

        // Should not see error from sim:// channel
        assertThat(error, is(nullValue()));
    }
}
