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
package com.github.dandelion.core.asset.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DevMode;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.cache.impl.HashMapAssetCache;
import com.github.dandelion.core.asset.cache.spi.AssetCache;
import com.github.dandelion.core.config.Configuration;
import com.github.dandelion.core.utils.Sha1Utils;

/**
 * <p>
 * System in charge of discovering all implementations of {@link AssetCache}
 * available in the classpath and manipulating the configured {@link AssetCache}.
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public final class AssetCacheSystem {

	private static final Logger LOG = LoggerFactory.getLogger(AssetCacheSystem.class);

	private static ServiceLoader<AssetCache> assetCacheServiceLoader = ServiceLoader.load(AssetCache.class);
	private static AssetCache assetCache;

	private static void initializeIfNeeded() {
		if (assetCache == null || DevMode.isEnabled()) {
			initializeAssetsCache();
		}
	}

	private static synchronized void initializeAssetsCache() {

		Map<String, AssetCache> caches = new HashMap<String, AssetCache>();
		for (AssetCache ac : assetCacheServiceLoader) {
			caches.put(ac.getCacheName(), ac);
			LOG.info("Asset cache found: {}", ac.getClass().getSimpleName());
		}

		String cacheName = Configuration.get("asset.cache.strategy");
		if (!caches.isEmpty()) {
			if (caches.containsKey(cacheName)) {
				assetCache = caches.get(cacheName);
			}
			else if (cacheName == null && caches.size() == 1) {
				assetCache = caches.values().iterator().next();
			}
			else {
				LOG.warn("Asset Cache Strategy is set with {}, but only caches with names {} have been found.",
						cacheName, caches.keySet());
			}
		}
		else if (cacheName != null) {
			LOG.warn("Asset Cache Strategy is set with {}, but we don't find any cache", cacheName);
		}

		if (assetCache == null) {
			assetCache = new HashMapAssetCache();
		}
		LOG.info("Selected asset cache system: {}", assetCache.getCacheName());
	}

	public static String generateCacheKey(String context, String location, String assetName, AssetType assetType) {
		StringBuilder key = new StringBuilder(Sha1Utils.generateSha1(context + "|" + location, true));
		key.append("-");
		key.append(assetName);
		key.append(".");
		key.append(assetType.name());
		LOG.debug("SHA1 key {} generated from context {}, location {}, asset name {}, asset type {}.", key.toString(),
				context, location, assetName, assetType);
		return key.toString();
	}

	public static String getContent(String cacheKey) {
		initializeIfNeeded();
		LOG.debug("Retrieving asset with the key {}", cacheKey);
		return assetCache.getContent(cacheKey);
	}

	public static String storeContent(String context, String location, String resourceName, AssetType type,
			String content) {
		initializeIfNeeded();
		String generatedKey = generateCacheKey(context, location, resourceName, type);
		LOG.debug("Storing asset under the key {}", generatedKey);
		assetCache.storeContent(generatedKey, content);
		return content;
	}

	public static String storeContent(String key, String content) {
		initializeIfNeeded();
		LOG.debug("Storing asset under the key {}", key);
		assetCache.storeContent(key, content);
		return content;
	}

	public static void remove(String key) {
		initializeIfNeeded();
		LOG.debug("Removing asset under the key {}", key);
		assetCache.remove(key);
	}

	public static String getCacheName() {
		initializeIfNeeded();
		return assetCache.getCacheName();
	}

	/**
	 * Prevents instantiation.
	 */
	private AssetCacheSystem() {
	}
}
