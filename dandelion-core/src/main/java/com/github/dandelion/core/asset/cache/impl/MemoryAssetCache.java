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
package com.github.dandelion.core.asset.cache.impl;

import java.util.Map;
import java.util.Set;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.cache.spi.AbstractAssetCache;
import com.github.dandelion.core.asset.cache.spi.AssetCache;

/**
 * <p>
 * Service provider for {@link AssetCache} that uses {@link SimpleLruCache}s as
 * stores.
 * 
 * @author Thibault Duchateau
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public class MemoryAssetCache extends AbstractAssetCache {
	private Map<String, String> cache;
	private Map<String, Set<Asset>> assets;

	public MemoryAssetCache(Context context) {
		super(context);
		cache = new SimpleLruCache<String, String>(context.getConfiguration().getCacheAssetMaxSize());
		assets = new SimpleLruCache<String, Set<Asset>>(context.getConfiguration().getCacheRequestMaxSize());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCacheName() {
		return "default";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAssetContent(String cacheKey) {
		return cache.get(cacheKey);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<Asset> getRequestAssets(String cacheKey) {
		return assets.get(cacheKey);
	}

	/**
	 * {@inheritDoc}
	 */
	public void storeAssetContent(String cacheKey, String cacheContent) {
		cache.put(cacheKey, cacheContent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void storeRequestAssets(String cacheKey, Set<Asset> a) {
		assets.put(cacheKey, a);
	}

	/**
	 * {@inheritDoc}
	 */
	public void remove(String cacheKey) {
		cache.remove(cacheKey);
	}
}
