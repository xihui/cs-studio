/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.archive.common.engine.httpserver;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.csstudio.archive.common.engine.model.ArchiveChannelBuffer;
import org.csstudio.archive.common.engine.model.EngineModel;
import org.csstudio.domain.desy.epics.name.EpicsChannelName;

/** Provide web page with detail for one channel.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
class ShowChannelResponse extends AbstractChannelResponse {

    private static String URL_BASE_PAGE;
    private static String URL_SHOW_CHANNEL_ACTION;
    static {
        URL_SHOW_CHANNEL_ACTION = "show";
        URL_BASE_PAGE = URL_CHANNEL_PAGE + "/" + URL_SHOW_CHANNEL_ACTION;
    }

    /** Avoid serialization errors */
    private static final long serialVersionUID = 1L;

    ShowChannelResponse(@Nonnull final EngineModel model) {
        super(model);
    }

    @Override
    protected void fillResponse(@Nonnull final HttpServletRequest req,
                                @Nonnull final HttpServletResponse resp) throws Exception {
        final EpicsChannelName name = parseEpicsNameOrConfigureRedirectResponse(req, resp);
        if (name == null) {
            return;
        }
        final ArchiveChannelBuffer<?, ?> channel = getModel().getChannel(name.toString());
        if (channel == null) {
            resp.sendError(400, "Unknown channel " + name.toString());
            return;
        }

        // HTML table similar to group's list of channels
        final HTMLWriter html = new HTMLWriter(resp, "Archive Engine Channel",name.getBaseName());

     //   createChannelTable(channel, html);

        html.close();
    }


    @Nonnull
    public static String baseUrl() {
        return URL_BASE_PAGE;
    }
    @Nonnull
    public static String linkTo(@Nonnull final String name) {
        return linkTo(name, name);
    }
    @Nonnull
    public static String linkTo(@Nonnull final String name, @Nonnull final String linkText) {
        return new Url(baseUrl()).with(PARAM_NAME, name).link(linkText);
    }
    @Nonnull
    public static String urlTo(@Nonnull final String name) {
        return new Url(baseUrl()).with(PARAM_NAME, name).url();
    }
}
