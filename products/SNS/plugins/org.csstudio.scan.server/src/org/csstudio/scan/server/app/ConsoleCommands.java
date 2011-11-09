/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * The scan engine idea is based on the "ScanEngine" developed
 * by the Software Services Group (SSG),  Advanced Photon Source,
 * Argonne National Laboratory,
 * Copyright (c) 2011 , UChicago Argonne, LLC.
 * 
 * This implementation, however, contains no SSG "ScanEngine" source code
 * and is not endorsed by the SSG authors.
 ******************************************************************************/
package org.csstudio.scan.server.app;

import java.rmi.RemoteException;
import java.util.List;

import org.csstudio.scan.server.ScanInfo;
import org.csstudio.scan.server.ScanServer;
import org.csstudio.scan.server.ScanServerImpl;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

/** Commands for the OSGi console
 *  Application registers this {@link CommandProvider}
 *
 *  @see CommandProvider
 *  @see Application#start(org.eclipse.equinox.app.IApplicationContext)
 *
 *  @author Kay Kasemir
 */
public class ConsoleCommands implements CommandProvider
{
    final private ScanServerImpl server;

    /** Initialize
     *  @param server {@link ScanServer}
     */
    public ConsoleCommands(final ScanServerImpl server)
    {
        this.server = server;
    }

    /** Provide help to the console
     *  @see CommandProvider
     * {@inheritDoc}
     */
    @Override
    public String getHelp()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append("---ScanServer commands---\n");
        buf.append("\tscans           - List all scans\n");
        buf.append("\tpause           - Pause current scan\n");
        buf.append("\tresume          - Resume paused scan\n");
        buf.append("\tabort  ID       - Abort scan with given ID\n");
        buf.append("\tremoveCompleted - Remove completed scans\n");
        return buf.toString();
    }

    // Note:
    // Every method that starts with underscore
    // and takes CommandInterpreter arg will be accessible
    // as command in console

    /** 'scans' command */
    public Object _scans(final CommandInterpreter intp)
    {
        try
        {
            final List<ScanInfo> infos = server.getScanInfos();
            if (infos.size() <= 0)
                intp.println("- No scans -");
            else
                for (ScanInfo info : infos)
                    intp.println(info.toString());
        }
        catch (RemoteException ex)
        {
            intp.printStackTrace(ex);
        }
        return null;
    }

    /** 'pause' command */
    public Object _pause(final CommandInterpreter intp)
    {
        try
        {
            server.pause(-1);
        }
        catch (RemoteException ex)
        {
            intp.printStackTrace(ex);
        }
        return _scans(intp);
    }

    /** 'resume' command */
    public Object _resume(final CommandInterpreter intp)
    {
        try
        {
            server.resume(-1);
        }
        catch (RemoteException ex)
        {
            intp.printStackTrace(ex);
        }
        return _scans(intp);
    }

    /** 'abort' command */
    public Object _abort(final CommandInterpreter intp)
    {
        final String arg = intp.nextArgument();
        if (arg == null)
        {
            intp.println("Syntax:");
            intp.println("   abort  ID-of-scan-to-abort");
            return null;
        }
        try
        {
            final long id = Long.parseLong(arg.trim());
            server.abort(id);
        }
        catch (Throwable ex)
        {
            intp.printStackTrace(ex);
        }
        return _scans(intp);
    }

    /** 'removeCompleted' command */
    public Object _removeCompleted(final CommandInterpreter intp)
    {
        try
        {
            server.removeCompletedScans();
        }
        catch (RemoteException ex)
        {
            intp.printStackTrace(ex);
        }
        return _scans(intp);
    }
}