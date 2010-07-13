/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.alarm.beast.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.csstudio.alarm.beast.SeverityLevel;
import org.csstudio.platform.data.ITimestamp;
import org.csstudio.platform.data.TimestampFactory;
import org.junit.Test;

/** JUnit plug-in test of AlarmLogic
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class AlarmLogicTest 
{
    private static final String OK = "OK";

    /** Implementation of alarm logic that remembers update/annunc. actions */
    class AlarmLogicDemo extends AlarmLogic
    {
        boolean fired_enablement = false;
        boolean fired_update = false;
        boolean annunciated = false;

        AlarmLogicDemo(final boolean latching, final boolean annunciating)
        {
            this(latching, annunciating, 0, 0);
        }

        AlarmLogicDemo(final boolean latching, final boolean annunciating,
                       final int delay)
        {
            this(latching, annunciating, delay, 0);
        }

        AlarmLogicDemo(final boolean latching, final boolean annunciating,
                       final int delay, final int count)
        {
            super(latching, annunciating, delay, count,
                  AlarmState.createClearState(),
                  AlarmState.createClearState());
        }

        @Override
        protected void fireEnablementUpdate()
        {
            fired_enablement = true;
        }

        @Override
        protected void fireStateUpdates()
        {
            fired_update = true;
        }
    
        @Override
        protected void fireAnnunciation(final SeverityLevel level)
        {
            annunciated = true;
        }

        /** @deprecated This one doesn't check the current message */
        @Deprecated
        void check(final boolean update, final boolean annunciate,
                final SeverityLevel current,
                final SeverityLevel sevr, final String msg)
        {
            System.out.println(
                (fired_update ? "new, " : "old, ") +
                (annunciated ? "annunciate : " : "silent     : ") +
                        toString());
            assertEquals("Update", update, fired_update);
            assertEquals("Annunciation", annunciate, annunciated);
            assertEquals("Current severity", current, getCurrentState().getSeverity());
            assertEquals("Alarm severity", sevr, getAlarmState().getSeverity());
            assertEquals("Alarm message", msg, getAlarmState().getMessage());
            // Reset
            fired_update = false;
            annunciated = false;
        }

        void check(final boolean update, final boolean annunciate,
                final SeverityLevel current_sevr, final String current_msg,
                final SeverityLevel sevr, final String msg)
        {
            System.out.println(
                (fired_update ? "new, " : "old, ") +
                (annunciated ? "annunciate : " : "silent     : ") +
                        toString());
            assertEquals("Update", update, fired_update);
            assertEquals("Annunciation", annunciate, annunciated);
            assertEquals("Current severity", current_sevr, getCurrentState().getSeverity());
            assertEquals("Current message", current_msg, getCurrentState().getMessage());
            assertEquals("Alarm severity", sevr, getAlarmState().getSeverity());
            assertEquals("Alarm message", msg, getAlarmState().getMessage());
            // Reset
            fired_update = false;
            annunciated = false;
        }
        
        void checkEnablementChange()
        {
            assertTrue("Enablement changed", fired_enablement);
            System.out.println("Logic is " + (isEnabled() ? "enabled" : "disabled"));
            fired_enablement = false;
        }

        public void computeNewState(final String value, final SeverityLevel sevr,
                final String msg)
        {
            computeNewState(new AlarmState(sevr, msg, value, TimestampFactory.now()));
        }
    }

    @Test
    public void testLatchedAnnunciatedAlarmAckOK()
    {
        System.out.println("* Latched, annunciated: Minor, Minor, Major, Major, Ack, Minor, OK");
        final AlarmLogicDemo logic = new AlarmLogicDemo(true, true);
        logic.check(false, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        assertEquals("", logic.getAlarmState().getValue());
        
        // Follow into MINOR alarm
        logic.computeNewState("a", SeverityLevel.MINOR, "high");
        logic.check(true, true, SeverityLevel.MINOR, "high", SeverityLevel.MINOR, "high");
        assertEquals("a", logic.getAlarmState().getValue());
        
        // No updates when state stays
        logic.computeNewState("b", SeverityLevel.MINOR, "high");
        logic.check(false, false, SeverityLevel.MINOR, "high", SeverityLevel.MINOR, "high");
        assertEquals("a", logic.getAlarmState().getValue());
        
        // Follow into MAJOR alarm
        logic.computeNewState("c", SeverityLevel.MAJOR, "very high");
        logic.check(true, true, SeverityLevel.MAJOR, "very high", SeverityLevel.MAJOR, "very high");
        assertEquals("c", logic.getAlarmState().getValue());
        
        // No updates when state stays
        logic.computeNewState("d", SeverityLevel.MAJOR, "ignored");
        logic.check(false, false, SeverityLevel.MAJOR, "ignored", SeverityLevel.MAJOR, "very high");
        assertEquals("c", logic.getAlarmState().getValue());

        // Ack'
        logic.acknowledge(true);
        logic.check(true, false, SeverityLevel.MAJOR, "ignored", SeverityLevel.MAJOR_ACK, "very high");
        assertEquals("c", logic.getAlarmState().getValue());

        // MINOR, but latch MAJOR alarm (not annunciated)
        logic.computeNewState("e", SeverityLevel.MINOR, "just high");
        logic.check(true, false, SeverityLevel.MINOR, "just high", SeverityLevel.MAJOR_ACK, "very high");
        assertEquals("c", logic.getAlarmState().getValue());

        // All back to OK
        logic.computeNewState("f", SeverityLevel.OK, OK);
        logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        assertEquals("", logic.getAlarmState().getValue());
    }

    @Test
    public void testLatchedAnnunciatedAlarmOKAck()
    {
        System.out.println("* Latched, annunciated: Minor, Major, Minor, OK, Ack");
        final AlarmLogicDemo logic = new AlarmLogicDemo(true, true);
        logic.check(false, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        
        // Follow into MINOR alarm
        logic.computeNewState("a", SeverityLevel.MINOR, "high");
        logic.check(true, true, SeverityLevel.MINOR, "high", SeverityLevel.MINOR, "high");
        
        // Follow into MAJOR alarm
        logic.computeNewState("b", SeverityLevel.MAJOR, "very high");
        logic.check(true, true, SeverityLevel.MAJOR, "very high", SeverityLevel.MAJOR, "very high");

        // MINOR, but latch MAJOR alarm (not annunciated)
        logic.computeNewState("c", SeverityLevel.MINOR, "just high");
        logic.check(true, false, SeverityLevel.MINOR, "just high", SeverityLevel.MAJOR, "very high");

        // OK, but latch MAJOR alarm (not annunciated)
        logic.computeNewState("d", SeverityLevel.OK, OK);
        logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.MAJOR, "very high");

        // Ack'
        logic.acknowledge(true);
        logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
    }

    @Test
    public void testLatchedAnnunciatedMajMinMajAckMinOK()
    {
        System.out.println("* Latched, annunciated: Major, Minor, Major, Ack, Minor, OK.");
        final AlarmLogicDemo logic = new AlarmLogicDemo(true, true);
        logic.check(false, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        
        // Follow into MAJOR alarm
        logic.computeNewState("a", SeverityLevel.MAJOR, "very high");
        logic.check(true, true, SeverityLevel.MAJOR, "very high", SeverityLevel.MAJOR, "very high");
    
        // MINOR, but latch MAJOR alarm (not annunciated)
        logic.computeNewState("b", SeverityLevel.MINOR, "just high");
        logic.check(true, false, SeverityLevel.MINOR, "just high", SeverityLevel.MAJOR, "very high");

        // Back into MAJOR alarm: Not annunciated
        logic.computeNewState("c", SeverityLevel.MAJOR, "very high");
        logic.check(true, false, SeverityLevel.MAJOR, "very high", SeverityLevel.MAJOR, "very high");

        // Ack'
        logic.acknowledge(true);
        logic.check(true, false, SeverityLevel.MAJOR, "very high", SeverityLevel.MAJOR_ACK, "very high");

        // MINOR
        logic.computeNewState("d", SeverityLevel.MINOR, "just high");
        logic.check(true, false, SeverityLevel.MINOR, "just high", SeverityLevel.MAJOR_ACK, "very high");
        
        // OK
        logic.computeNewState("e", SeverityLevel.OK, OK);
        logic.check(true, false, SeverityLevel.OK, SeverityLevel.OK, OK);
    }

    @Test
    public void testUnlatchedAnnunciatedMajMinMajAckMinOK()
    {
        System.out.println("* Unlatched, annunciated: Major, Minor, Major, Ack, Minor, OK.");
        final AlarmLogicDemo logic = new AlarmLogicDemo(false, true);
        logic.check(false, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        
        // Follow into MAJOR alarm
        logic.computeNewState("a", SeverityLevel.MAJOR, "very high");
        logic.check(true, true, SeverityLevel.MAJOR, "very high", SeverityLevel.MAJOR, "very high");
    
        // Follow into MINOR (not annunc)
        logic.computeNewState("b", SeverityLevel.MINOR, "high");
        logic.check(true, false, SeverityLevel.MINOR, "high", SeverityLevel.MINOR, "high");

        // Back into MAJOR alarm (annunc)
        logic.computeNewState("c", SeverityLevel.MAJOR, "very high");
        logic.check(true, true, SeverityLevel.MAJOR, "very high", SeverityLevel.MAJOR, "very high");

        // Ack'.
        logic.acknowledge(true);
        logic.check(true, false, SeverityLevel.MAJOR, "very high", SeverityLevel.MAJOR_ACK, "very high");

        // MINOR, but remember that MAJOR was ack'ed
        logic.computeNewState("d", SeverityLevel.MINOR, "just high");
        logic.check(true, false, SeverityLevel.MINOR, "just high", SeverityLevel.MAJOR_ACK, "very high");
        
        // OK
        logic.computeNewState("e", SeverityLevel.OK, OK);
        logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
    }

    @Test
    public void testUnlatchedAnnunciatedMajMinAckMajAckMinOK()
    {
        System.out.println("* Unlatched, annunciated: Major, Minor, Ack, Major, Ack, Minor, OK.");
        final AlarmLogicDemo logic = new AlarmLogicDemo(false, true);
        logic.check(false, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        assertEquals("", logic.getAlarmState().getValue());
        
        // Follow into MAJOR alarm
        logic.computeNewState("a", SeverityLevel.MAJOR, "very high");
        logic.check(true, true, SeverityLevel.MAJOR, "very high", SeverityLevel.MAJOR, "very high");
        assertEquals("a", logic.getAlarmState().getValue());
    
        // Follow into MINOR (not annunc)
        logic.computeNewState("b", SeverityLevel.MINOR, "high");
        logic.check(true, false, SeverityLevel.MINOR, "high", SeverityLevel.MINOR, "high");
        assertEquals("b", logic.getAlarmState().getValue());

        // Ack'.
        logic.acknowledge(true);
        logic.check(true, false, SeverityLevel.MINOR, "high", SeverityLevel.MINOR_ACK, "high");

        // Back into MAJOR alarm (annunc)
        logic.computeNewState("c", SeverityLevel.MAJOR, "very high");
        logic.check(true, true, SeverityLevel.MAJOR, "very high", SeverityLevel.MAJOR, "very high");

        // Ack'.
        logic.acknowledge(true);
        logic.check(true, false, SeverityLevel.MAJOR, "very high", SeverityLevel.MAJOR_ACK, "very high");

        // MINOR, but remember that MAJOR was ack'ed
        logic.computeNewState("d", SeverityLevel.MINOR, "just high");
        logic.check(true, false, SeverityLevel.MINOR, "just high", SeverityLevel.MAJOR_ACK, "very high");
        
        // OK
        logic.computeNewState("e", SeverityLevel.OK, OK);
        logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
    }

    @Test
    public void testDelayedButShort() throws Exception
    {
        System.out.println("* Latched, annunciated, delayed: Major, clear");
        final int delay = 2;
        final AlarmLogicDemo logic = new AlarmLogicDemo(true, true, delay);
        logic.check(false, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        assertEquals("", logic.getAlarmState().getValue());
        
        // MAJOR alarm has no immediate effect
        logic.computeNewState("a", SeverityLevel.MAJOR, "very high");      
        logic.check(true, false, SeverityLevel.MAJOR, "very high", SeverityLevel.OK, OK);
        assertEquals("", logic.getAlarmState().getValue());

        // .. if it clears in time (1/2 the delay time)
        Thread.sleep(delay * 500);
        logic.computeNewState("b", SeverityLevel.OK, OK);
        logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        assertEquals("", logic.getAlarmState().getValue());

        // Assert that it stays that way
        System.out.println("wait...");
        Thread.sleep(delay * 1500);
        logic.check(false, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        assertEquals("", logic.getAlarmState().getValue());
    }
    
    @Test
    public void testLatchedAnnunciatedDelayed() throws Exception
    {
        System.out.println("* Latched, annunciated, delayed: Major, persists, clear, ack; MINOR, MAJOR, MINOR, persist");
        final int delay = 2;
        final AlarmLogicDemo logic = new AlarmLogicDemo(true, true, delay);
        logic.check(false, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        assertEquals("", logic.getAlarmState().getValue());
        
        // MAJOR alarm has no immediate effect
        logic.computeNewState("a", SeverityLevel.MAJOR, "very high");      
        logic.check(true, false, SeverityLevel.MAJOR, "very high", SeverityLevel.OK, OK);
        assertEquals("", logic.getAlarmState().getValue());
        
        // ... until after some delay
        System.out.println("wait...");
        Thread.sleep(delay * 1500);
        logic.check(true, true, SeverityLevel.MAJOR, "very high", SeverityLevel.MAJOR, "very high");
        assertEquals("a", logic.getAlarmState().getValue());
        
        // Clear PV, but alarm still latched
        logic.computeNewState("b", SeverityLevel.OK, OK);
        logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.MAJOR, "very high");
        assertEquals("a", logic.getAlarmState().getValue());
        // Ack to clear alarm
        logic.acknowledge(true);
        logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        assertEquals("", logic.getAlarmState().getValue());

        // -----
        
        // MINOR alarm has no immediate effect
        logic.computeNewState("c", SeverityLevel.MINOR, "high");      
        Thread.sleep(500);
        logic.check(true, false, SeverityLevel.MINOR, "high", SeverityLevel.OK, OK);
        assertEquals("", logic.getAlarmState().getValue());

        // Neither has MAJOR
        final ITimestamp now = TimestampFactory.now();
        logic.computeNewState(new AlarmState(SeverityLevel.MAJOR, "too high", "d", now));      
        logic.check(true, false, SeverityLevel.MAJOR, "too high", SeverityLevel.OK, OK);
        Thread.sleep(delay * 100);
        assertEquals("", logic.getAlarmState().getValue());
        
        // Back to MINOR
        logic.computeNewState("e", SeverityLevel.MINOR, "high");      
        logic.check(true, false, SeverityLevel.MINOR, "high", SeverityLevel.OK, OK);
        assertEquals("", logic.getAlarmState().getValue());
        
        // ... until latched MAJOR (!) appears after some delay
        System.out.println("wait...");
        Thread.sleep(delay * 1500);
        logic.check(true, true, SeverityLevel.MINOR, "high", SeverityLevel.MAJOR, "too high");
        assertEquals("d", logic.getAlarmState().getValue());
        // Time should match the time of MAJOR event
        assertEquals(now, logic.getAlarmState().getTime());
     }
    
    @Test
    public void testUnlatchedAnnunciatedDelayed() throws Exception
    {
        System.out.println("* Unlatched, annunciated, delayed: Major, persists, clear, ack; MAJOR, MINOR, persist");
        final int delay = 2;
        final AlarmLogicDemo logic = new AlarmLogicDemo(false, true, delay);
        logic.check(false, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        
        // MAJOR alarm has no immediate effect
        logic.computeNewState("a", SeverityLevel.MAJOR, "very high");      
        logic.check(true, false, SeverityLevel.MAJOR, "very high", SeverityLevel.OK, OK);
        
        // ... until after some delay
        System.out.println("wait...");
        Thread.sleep(delay * 1500);
        logic.check(true, true, SeverityLevel.MAJOR, "very high", SeverityLevel.MAJOR, "very high");
        
        // Clearing the alarm
        logic.computeNewState("b", SeverityLevel.OK, OK);
        logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);

        // -----
        
        // MAJOR alarm has no immediate effect
        logic.computeNewState("c", SeverityLevel.MAJOR, "too high");      
        logic.check(true, false, SeverityLevel.MAJOR, "too high", SeverityLevel.OK, OK);

        // Back to MINOR
        logic.computeNewState("d", SeverityLevel.MINOR, "high");      
        logic.check(true, false, SeverityLevel.MINOR, "high", SeverityLevel.OK, OK);
        
        // ... until alarm persists, using the last alarm (MINOR) because not latched
        System.out.println("wait...");
        Thread.sleep(delay * 1500);
        logic.check(true, true, SeverityLevel.MINOR, "high", SeverityLevel.MINOR, "high");
     }
    
    @Test
    public void testLatchedAnnunciatedCount() throws Exception
    {
        System.out.println("* Latched, annunciated, count: minor, ok, minor, ok");
        final int delay = 200;
        int count = 3;
        final AlarmLogicDemo logic = new AlarmLogicDemo(true, true, delay, count);
        logic.check(false, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        
        // (count-1) brief MINOR alarms have no effect
        System.out.println((count-1) + " ignored alarms....");
        for (int i=0; i<count-1; ++i)
        {
            logic.computeNewState("a", SeverityLevel.MINOR, "high");      
            logic.check(true, false, SeverityLevel.MINOR, "high", SeverityLevel.OK, OK);
            logic.computeNewState("b", SeverityLevel.OK, OK);
            logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        }

        // But when they reach the count, it matters
        System.out.println("Final alarm to get count of " + count);
        logic.computeNewState("c", SeverityLevel.MINOR, "high");      
        logic.check(true, true, SeverityLevel.MINOR, "high", SeverityLevel.MINOR, "high");
        // Clear alarm
        logic.computeNewState("d", SeverityLevel.OK, OK);      
        logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.MINOR, "high");
        // Ack.
        logic.acknowledge(true);
        logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        
        // Change the count
        count = 10;
        logic.setCount(count);
        logic.check(false, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        
        // (count-1) brief MINOR alarms have no effect
        System.out.println((count-1) + " ignored alarms....");
        for (int i=0; i<count-1; ++i)
        {
            logic.computeNewState("e", SeverityLevel.MINOR, "high");      
            logic.check(true, false, SeverityLevel.MINOR, "high", SeverityLevel.OK, OK);
            logic.computeNewState("f", SeverityLevel.OK, OK);
            logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        }

        // But when they reach the count, it matters
        System.out.println("Final alarm to get count of " + count);
        logic.computeNewState("g", SeverityLevel.MINOR, "high");      
        logic.check(true, true, SeverityLevel.MINOR, "high", SeverityLevel.MINOR, "high");
    }

    @Test
    public void testDisabledLatchedAnnunciatedAlarmAckOK()
    {
        System.out.println("* Disabled, latched, annunciated: Minor");
        final AlarmLogicDemo logic = new AlarmLogicDemo(true, true);
        logic.check(false, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        assertEquals("", logic.getAlarmState().getValue());
        assertTrue(logic.isEnabled());
        
        // Disabling results in one update that fakes an all OK
        // with message "Disabled"
        logic.setEnabled(false);
        logic.checkEnablementChange();
        assertFalse(logic.isEnabled());
        logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.OK, Messages.AlarmMessageDisabled);
        
        // Should now ignore received MINOR alarm
        logic.computeNewState("a", SeverityLevel.MINOR, "high");
        logic.check(false, false, SeverityLevel.OK, OK, SeverityLevel.OK, Messages.AlarmMessageDisabled);
        
        // Re-enable
        logic.setEnabled(true);
        logic.checkEnablementChange();
        assertTrue(logic.isEnabled());
        logic.check(true, true, SeverityLevel.MINOR, "high", SeverityLevel.MINOR, "high");
        assertEquals("a", logic.getAlarmState().getValue());
        
        // Another Minor doesn't matter
        logic.computeNewState("a2", SeverityLevel.MINOR, "high");
        logic.check(false, false, SeverityLevel.MINOR, "high", SeverityLevel.MINOR, "high");
        assertEquals("a", logic.getAlarmState().getValue());

        // Follow into Major
        logic.computeNewState("b", SeverityLevel.MAJOR, "hihi");
        logic.check(true, true, SeverityLevel.MAJOR, "hihi", SeverityLevel.MAJOR, "hihi");
        assertEquals("b", logic.getAlarmState().getValue());
        
        // Disable again
        logic.setEnabled(false);
        logic.checkEnablementChange();
        assertFalse(logic.isEnabled());
        logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.OK, Messages.AlarmMessageDisabled);
        
        // Re-enable, and MAJOR alarm resurfaces since nothing else was received
        logic.setEnabled(true);
        logic.checkEnablementChange();
        assertTrue(logic.isEnabled());
        logic.check(true, true, SeverityLevel.MAJOR, "hihi", SeverityLevel.MAJOR, "hihi");
        assertEquals("b", logic.getAlarmState().getValue());
    }
    
    /** There used to be an error in the logic:
     *  After getting 'count' alarms within 'delay',
     *  it would immediately react to the next one
     *  instead of waiting for another 'count'.
     *  This checks for that problem
     */
    @Test
    public void testUnlatchedAnnunciatedCount() throws Exception
    {
        System.out.println("* NonLatched, annunciated, count: minor, ok, minor, ok");
        final int delay = 10;
        int count = 5;
        final AlarmLogicDemo logic = new AlarmLogicDemo(false, true, delay, count);
        logic.check(false, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        
        // (count-1) brief MINOR alarms have no effect
        System.out.println((count-1) + " ignored alarms....");
        for (int i=0; i<count-1; ++i)
        {
            logic.computeNewState("a", SeverityLevel.MINOR, "high");      
            logic.check(true, false, SeverityLevel.MINOR, "high", SeverityLevel.OK, OK);
            logic.computeNewState("b", SeverityLevel.OK, OK);
            logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        }

        // But when they reach the count, it matters
        System.out.println("Final alarm to get count of " + count);
        logic.computeNewState("c", SeverityLevel.MINOR, "high");      
        logic.check(true, true, SeverityLevel.MINOR, "high", SeverityLevel.MINOR, "high");
        // Clear alarm
        logic.computeNewState("d", SeverityLevel.OK, OK);      
        logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        
        // Start over: another (count-1) brief MINOR alarms have no effect
        System.out.println((count-1) + " ignored alarms....");
        for (int i=0; i<count-1; ++i)
        {
            logic.computeNewState("a2", SeverityLevel.MINOR, "high");      
            logic.check(true, false, SeverityLevel.MINOR, "high", SeverityLevel.OK, OK);
            logic.computeNewState("b2", SeverityLevel.OK, OK);
            logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        }
        // But when they reach the count, it matters
        System.out.println("Final alarm to get count of " + count);
        logic.computeNewState("c2", SeverityLevel.MINOR, "high");      
        logic.check(true, true, SeverityLevel.MINOR, "high", SeverityLevel.MINOR, "high");
        // Clear alarm
        logic.computeNewState("d", SeverityLevel.OK, OK);      
        logic.check(true, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
    }

    @Test
    public void testMaintenanceMode() throws Exception
    {
        System.out.println("* testMaintenanceMode");
        AlarmLogicDemo logic = new AlarmLogicDemo(false, true);
        logic.check(false, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        
        AlarmLogicDemo.setMaintenanceMode(true);
     
        // Normal alarm
        logic.computeNewState("a", SeverityLevel.MINOR, "high");      
        logic.check(true, true, SeverityLevel.MINOR, "high", SeverityLevel.MINOR, "high");
        
        // INVALID is ack'ed automatically, no annunciation
        logic.computeNewState("b", SeverityLevel.INVALID, "Disconnected");
        logic.check(true, false, SeverityLevel.INVALID, "Disconnected", SeverityLevel.INVALID_ACK, "Disconnected");

        // Another non-INVALID alarm comes through
        logic.computeNewState("c", SeverityLevel.MINOR, "high");      
        logic.check(true, true, SeverityLevel.MINOR, "high", SeverityLevel.MINOR, "high");
        
        // -- Similar, but with 'priority' alarm --
        logic = new AlarmLogicDemo(false, true)
        {
            @Override
            public boolean isPriorityAlarm()
            {
                return true;
            }
        };
        logic.check(false, false, SeverityLevel.OK, OK, SeverityLevel.OK, OK);
        
        // Normal alarm
        logic.computeNewState("a", SeverityLevel.MINOR, "high");      
        logic.check(true, true, SeverityLevel.MINOR, "high", SeverityLevel.MINOR, "high");
        
        // INVALID is _not_ ack'ed, there _is_ annunciation:
        // Priority alarm ignores the maintenance mode
        logic.computeNewState("b", SeverityLevel.INVALID, "Disconnected");
        logic.check(true, true, SeverityLevel.INVALID, "Disconnected", SeverityLevel.INVALID, "Disconnected");

        // Another non-INVALID alarm comes through
        logic.computeNewState("c", SeverityLevel.MINOR, "high");      
        logic.check(true, true, SeverityLevel.MINOR, "high", SeverityLevel.MINOR, "high");
    }
}
