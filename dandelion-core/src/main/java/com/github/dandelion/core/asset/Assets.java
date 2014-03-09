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
package com.github.dandelion.core.asset;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.DevMode;
import com.github.dandelion.core.asset.processor.AssetProcessorSystem;
import com.github.dandelion.core.asset.processor.spi.AssetProcessor;
import com.github.dandelion.core.asset.web.AssetRequestContext;
import com.github.dandelion.core.asset.wrapper.spi.AssetLocationWrapper;
import com.github.dandelion.core.bundle.Bundle;
import com.github.dandelion.core.bundle.BundleStorage;

/**
 * <p>
 * Main entry point for accessing the assets managed by Dandelion.
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * 
 * @since 0.10.0
 */
public final class Assets {

	private static AssetConfigurator assetConfigurator;
	private static BundleStorage bundleStorage;

	/**
	 * Initialize Assets only if needed
	 */
	public static void initializeIfNeeded() {
		if (bundleStorage == null || DevMode.isEnabled()) {
			initializeBundleStorage();
		}
		if (assetConfigurator == null || DevMode.isEnabled()) {
			initializeAssetConfigurator();
		}
	}

	/**
	 * Initialize Assets Configurator only if needed
	 */
	private static synchronized void initializeAssetConfigurator() {
		if (assetConfigurator == null) {
			assetConfigurator = new AssetConfigurator();
			assetConfigurator.initialize();
		}
	}

	private static synchronized void initializeBundleStorage() {
		if (bundleStorage == null) {
			bundleStorage = new BundleStorage();
		}
	}

	/**
	 * Get Configured Locations of Assets<br/>
	 * 
	 * Configured by assets.locations in 'dandelion/*.properties'
	 * 
	 * @return locations of Assets
	 */
	public static List<String> getAssetLocations() {
		initializeIfNeeded();
		return assetConfigurator.getAssetLocations();
	}

	/**
	 * Get Configured Wrappers for Locations of Assets<br/>
	 * 
	 * Configured by assets.locations in 'dandelion/*.properties'
	 * 
	 * @return wrappers for locations of Assets
	 */
	public static Map<String, AssetLocationWrapper> getAssetLocationWrappers() {
		initializeIfNeeded();
		return assetConfigurator.getAssetsLocationWrappers();
	}

	/**
	 * Returns the implementation of {@link AssetLocationWrapper} corresponding
	 * to the given location key.
	 * 
	 * @return the location wrapper.
	 */
	public static AssetLocationWrapper getAssetLocationWrapper(String locationKey) {
		initializeIfNeeded();
		return assetConfigurator.getAssetsLocationWrappers().get(locationKey);
	}

	/**
	 * @param assets
	 *            assets to filter
	 * @param filters
	 *            exclude assets names
	 * @return a filtered list of assets
	 */
	public static Set<Asset> excludeByName(Set<Asset> assets, String... filters) {
		Set<Asset> _assets = new LinkedHashSet<Asset>();
		List<String> _filters = new ArrayList<String>();

		if (filters != null) {
			for (String filter : filters) {
				_filters.add(filter.toLowerCase());
			}
		}

		for (Asset asset : assets) {
			if (!_filters.contains(asset.getName().toLowerCase())
					&& !_filters.contains(asset.getAssetKey().toLowerCase())) {
				_assets.add(asset);
			}
		}

		return _assets;
	}

	public static Set<Asset> assetsFor(HttpServletRequest request, String[] excludedAssets, String... bundleNames) {
		initializeIfNeeded();

		Set<Asset> assets = new LinkedHashSet<Asset>();

		for (String bundleName : bundleNames) {
			List<String> bundles = Assets.getStorage().getBundleDag().bundlesFor(bundleName);
			for (String s : bundles) {
				Bundle b = Assets.getStorage().getBundleDag().getVertex(s);
				if (b.getAssets() != null) {
					assets.addAll(b.getAssets());
				}
			}
		}

		assets = excludeByName(assets, excludedAssets);
		return processedAssets(assets, request);
	}

	public static Set<Asset> assetsFor(HttpServletRequest request, String bundleName) {
		initializeIfNeeded();
		String[] bundleNames = new String[] { bundleName };
		String[] excludedAssets = AssetRequestContext.get(request).getExcludedAssets();
		return assetsFor(request, excludedAssets, bundleNames);
	}

	public static Set<Asset> assetsFor(HttpServletRequest request, AssetDOMPosition position) {
		initializeIfNeeded();
		Set<Asset> assets = assetsFor(position, AssetRequestContext.get(request).getBundles(true));
		String[] excludedAssets = AssetRequestContext.get(request).getExcludedAssets();
		assets = excludeByName(assets, excludedAssets);
		return AssetProcessorSystem.process(assets, request);
	}

	public static Set<Asset> assetsFor(HttpServletRequest request) {
		initializeIfNeeded();
		String[] bundleNames = AssetRequestContext.get(request).getBundles(true);
		String[] excludedAssets = AssetRequestContext.get(request).getExcludedAssets();
		return assetsFor(request, excludedAssets, bundleNames);
	}

	public static BundleStorage getStorage() {
		initializeIfNeeded();
		return bundleStorage;
	}

	public static Set<Bundle> bundlesFor(HttpServletRequest request) {
		initializeIfNeeded();
		String[] bundles = AssetRequestContext.get(request).getBundles(true);
		Set<Bundle> retval = new LinkedHashSet<Bundle>();
		for (String bundleName : bundles) {
			List<String> bundlesToLoad = Assets.getStorage().getBundleDag().bundlesFor(bundleName);
			for (String s : bundlesToLoad) {
				Bundle b = Assets.getStorage().getBundleDag().getVertex(s);
				retval.add(b);
			}

		}

		return retval;
	}

	/**
	 * <p>
	 * Returns {@code true} if at least one asset needs to be display for the
	 * current request.
	 * <p>
	 * Note that asset locations are not processed in order to improve
	 * performance.
	 * 
	 * @param request
	 * @return
	 */
	public static boolean existsAssetsFor(HttpServletRequest request) {
		initializeIfNeeded();
		Set<Asset> assets = assetsFor(request, AssetRequestContext.get(request).getExcludedAssets(),
				AssetRequestContext.get(request).getBundles(false));
		return !assets.isEmpty();
	}

	/**
	 * Applies the {@link AssetProcessor} chain to the passed set of
	 * {@link Asset}s.
	 * 
	 * @param assetsToProcess
	 *            The set of {@link Asset} to process with the active
	 *            {@link AssetProcessor}.
	 * @param request
	 *            The current {@link HttpServletRequest}.
	 * @return a new set of processed {@link Asset}.
	 */
	private static Set<Asset> processedAssets(Set<Asset> assetsToProcess, HttpServletRequest request) {
		return AssetProcessorSystem.process(assetsToProcess, request);
	}

	private static Set<Asset> assetsFor(AssetDOMPosition position, String... bundleNames) {
		Set<Asset> assets = new LinkedHashSet<Asset>();

		for (String bundleName : bundleNames) {
			List<String> bundles = Assets.getStorage().getBundleDag().bundlesFor(bundleName);
			for (String s : bundles) {
				Bundle b = Assets.getStorage().getBundleDag().getVertex(s);
				if (b.getAssets() != null && !b.getAssets().isEmpty()) {
					for (Asset a : b.getAssets()) {
						AssetDOMPosition assetPosition = a.getDom() == null ? a.getType().getDefaultDom() : a.getDom();
						if (position.equals(assetPosition)) {
							assets.add(a);
						}
					}
				}
			}
		}

		return assets;
	}

	/**
	 * Prevents instantiation.
	 */
	private Assets() {
	}
}