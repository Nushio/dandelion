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
package com.github.dandelion.core.asset.loader.impl;

import com.github.dandelion.core.asset.AssetsComponent;
import com.github.dandelion.core.asset.loader.spi.AssetsLoader;
import com.github.dandelion.core.utils.ResourceScanner;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Assets Loader for JSON definition
 */
public abstract class AbstractAssetsJsonLoader implements AssetsLoader {
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Load assets from '${folder}/*.json' files by Classpath Scanning.
	 */
	public List<AssetsComponent> loadAssets() {
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        List<AssetsComponent> assetsComponentList = new ArrayList<AssetsComponent>();
		try {
            Set<String> resources = ResourceScanner.getResources(getFolder(), null, ".json");
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			for (String resource : resources) {
                getLogger().debug("resources {}", resource);
				InputStream configFileStream = classLoader.getResourceAsStream(resource);

				AssetsComponent assetsComponent = mapper.readValue(configFileStream, AssetsComponent.class);

                getLogger().debug("found {}", assetsComponent);
                assetsComponentList.add(assetsComponent);
			}
		} catch (IOException e) {
            getLogger().error(e.getMessage(), e);
		}
        return assetsComponentList;
	}

    protected abstract Logger getLogger();

    public abstract String getFolder();

    @Override
    public String getType() {
        return "json";
    }
}
