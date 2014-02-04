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

package com.github.dandelion.core.asset.wrapper.impl;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.wrapper.spi.AssetLocationWrapper;
import com.github.dandelion.core.utils.RequestUtils;
import com.github.dandelion.core.utils.ResourceUtils;

/**
 * <p>
 * Location wrapper for {@code webapp} assets.
 * 
 * <p>
 * Basically, a "webapp asset" is an asset coming from the resources of the
 * deployed web application.
 * 
 * @author Romain Lespinasse
 * @since 0.2.0
 */
public class WebappLocationWrapper implements AssetLocationWrapper {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLocationKey() {
		return "webapp";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getWrappedLocation(Asset asset, HttpServletRequest request) {
		String location = asset.getLocations().get(getLocationKey());
		String base = RequestUtils.getBaseUrl(request);
		boolean pathLocation = location.startsWith("/");
		boolean pathBase = base.endsWith("/");
		if (pathLocation && pathBase) {
			location = location.substring(1);
		}
		else if (!pathLocation && !pathBase) {
			location = "/" + location;
		}
		return base + location;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getWrappedContent(Asset asset, HttpServletRequest request) {
		String location = asset.getLocations().get(getLocationKey());
		return ResourceUtils.getContentFromUrl(request, location, true);
	}
}
