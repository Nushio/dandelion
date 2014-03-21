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

import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DevMode;
import com.github.dandelion.core.asset.web.AssetRequestContext;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorage;
import com.github.dandelion.core.storage.BundleStorageUnit;
import com.github.dandelion.core.utils.UrlUtils;

/**
 * <p>Allows to build and execute a query against the {@link BundleStorage}.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class AssetQuery {

	private Set<Asset> requestedAssets;
	
	public AssetQuery(HttpServletRequest request, Context context) {
	
		String key = UrlUtils.getCurrentUri(request).toString();
		this.requestedAssets = context.getCacheManager().getAssets(key);
		
		if (this.requestedAssets == null || DevMode.isEnabled()) {

			// Gathers all asset storage units in an ordered set
			Set<AssetStorageUnit> assetStorageUnits = new LinkedHashSet<AssetStorageUnit>();

			String[] bundleNames = AssetRequestContext.get(request).getBundles(false);
			for (BundleStorageUnit bsu : context.getBundleStorage().bundlesFor(bundleNames)) {
				assetStorageUnits.addAll(bsu.getAssetStorageUnits());
			}

			// Convert all asset storage units into assets
			AssetMapper assetMapper = new AssetMapper(request, context);
			Set<Asset> mappedAssets = assetMapper.mapToAssets(assetStorageUnits);

			// Applying the active processors
			mappedAssets = context.getProcessorManager().process(mappedAssets, request);
			
			this.requestedAssets = context.getCacheManager().storeAssets(key, mappedAssets);
		}
	}
	
	public AssetQuery withPosition(AssetDomPosition desiredPosition){
		this.requestedAssets = AssetUtils.filtersByDomPosition(this.requestedAssets, desiredPosition);
		return this;
	}
	
	public AssetQuery withType(AssetType desiredType){
		// TODO
		return this;
	}
	
	public AssetQuery excludeAssets(String[] excludedAssetNames){
		this.requestedAssets = AssetUtils.filtersByName(this.requestedAssets, excludedAssetNames);
		return this;
	}
	
	public AssetQuery excludeBundles(String[] excludedBundleNames){
		// TODO 
		return this;
	}
	
	public Set<Asset> perform(){
		return this.requestedAssets;
	}
}
