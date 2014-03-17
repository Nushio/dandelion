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
package com.github.dandelion.core.asset.locator.spi;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.processor.spi.AssetProcessor;
import com.github.dandelion.core.asset.web.AssetServlet;
import com.github.dandelion.core.storage.AssetStorageUnit;

/**
 * <p>
 * SPI for all asset locators.
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.2.0
 */
public interface AssetLocator {

	/**
	 * @return the location key associated to the locator.
	 */
	String getLocationKey();

	/**
	 * <p>
	 * Computes and returns the location of the asset.
	 * 
	 * @param AssetStorageUnit
	 *            The asset storage unit from which the location should be
	 *            extracted.
	 * @param request
	 *            The current HTTP request.
	 * @return the customized location
	 */
	String getLocation(AssetStorageUnit asu, HttpServletRequest request);

	/**
	 * <p>
	 * Returns the content of the given {@link AssetStorageUnit}.
	 * 
	 * @param asu
	 *            The asset storage unit from which the content should be
	 *            extracted.
	 * @param request
	 *            The current HTTP request.
	 * @return the content of location
	 */
	String getContent(AssetStorageUnit asu, HttpServletRequest request);

	/**
	 * <p>
	 * Returns the content of the given {@link Asset}.
	 * <p>
	 * Note that this method can be used to access the asset's content after the
	 * {@link AssetProcessor}'s execution.
	 * 
	 * @param asset
	 *            The asset from which the content should be extracted.
	 * @param request
	 *            The current HTTP request.
	 * @return
	 */
	String getContent(Asset asset, HttpServletRequest request);

	/**
	 * @return {@code true} if the asset locator is active, otherwise
	 *         {@code false}. By default, all {@link AssetLocator}s are active.
	 */
	boolean isActive();

	/**
	 * @return {@code true} if the asset has to be cached in order to be
	 *         accessed by the {@link AssetServlet}, otherwise {@code false}.
	 */
	boolean isCachingForced();
}
