/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2014 Dandelion
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of Dandelion nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.dandelion.core.asset.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.cache.spi.AssetCache;

/**
 * <p>
 * Dandelion servlet in charge of serving the assets stored in the configured
 * {@link AssetCache}.
 * 
 * @author Thibault Duchateau
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public class AssetServlet extends HttpServlet {

	private static final long serialVersionUID = -6874842638265359418L;

	private static Logger LOG = LoggerFactory.getLogger(AssetServlet.class);

	public static final String DANDELION_ASSETS = "dandelionAssets";
	public static final String DANDELION_ASSETS_URL = "/dandelion-assets/";
	public static final String DANDELION_ASSETS_URL_PATTERN = "/dandelion-assets/*";

	private HttpHeadersConfigurer httpHeadersConfigurer;
	

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		getLogger().debug("Dandelion Asset servlet captured GET request {}", request.getRequestURI());

		Context context = (Context) request.getAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE);
		httpHeadersConfigurer = new HttpHeadersConfigurer(context);
		
		// Get the asset content thanks to the cache key
		String assetKey = context.getCacheManager().getCacheKeyFromRequest(request);
		AssetType assetType = AssetType.typeOfAsset(assetKey);
		
		// Configure response headers
		httpHeadersConfigurer.configureResponseHeaders(response, assetType.getContentType());
		
		// Send the asset's content
		PrintWriter writer = response.getWriter();
		writer.write(context.getCacheManager().getContent(assetKey));
		
		// The response is explicitely closed here instead of setting a
		// Content-Length header
		writer.close();
	}

	protected Logger getLogger() {
		return LOG;
	}
}
