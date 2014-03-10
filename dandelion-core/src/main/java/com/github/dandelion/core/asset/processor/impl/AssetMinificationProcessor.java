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
package com.github.dandelion.core.asset.processor.impl;

import static com.github.dandelion.core.asset.web.AssetServlet.DANDELION_ASSETS_URL;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Beta;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.Assets;
import com.github.dandelion.core.asset.cache.AssetCacheSystem;
import com.github.dandelion.core.asset.processor.spi.AssetProcessor;
import com.github.dandelion.core.asset.wrapper.spi.AssetLocationWrapper;
import com.github.dandelion.core.config.Configuration;
import com.github.dandelion.core.utils.ResourceUtils;
import com.github.dandelion.core.utils.UrlUtils;
import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * <p>
 * Processor entry in charge of compressing all assets present in the
 * {@link Assets}.
 * 
 * <p>
 * This processor entry is based on YUI Compressor.
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.10.0
 */
@Beta
public class AssetMinificationProcessor extends AssetProcessor {

	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(AssetMinificationProcessor.class);

	public static final String MINIFICATION = "minification";
	public static final String MINIFICATION_ENABLED_KEY = "dandelion.minification.enabled";
	public static final String MINIFICATION_JS_MUNGE = "dandelion.minification.js.munge";
	public static final String MINIFICATION_JS_PRESERVE_SEMICOLONS = "dandelion.minification.js.preserveSemiColons";
	public static final String MINIFICATION_JS_DISABLE_OPTIMIZATIONS = "dandelion.minification.js.disableOptimizations";

	private boolean minificationEnabled = false;
	private boolean jsMunge = true;
	private boolean jsPreserveSemiColons = true;
	private boolean jsDisableOptimizations = true;

	public AssetMinificationProcessor() {
		this.minificationEnabled = Boolean.TRUE.toString().equals(
				Configuration.getProperty(MINIFICATION_ENABLED_KEY, Boolean.toString(minificationEnabled)));
		this.jsMunge = Boolean.TRUE.toString().equals(
				Configuration.getProperty(MINIFICATION_JS_MUNGE, Boolean.toString(minificationEnabled)));
		this.jsPreserveSemiColons = Boolean.TRUE.toString().equals(
				Configuration.getProperty(MINIFICATION_JS_PRESERVE_SEMICOLONS, Boolean.toString(minificationEnabled)));
		this.jsDisableOptimizations = Boolean.TRUE.toString()
				.equals(Configuration.getProperty(MINIFICATION_JS_DISABLE_OPTIMIZATIONS,
						Boolean.toString(minificationEnabled)));

		LOG.info("Asset minification is {}", minificationEnabled ? "enabled" : "disabled");
		if (minificationEnabled) {
			LOG.debug("With the following parameters:");
			LOG.debug("   JS munge is {}", jsMunge ? "enabled" : "disabled");
			LOG.debug("   JS preserve semicolons is {}", jsPreserveSemiColons ? "enabled" : "disabled");
			LOG.debug("   JS disable optimizations is {}", jsDisableOptimizations ? "enabled" : "disabled");
		}
	}

	@Override
	public String getProcessorKey() {
		return MINIFICATION;
	}

	@Override
	public int getRank() {
		return 2000;
	}

	@Override
	public Set<Asset> process(Set<Asset> assets, HttpServletRequest request) {
		if (!minificationEnabled) {
			return assets;
		}

		String context = UrlUtils.getCurrentUrl(request, true).toString();
		context = context.replaceAll("\\?", "_").replaceAll("&", "_");

		Set<Asset> compressedAssets = new LinkedHashSet<Asset>();

		for (Asset asset : assets) {
			String cacheKey = AssetCacheSystem.generateCacheKey(context, asset.getLocation(), asset.getName() + "-min",
					asset.getType());

			// Updates the cache in order for the compressed content to be
			// retrieved by the servlet
			LOG.debug("Cache updated with minified assets (key={})", asset.getAssetKey());
			cacheCompressedContent(request, context, asset.getLocation(), asset, cacheKey);

			String newLocation = UrlUtils.getBaseUrl(request) + DANDELION_ASSETS_URL + cacheKey;

			Asset minifiedAsset = new Asset(asset.getName() + "-min", asset.getVersion(), asset.getType(), newLocation);
			compressedAssets.add(minifiedAsset);
			LOG.debug("New minified asset created: {}", minifiedAsset);
		}
		return compressedAssets;
	}

	private void cacheCompressedContent(HttpServletRequest request, String context, String location, Asset asset,
			String cacheKey) {
		String content = compress(asset, request);
		AssetCacheSystem.storeContent(context, location, MINIFICATION, asset.getType(), content);
	}

	private String compress(Asset asset, HttpServletRequest request) {
		String content = extractContent(asset, request);
		switch (asset.getType()) {
		case css:
			LOG.debug("CSS minification for asset {}", asset.getAssetKey());
			return compressCss(asset.getAssetKey(), content);
		case js:
			LOG.debug("JS minification for asset {}", asset.getAssetKey());
			return compressJs(asset.getAssetKey(), content);
		default:
			LOG.debug("No minification for asset {}", asset.getAssetKey());
			return content;
		}
	}

	private String compressJs(String assetKey, String content) {
		LOG.debug("JS minification with YUI compressor");
		Writer output = new StringWriter();

		try {
			JavaScriptCompressor compressor = new JavaScriptCompressor(new StringReader(content),
					new YuiCompressorErrorReporter());
			compressor.compress(output, -1, jsMunge, false, jsPreserveSemiColons, jsDisableOptimizations);
		}
		catch (EvaluatorException e) {
			LOG.error("YUI compressor can't evaluate the content of {}", assetKey);
			LOG.debug("YUI compressor can't evaluate the content [{}]", content);
			throw DandelionException.wrap(e, null).set("assetKey", assetKey).set("content", content);
		}
		catch (IOException e) {
			LOG.error("YUI compressor can't access to the content of {}", assetKey);
			throw DandelionException.wrap(e, null).set("assetKey", assetKey);
		}

		return output.toString();
	}

	private String compressCss(String assetKey, String content) {
		LOG.debug("CSS compression with YUI compressor");
		Writer output = new StringWriter();

		try {
			CssCompressor compressor = new CssCompressor(new StringReader(content));
			compressor.compress(output, -1);
		}
		catch (IOException e) {
			LOG.error("YUI compressor can't access to the content of {}", assetKey);
			throw DandelionException.wrap(e, null).set("assetKey", assetKey);
		}

		return output.toString();
	}

	private String extractContent(Asset asset, HttpServletRequest request) {
		Map<String, AssetLocationWrapper> wrappers = Assets.getAssetLocationWrappers();
		StringBuilder groupContent = new StringBuilder();

		for (Map.Entry<String, String> location : asset.getLocations().entrySet()) {
			AssetLocationWrapper wrapper = wrappers.get(location.getKey());
			String content;
			if (wrapper != null && wrapper.isActive()) {
				content = wrapper.getWrappedContent(asset, request);
			}
			else {
				content = ResourceUtils.getContentFromUrl(request, location.getValue(), true);
			}
			if (content != null) {
				groupContent.append(content).append("\n");
			}
		}
		return groupContent.toString();
	}

	public boolean isMinificationEnabled() {
		return minificationEnabled;
	}
}
