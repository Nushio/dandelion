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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.asset.processor.AssetProcessorSystem;
import com.github.dandelion.core.asset.wrapper.spi.AssetLocationWrapper;

/**
 * 
 * TODO
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 */
public class AssetStack {

	static AssetConfigurator assetConfigurator;
	static AssetStorage assetStorage;

	/**
	 * Initialize Assets only if needed
	 */
	static void initializeIfNeeded() {
		if (assetStorage == null) {
			initializeStorageIfNeeded();
		}
		if (assetConfigurator == null) {
			initializeConfiguratorIfNeeded();
		}
	}

	/**
	 * Initialize Assets Configurator only if needed
	 */
	synchronized private static void initializeConfiguratorIfNeeded() {
		if (assetConfigurator == null) {
			assetConfigurator = new AssetConfigurator(assetStorage);
			assetConfigurator.initialize();
		}
	}

	/**
	 * Initialize Assets Storage only if needed
	 */
	synchronized private static void initializeStorageIfNeeded() {
		if (assetStorage == null) {
			assetStorage = new AssetStorage();
		}
	}

	/**
	 * Check if the Asset Stack is empty
	 * 
	 * @return <code>true</code> if the stack is empty
	 */
	public static boolean isEmpty() {
		return assetStorage.containsAnyAsset();
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
		return assetConfigurator.assetsLocations;
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
		return assetConfigurator.assetsLocationWrappers;
	}

	/**
	 * Returns the implementation of {@link AssetLocationWrapper} corresponding
	 * to the given location key.
	 * 
	 * @return the location wrapper.
	 */
	public static AssetLocationWrapper getAssetLocationWrapper(String locationKey) {
		initializeIfNeeded();
		return assetConfigurator.assetsLocationWrappers.get(locationKey);
	}

	/**
	 * Prepare assets stored inside the given bundles for the rendering/
	 * 
	 * @param bundles
	 *            bundles of assets
	 * @param request
	 *            http request
	 * @return Prepared Assets of bundles
	 */
	public static List<Asset> prepareAssetsFor(HttpServletRequest request, String[] bundles, String[] excludeAssetsName) {
		return AssetProcessorSystem.process(excludeByName(assetsFor(bundles), excludeAssetsName), request);
	}

	/**
	 * Find Assets for bundles.
	 * 
	 * @param bundles
	 *            Bundle of assets
	 * @return Assets of bundles
	 */
	public static List<Asset> assetsFor(String... bundles) {
		initializeIfNeeded();
		return assetStorage.assetsFor(bundles);
	}

	/**
	 * Find Assets for bundles
	 * 
	 * @param bundles
	 *            bundles of assets
	 * @return Assets of bundles
	 */
	public static List<Asset> assetsFor(Collection<String> bundles) {
		initializeIfNeeded();
		return assetStorage.assetsFor(bundles.toArray(new String[] {}));
	}

	/**
	 * Check if any asset is contains in some bundles.
	 * 
	 * @param bundles
	 *            Bundles of assets.
	 * @param assetNameFilter
	 *            exclude assets names
	 * @return <code>true</code> if any asset is found
	 */
	public static boolean existsAssetsFor(String[] bundles, String[] assetNameFilter) {
		return !excludeByName(assetsFor(bundles), assetNameFilter).isEmpty();
	}

	/**
	 * @param assets
	 *            assets to filter
	 * @param filters
	 *            exclude assets names
	 * @return a filtered list of assets
	 */
	public static List<Asset> excludeByName(List<Asset> assets, String... filters) {
		List<Asset> _assets = new ArrayList<Asset>();
		List<String> _filters = new ArrayList<String>();
		for (String filter : filters) {
			_filters.add(filter.toLowerCase());
		}
		for (Asset _asset : assets) {
			if (!_filters.contains(_asset.getName().toLowerCase())
					&& !_filters.contains(_asset.getAssetKey().toLowerCase())) {
				_assets.add(_asset);
			}
		}
		return _assets;
	}

	/**
	 * @param assets
	 *            assets to filter
	 * @param filters
	 *            filtered assets dom position
	 * @return a filtered list of assets
	 */
	public static List<Asset> filterByDOMPosition(List<Asset> assets, AssetDOMPosition... filters) {
		List<Asset> _assets = new ArrayList<Asset>();
		List<AssetDOMPosition> _filters = new ArrayList<AssetDOMPosition>(Arrays.asList(filters));
		for (Asset _asset : assets) {
			AssetDOMPosition position = _asset.getDom() == null ? _asset.getType().getDefaultDom() : _asset.getDom();
			if (_filters.contains(position)) {
				_assets.add(_asset);
			}
		}
		return _assets;
	}

	/**
	 * @param assets
	 *            assets to filter
	 * @param filters
	 *            filtered assets type
	 * @return a filtered list of assets
	 */
	public static List<Asset> filterByType(List<Asset> assets, AssetType... filters) {
		List<Asset> _assets = new ArrayList<Asset>();
		List<AssetType> _filters = new ArrayList<AssetType>(Arrays.asList(filters));
		for (Asset _asset : assets) {
			if (_filters.contains(_asset.getType())) {
				_assets.add(_asset);
			}
		}
		return _assets;
	}
}