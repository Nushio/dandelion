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

package com.github.dandelion.core.asset.processor.impl;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetStack;
import com.github.dandelion.core.asset.cache.AssetsCacheSystem;
import com.github.dandelion.core.asset.processor.spi.AssetProcessorEntry;
import com.github.dandelion.core.asset.wrapper.spi.AssetsLocationWrapper;
import com.github.dandelion.core.config.Configuration;
import com.github.dandelion.core.DevMode;
import com.github.dandelion.core.utils.RequestUtils;
import com.github.dandelion.core.utils.ResourceUtils;
import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

import static com.github.dandelion.core.asset.web.AssetsServlet.DANDELION_ASSETS_URL;

public class AssetCompressionProcessorEntry extends AssetProcessorEntry {
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(AssetAggregationProcessorEntry.class);

    public static final String COMPRESSION = "compression";
    public static final String COMPRESSION_ENABLED_KEY = "dandelion.compression.enabled";
    public static final String COMPRESSION_JS_MUNGE = "dandelion.compression.js.munge";
    public static final String COMPRESSION_JS_PRESERVE_SEMICOLONS = "dandelion.compression.js.preserveSemiColons";
    public static final String COMPRESSION_JS_DISABLE_OPTIMIZATIONS = "dandelion.compression.js.disableOptimizations";

    private boolean compressionEnabled = true;
    private boolean jsMunge = true;
    private boolean jsPreserveSemiColons = true;
    private boolean jsDisableOptimizations = true;

    public AssetCompressionProcessorEntry() {
        this.compressionEnabled = Boolean.TRUE.toString().equals(
                Configuration.getProperty(COMPRESSION_ENABLED_KEY, Boolean.toString(compressionEnabled)));
        this.jsMunge = Boolean.TRUE.toString().equals(
                Configuration.getProperty(COMPRESSION_JS_MUNGE, Boolean.toString(compressionEnabled)));
        this.jsPreserveSemiColons = Boolean.TRUE.toString().equals(
                Configuration.getProperty(COMPRESSION_JS_PRESERVE_SEMICOLONS, Boolean.toString(compressionEnabled)));
        this.jsDisableOptimizations = Boolean.TRUE.toString().equals(
                Configuration.getProperty(COMPRESSION_JS_DISABLE_OPTIMIZATIONS, Boolean.toString(compressionEnabled)));

        if(DevMode.isDevModeEnabled()) {
            this.compressionEnabled = false;
        }

        LOG.info("Dandelion Asset Compression is {}", compressionEnabled?"enabled":"disabled");
        if(compressionEnabled) {
            LOG.debug("Dandelion Asset Compression JS munge is {}", jsMunge?"enabled":"disabled");
            LOG.debug("Dandelion Asset Compression JS preserve semicolons is {}", jsPreserveSemiColons?"enabled":"disabled");
            LOG.debug("Dandelion Asset Compression JS disable optimizations is {}", jsDisableOptimizations ? "enabled" : "disabled");
        }
    }

    @Override
    public String getTreatmentKey() {
        return COMPRESSION;
    }

    @Override
    public int getRank() {
        return 2000;
    }

    @Override
    public List<Asset> process(List<Asset> assets, HttpServletRequest request) {
        if(!compressionEnabled) {
            return assets;
        }

        String context = RequestUtils.getCurrentUrl(request, true);
        context = context.replaceAll("\\?", "_").replaceAll("&", "_");

        String baseUrl = RequestUtils.getBaseUrl(request);
        List<Asset> compressedAssets = new ArrayList<Asset>();
        for(Asset asset:assets) {
            for(String location:asset.getLocations().values()) {
                String cacheKey = AssetsCacheSystem.generateCacheKey(context, COMPRESSION, location, COMPRESSION, asset.getType());

                if (!AssetsCacheSystem.checkCacheKey(cacheKey)) {
                    LOG.debug("cache assets compression for asset {}", asset.getAssetKey());
                    cacheCompressedContent(request, context, location, asset, cacheKey);
                }

                String accessLocation = baseUrl + DANDELION_ASSETS_URL + cacheKey;

                Map<String, String> locations = new HashMap<String, String>();
                locations.put(COMPRESSION, accessLocation);

                compressedAssets.add(new Asset(cacheKey, COMPRESSION, asset.getType(), locations));
                LOG.debug("create a new asset with name {}, version {}, type {}, locations [{}={}]", cacheKey, COMPRESSION, asset.getType(), COMPRESSION, accessLocation);
            }
        }
        return compressedAssets;
    }

    private void cacheCompressedContent(HttpServletRequest request, String context, String location, Asset asset, String cacheKey) {
        String content = compress(asset, request);
        AssetsCacheSystem.storeCacheContent(context, COMPRESSION, location, COMPRESSION, asset.getType(), content);
    }

    private String compress(Asset asset, HttpServletRequest request) {
        String content = extractContent(asset, request);
        switch (asset.getType()) {
            case css:
                LOG.debug("CSS compression for asset {}", asset.getAssetKey());
                return compressCss(asset.getAssetKey(), content);
            case js:
                LOG.debug("JS compression for asset {}", asset.getAssetKey());
                return compressJs(asset.getAssetKey(), content);
            default:
                LOG.debug("No compression for asset {}", asset.getAssetKey());
                return content;
        }
    }

    private String compressJs(String assetKey, String content) {
        LOG.debug("JS compression with YUI compressor");
        Writer output = new StringWriter();

        try {
            JavaScriptCompressor compressor = new JavaScriptCompressor(new StringReader(content), new YuiCompressorErrorReporter());
            compressor.compress(output, -1, jsMunge, false, jsPreserveSemiColons, jsDisableOptimizations);
        } catch (EvaluatorException e) {
            LOG.error("YUI compressor can't evaluate the content of {}", assetKey);
            LOG.debug("YUI compressor can't evaluate the content [{}]", content);
            throw DandelionException.wrap(e, null)
                    .set("assetKey", assetKey).set("content", content);
        } catch (IOException e) {
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
        } catch (IOException e) {
            LOG.error("YUI compressor can't access to the content of {}", assetKey);
            throw DandelionException.wrap(e, null).set("assetKey", assetKey);
        }

        return output.toString();
    }

    private String extractContent(Asset asset, HttpServletRequest request) {
        Map<String, AssetsLocationWrapper> wrappers = AssetStack.getAssetsLocationWrappers();
        StringBuilder groupContent = new StringBuilder();

        for (Map.Entry<String, String> location : asset.getLocations().entrySet()) {
            AssetsLocationWrapper wrapper = wrappers.get(location.getKey());
            List<String> contents;
            if (wrapper == null) {
                contents = Arrays.asList(ResourceUtils.getContentFromUrl(location.getValue(), true));
            } else {
                contents = wrapper.getContents(asset, request);
            }
            for (String content : contents) {
                groupContent.append(content).append("\n");
            }
        }
        return groupContent.toString();
    }
}
