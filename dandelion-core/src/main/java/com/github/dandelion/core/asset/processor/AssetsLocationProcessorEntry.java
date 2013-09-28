/*
 * [The "BSD licence"]
 * Copyright (c) 2013 Dandelion
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
package com.github.dandelion.core.asset.processor;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetsStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class AssetsLocationProcessorEntry extends AssetsProcessorEntry {
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(AssetsLocationProcessorEntry.class);

    @Override
    public List<Asset> process(List<Asset> assets, HttpServletRequest request) {
        List<Asset> _assets = new ArrayList<Asset>();

        for(Asset asset:assets) {
            // no available locations = no locations
            if(asset.getLocations().isEmpty()) {
                LOG.warn("no available locations for {}.", asset.toString());
                continue;
            }

            String locationKey = null;
            if(asset.getLocations().size() == 1) {
                // use the unique location if needed
                LOG.debug("only one location {}, automatically used.", asset.toString());
                for(String _locationKey:asset.getLocations().keySet()) {
                    locationKey = _locationKey;
                }
            } else {
                // otherwise search for the first match in authorized locations
                LOG.debug("search the right location for {}.", asset.toString());
                for(String searchedLocationKey: AssetsStack.getAssetsLocations()) {
                    if(asset.getLocations().containsKey(searchedLocationKey)) {
                        String location = asset.getLocations().get(searchedLocationKey);
                        if(location != null && !location.isEmpty()) {
                            locationKey = searchedLocationKey;
                            break;
                        }
                    }
                }

            }

            // And if any location was found = no locations
            if(locationKey == null) {
                LOG.warn("any location match the asked locations {} for {}.", AssetsStack.getAssetsLocations(), asset.toString());
                continue;
            }

            // Otherwise check for wrapper
            if(AssetsStack.getAssetsLocationWrappers().containsKey(locationKey)) {
                LOG.debug("use location wrapper for {} on {}.", locationKey, asset);
                List<String> wrappedUrls = AssetsStack.getAssetsLocationWrappers().get(locationKey).wrapLocations(asset, request);
                for(String wrapperUrl:wrappedUrls) {
                    Asset wrappedAsset = asset.clone(true);
                    wrappedAsset.getLocations().put(locationKey, wrapperUrl);
                    _assets.add(wrappedAsset);
                }
            } else {
                Asset wrappedAsset = asset.clone(true);
                wrappedAsset.getLocations().put(locationKey, asset.getLocations().get(locationKey));
                _assets.add(wrappedAsset);
            }

        }
        return _assets;
    }
}
