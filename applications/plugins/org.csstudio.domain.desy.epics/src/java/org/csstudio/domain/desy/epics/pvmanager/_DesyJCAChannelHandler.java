/**
 * Copyright (C) 2010-12 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.csstudio.domain.desy.epics.pvmanager;

import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_CTRL_Double;
import gov.aps.jca.dbr.DBR_LABELS_Enum;
import gov.aps.jca.dbr.DBR_TIME_Byte;
import gov.aps.jca.dbr.DBR_TIME_Double;
import gov.aps.jca.dbr.DBR_TIME_Enum;
import gov.aps.jca.dbr.DBR_TIME_Float;
import gov.aps.jca.dbr.DBR_TIME_Int;
import gov.aps.jca.dbr.DBR_TIME_Short;
import gov.aps.jca.dbr.DBR_TIME_String;
import gov.aps.jca.event.ConnectionEvent;
import gov.aps.jca.event.ConnectionListener;
import gov.aps.jca.event.GetEvent;
import gov.aps.jca.event.GetListener;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.epics.pvmanager.ChannelWriteCallback;
import org.epics.pvmanager.DataSourceTypeAdapter;
import org.epics.pvmanager.MultiplexedChannelHandler;
import org.epics.pvmanager.ValueCache;

/**
 * A ChannelHandler for the JCADataSource.
 * <p>
 * NOTE: this class is extensible as per Bastian request so that DESY can hook
 * a different type factory. This is a temporary measure until the problem
 * is solved in better, more general way, so that data sources
 * can work only with data source specific types, while allowing
 * conversions to normalized type through operators. The contract of this
 * class is, therefore, expected to change.
 * <p>
 * Related changes are marked so that they are not accidentally removed in the
 * meantime, and can be intentionally removed when a better solution is implemented.
 *
 * @author carcassi
 */
public class _DesyJCAChannelHandler extends MultiplexedChannelHandler<Channel, DesyJCAMessagePayload> {

    private static final int LARGE_ARRAY = 100000;
    private final DesyJCADataSource jcaDataSource;
    private volatile Channel channel;
    private volatile boolean needsMonitor;
    private volatile boolean largeArray = false;
    private boolean putCallback = false;

    private final static Pattern hasOptions = Pattern.compile(".* \\{.*\\}");

    public _DesyJCAChannelHandler(final String channelName, final DesyJCADataSource jcaDataSource) {
        super(channelName);
        this.jcaDataSource = jcaDataSource;
        parseParameters();
    }

    private void parseParameters() {
        if (hasOptions.matcher(getChannelName()).matches()) {
            if (getChannelName().endsWith("{\"putCallback\":true}")) {
                putCallback = true;
            } else if (getChannelName().endsWith("{\"putCallback\":false}")) {
                putCallback = false;
            } else {
                throw new IllegalArgumentException("Option not recognized for " + getChannelName());
            }
        }
    }

    public boolean isPutCallback() {
        return putCallback;
    }

    @Override
    protected DataSourceTypeAdapter findTypeAdapter(final ValueCache<?> cache, final Channel channel) {
        return jcaDataSource.getTypeSupport().find(cache, channel);
    }

    @Override
    public void connect() {
        try {
            // Give the listener right away so that no event gets lost
	    // If it's a large array, connect using lower priority
	    if (largeArray) {
                channel = jcaDataSource.getContext().createChannel(getChannelName(), connectionListener, Channel.PRIORITY_MIN);
	    } else {
                channel = jcaDataSource.getContext().createChannel(getChannelName(), connectionListener, (short) (Channel.PRIORITY_MIN + 1));
	    }
            needsMonitor = true;
        } catch (final CAException ex) {
            throw new RuntimeException("JCA Connection failed", ex);
        }
    }

    private void putWithCallback(final Object newValue, final ChannelWriteCallback callback) throws CAException {
        final PutListener listener = new PutListener() {

            @Override
            public void putCompleted(final PutEvent ev) {
                if (ev.getStatus().isSuccessful()) {
                    callback.channelWritten(null);
                } else {
                    callback.channelWritten(new Exception(ev.toString()));
                }
            }
        };
        if (newValue instanceof String) {
            channel.put(newValue.toString(), listener);
        } else if (newValue instanceof byte[]) {
            channel.put((byte[]) newValue, listener);
        } else if (newValue instanceof short[]) {
            channel.put((short[]) newValue, listener);
        } else if (newValue instanceof int[]) {
            channel.put((int[]) newValue, listener);
        } else if (newValue instanceof float[]) {
            channel.put((float[]) newValue, listener);
        } else if (newValue instanceof double[]) {
            channel.put((double[]) newValue, listener);
        } else if (newValue instanceof Byte || newValue instanceof Short
                || newValue instanceof Integer || newValue instanceof Long) {
            channel.put(((Number) newValue).longValue(), listener);
        } else if (newValue instanceof Float || newValue instanceof Double) {
            channel.put(((Number) newValue).doubleValue(), listener);
        } else {
            throw new RuntimeException("Unsupported type for CA: " + newValue.getClass());
        }
        jcaDataSource.getContext().flushIO();
    }

    private void put(final Object newValue, final ChannelWriteCallback callback) throws CAException {
        if (newValue instanceof String) {
            channel.put(newValue.toString());
        } else if (newValue instanceof byte[]) {
            channel.put((byte[]) newValue);
        } else if (newValue instanceof short[]) {
            channel.put((short[]) newValue);
        } else if (newValue instanceof int[]) {
            channel.put((int[]) newValue);
        } else if (newValue instanceof float[]) {
            channel.put((float[]) newValue);
        } else if (newValue instanceof double[]) {
            channel.put((double[]) newValue);
        } else if (newValue instanceof Byte || newValue instanceof Short
                || newValue instanceof Integer || newValue instanceof Long) {
            channel.put(((Number) newValue).longValue());
        } else if (newValue instanceof Float || newValue instanceof Double) {
            channel.put(((Number) newValue).doubleValue());
        } else {
            callback.channelWritten(new Exception(new RuntimeException("Unsupported type for CA: " + newValue.getClass())));
            return;
        }
        jcaDataSource.getContext().flushIO();
        callback.channelWritten(null);
    }

    private void setup(final Channel channel) throws CAException {
        final DBRType metaType = metadataFor(channel);

        // If metadata is needed, get it
        if (metaType != null) {
            // Need to use callback for the listener instead of doing a synchronous get
            // (which seemed to perform better) because JCA (JNI implementation)
            // would return an empty list of labels for the Enum metadata
            channel.get(metaType, 1, new GetListener() {

                @Override
                public void getCompleted(final GetEvent ev) {
                    synchronized(_DesyJCAChannelHandler.this) {
                        // In case the metadata arrives after the monitor
                        MonitorEvent event = null;
                        if (getLastMessagePayload() != null) {
                            event = getLastMessagePayload().getEvent();
                        }
                        processMessage(new DesyJCAMessagePayload(ev.getDBR(), event));
                    }
                }
            });
        }

        // Start the monitor only if the channel was (re)created, and
        // not because a disconnection/reconnection
        if (needsMonitor) {
            channel.addMonitor(valueTypeFor(channel), countFor(channel), jcaDataSource.getMonitorMask(), monitorListener);
            needsMonitor = false;
        }

        // Setup metadata monitor if required
        if (jcaDataSource.isDbePropertySupported() && metaType != null) {
            channel.addMonitor(metaType, 1, Monitor.PROPERTY, new MonitorListener() {

                @Override
                public void monitorChanged(final MonitorEvent ev) {
                    synchronized(_DesyJCAChannelHandler.this) {
                        // In case the metadata arrives after the monitor
                        MonitorEvent event = null;
                        if (getLastMessagePayload() != null) {
                            event = getLastMessagePayload().getEvent();
                        }
                        processMessage(new DesyJCAMessagePayload(ev.getDBR(), event));
                    }
                }
            });
        }

        // Flush the entire context (it's the best we can do)
        channel.getContext().flushIO();
    }

    private final ConnectionListener connectionListener = new ConnectionListener() {

            @Override
            public void connectionChanged(final ConnectionEvent ev) {
                synchronized(_DesyJCAChannelHandler.this) {
                    try {
                        // Take the channel from the event so that there is no
                        // synchronization problem
                        final Channel channel = (Channel) ev.getSource();

                        // Check whether the channel is large and was opened
                        // as large. Reconnect if does not match
                        if (ev.isConnected() && channel.getElementCount() >= LARGE_ARRAY && !largeArray) {
                            disconnect();
                            largeArray = true;
                            connect();
                            return;
                        }

                        // Setup monitors on connection
                        processConnection(channel);
                        if (ev.isConnected()) {
                            setup(channel);
                        }

                    } catch (final Exception ex) {
                        reportExceptionToAllReadersAndWriters(ex);
                    }
                }
            }
        };;

    private final MonitorListener monitorListener = new MonitorListener() {

        @Override
        public void monitorChanged(final MonitorEvent event) {
            synchronized(_DesyJCAChannelHandler.this) {
                DBR metadata = null;
                if (getLastMessagePayload() != null) {
                    metadata = getLastMessagePayload().getMetadata();
                }
                processMessage(new DesyJCAMessagePayload(metadata, event));
            }
        }
    };

    @Override
    public void disconnect() {
        try {
            // Close the channel
            channel.destroy();
        } catch (final CAException ex) {
            throw new RuntimeException("JCA Disconnect fail", ex);
        } finally {
            channel = null;
            processConnection(null);
        }
    }

    @Override
    public void write(final Object newValue, final ChannelWriteCallback callback) {
        try {
            if (isPutCallback()) {
				putWithCallback(newValue, callback);
			} else {
				put(newValue, callback);
			}
        } catch (final CAException ex) {
            callback.channelWritten(ex);
        }
    }

    @Override
    protected boolean isConnected(final Channel channel) {
        return isChannelConnected(channel);
    }


    static boolean isChannelConnected(final Channel channel) {
        return channel != null && channel.getConnectionState() == Channel.ConnectionState.CONNECTED;
    }

    @Override
    public synchronized Map<String, Object> getProperties() {
        final Map<String, Object> properties = new HashMap<String, Object>();
        if (channel != null) {
            properties.put("Channel name", channel.getName());
            properties.put("Connection state", channel.getConnectionState().getName());
            if (channel.getConnectionState() == Channel.ConnectionState.CONNECTED) {
                properties.put("Hostname", channel.getHostName());
                properties.put("Channel type", channel.getFieldType().getName());
                properties.put("Element count", channel.getElementCount());
                properties.put("Read access", channel.getReadAccess());
                properties.put("Write access", channel.getWriteAccess());
            }
        }
        return properties;
    }

    protected DBRType metadataFor(final Channel channel) {
        final DBRType type = channel.getFieldType();

        if (type.isBYTE() || type.isSHORT() || type.isINT() || type.isFLOAT() || type.isDOUBLE()) {
			return DBR_CTRL_Double.TYPE;
		}

        if (type.isENUM()) {
			return DBR_LABELS_Enum.TYPE;
		}

        return null;
    }

    protected int countFor(final Channel channel) {
        if (channel.getElementCount() == 1) {
			return 1;
		}

        if (jcaDataSource.isVarArraySupported()) {
			return 0;
		} else {
			return channel.getElementCount();
		}
    }

    protected DBRType valueTypeFor(final Channel channel) {
        final DBRType type = channel.getFieldType();

        // TODO: .RTYP should not request the time

        // For scalar numbers, only use Double or Int
        if (channel.getElementCount() == 1) {
            if (type.isBYTE() || type.isSHORT() || type.isINT()) {
				return DBR_TIME_Int.TYPE;
			}
            if (type.isFLOAT() || type.isDOUBLE()) {
				return DBR_TIME_Double.TYPE;
			}
        }

        if (type.isBYTE()) {
            return DBR_TIME_Byte.TYPE;
        } else if (type.isSHORT()) {
            return DBR_TIME_Short.TYPE;
        } else if (type.isINT()) {
            return DBR_TIME_Int.TYPE;
        } else if (type.isFLOAT()) {
            return DBR_TIME_Float.TYPE;
        } else if (type.isDOUBLE()) {
            return DBR_TIME_Double.TYPE;
        } else if (type.isENUM()) {
            return DBR_TIME_Enum.TYPE;
        } else if (type.isSTRING()) {
            return DBR_TIME_String.TYPE;
        }

        throw new IllegalArgumentException("Unsupported type " + type);
    }
}
