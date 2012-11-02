/*
 * Copyright (c) 2011 Stiftung Deutsches Elektronen-Synchrotron,
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
package org.csstudio.domain.desy.epics.pvmanager;

import gov.aps.jca.CAException;
import gov.aps.jca.Context;
import gov.aps.jca.JCALibrary;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.epics.pvmanager.ChannelHandler;
import org.epics.pvmanager.jca.JCADataSource;



/**
 * The DESY specific data source that create DESY specific channel handlers (which will refer to
 * DESY specific type factories).
 *
 * @author bknerr
 * @since 30.08.2011
 */
public class DesyJCADataSource extends JCADataSource {

    private static final Logger log = Logger.getLogger(JCADataSource.class.getName());

	DesyJCATypeSupport typeSupport;
    public DesyJCADataSource(@Nonnull final String className,
                             final int monitorMask) {
    	 this(createContext(className), monitorMask);

    }

    /**
     * Creates a new data source using the given context. The context will
     * never be closed.
     *
     * @param jcaContext the context to be used
     * @param monitorMask Monitor.VALUE, ...
     */
    public DesyJCADataSource(final Context jcaContext, final int monitorMask) {
        this(jcaContext, monitorMask, new DesyJCATypeSupport(new DesyJCAVTypeAdapterSet()));
    }
    private static Context createContext(final String className) {
        try {
            final JCALibrary jca = JCALibrary.getInstance();
            return jca.createContext(className);
        } catch (final CAException ex) {
            log.log(Level.SEVERE, "JCA context creation failed", ex);
            throw new RuntimeException("JCA context creation failed", ex);
        }
    }
    /**
     * Creates a new data source using the given context. The context will
     * never be closed. The type mapping con be configured with a custom
     * type support.
     *
     * @param jcaContext the context to be used
     * @param monitorMask Monitor.VALUE, ...
     * @param typeSupport type support to be used
     */
    public DesyJCADataSource(final Context jcaContext, final int monitorMask, final DesyJCATypeSupport typeSupport) {

      	this.typeSupport=typeSupport;
    }
    @Override
    @Nonnull
    protected ChannelHandler createChannel(@Nonnull final String channelName) {
           return new DesyJCAChannelHandler(channelName, this);
    }
    DesyJCATypeSupport getTypeSupport() {
        return typeSupport;
    }
    @CheckForNull
    public DesyJCAChannelHandler getHandler(@Nonnull final String channelName) {
        return (DesyJCAChannelHandler) getChannels().get(channelName);
    }
}
