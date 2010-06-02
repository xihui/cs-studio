/*
 * Copyright (c) 2008 Stiftung Deutsches Elektronen-Synchrotron,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS.
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE.
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS,
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION,
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 */
package org.csstudio.alarm.treeView.model;

import java.util.Date;


/**
 * Represents an alarm.
 * <p>
 * Instances of this class are immutable for easy sharing between threads.
 *
 * @author Joerg Rathlev
 */
public final class Alarm {

	/**
	 * The name of the object to which this alarm applies.
	 */
	private final String _objectName;

	/**
	 * The severity of this alarm.
	 */
	private final Severity _severity;

	/**
	 * The time at which this alarm event occured.
	 */
	private final Date _eventtime;


	/**
	 * Creates a new alarm with the given severity.
	 * @param objectName the name of the object on which this alarm occured.
	 * @param severity the severity of the alarm.
	 *
	 * @deprecated use {@link #Alarm(String, Severity, Date)} instead.
	 */
	@Deprecated
	public Alarm(final String objectName, final Severity severity) {
		this(objectName, severity, new Date());
	}


	/**
	 * Creates a new alarm.
	 *
	 * @param objectName the name of the object on which this alarm occured.
	 * @param severity the severity of the alarm.
	 * @param eventtime the time at which the alarm event occured.
	 */
	public Alarm(final String objectName, final Severity severity, final Date eventtime) {
		_objectName = objectName;
		_severity = severity;
		_eventtime = (Date) eventtime.clone();
	}


	/**
	 * Returns the severity of this alarm.
	 * @return the severity of this alarm.
	 */
	public final Severity getSeverity() {
		return _severity;
	}


	/**
	 * Returns whether this alarm has a higher severity than some other alarm.
	 * If the other alarm is <code>null</code>, this method returns
	 * <code>true</code>.
	 *
	 * @param alarm
	 *            the alarm to compare to.
	 * @return <code>true</code> if this alarm has a higher severity,
	 *         <code>false</code> otherwise.
	 */
	public boolean severityHigherThan(final Alarm alarm) {
		return (alarm == null) || (_severity.compareTo(alarm._severity) > 0);
	}


	/**
	 * Returns whether this alarm occured after some other alarm. If the other
	 * alarm is <code>null</code>, this method returns <code>true</code>.
	 *
	 * @param alarm
	 *            the alarm to compare to.
	 * @return <code>true</code> if this alarm occured after the other alarm,
	 *         <code>false</code> otherwise.
	 */
	public boolean occuredAfter(final Alarm alarm) {
		return (alarm == null) || _eventtime.after(alarm._eventtime);
	}


	/**
	 * Returns the name of the object on which this alarm occured.
	 * @return the name of the object on which this alarm occured.
	 */
	public final String getObjectName() {
		return _objectName;
	}


	/**
	 * Returns a string representation of this alarm.
	 * @return a string representation of this alarm.
	 */
	@Override
    public final String toString() {
		return "Alarm[" + _objectName + "," + _severity + "]";
	}
}
