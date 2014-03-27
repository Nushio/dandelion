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
package com.github.dandelion.fakedomain;

import static org.fest.util.Collections.list;
import static org.fest.util.Collections.set;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.bundle.loader.spi.AbstractBundleLoader;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorageUnit;

public class AssetFakeLoader extends AbstractBundleLoader {
	
	@Override
	public List<BundleStorageUnit> loadBundles() {
		Map<String, String> locations = new HashMap<String, String>();
		locations.put("remote", "remoteURL");
		locations.put("local", "localPath");
		Map<String, String> locations2 = new HashMap<String, String>();
		locations.put("remote", "remoteURL2");
		locations.put("local", "localPath2");

		return list(
				new BundleStorageUnit("default", new HashSet<AssetStorageUnit>()),
				new BundleStorageUnit("fake", set(new AssetStorageUnit("name", "version", AssetType.js, locations),
						new AssetStorageUnit("name2", "version2", AssetType.js, locations2))));
	}

	@Override
	public String getName() {
		return "fake";
	}

	@Override
	public boolean isRecursive() {
		return true;
	}

	@Override
	protected Logger getLogger() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}
}
