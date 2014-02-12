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

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.util.Collections.list;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fest.assertions.MapAssert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.DandelionExceptionMatcher;

public class AssetStorageTest {
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	AssetStorage assetStorage;

	static Map<String, String> locations;
	static Asset asset, asset2, asset3, asset4, assetConflict;

	@BeforeClass
	public static void set_up_class() {
		locations = new HashMap<String, String>();
		locations.put("remote", "remoteURL");
		locations.put("local", "localPath");

		asset = new Asset("name", "version", AssetType.js, locations);
		asset2 = new Asset("name2", "version", AssetType.js, locations);
		asset3 = new Asset("name3", "version", AssetType.js, locations);
		asset4 = new Asset("name4", "version", AssetType.css, locations);
		assetConflict = new Asset("name", "versionConflict", AssetType.js, locations);
	}

	@Before
	public void set_up() {
		assetStorage = new AssetStorage();
	}

	@Before
	public void tear_down() {
		asset.storagePosition = -1;
		asset2.storagePosition = -1;
		asset3.storagePosition = -1;
		asset4.storagePosition = -1;
		assetConflict.storagePosition = -1;
	}

	@Test
	public void should_not_store_invalid_asset() {
		assetStorage.store(new Asset());

		assertThat(assetStorage.assetsFor()).hasSize(0);
	}

	@Test
	public void should_store_asset_in_default_bundle() {
		assetStorage.store(asset);

		assertThat(assetStorage.assetsFor("default")).hasSize(1).contains(asset);
	}

	@Test
	public void should_store_assets_in_default_bundle() {
		assetStorage.store(asset);
		assetStorage.store(asset2);

		assertThat(assetStorage.assetsFor("default")).hasSize(2).contains(asset, asset2);
	}

	@Test
	public void should_access_to_assets_without_any_bundle() {
		assetStorage.store(asset);
		assetStorage.store(asset2);

		assertThat(assetStorage.assetsFor()).hasSize(2);
	}

	@Test
	public void should_store_asset_in_another_bundle() {
		assetStorage.store(asset);
		assetStorage.store(asset2);
		assetStorage.store(asset3, "another");

		assertThat(assetStorage.assetsFor("another")).hasSize(3).contains(asset, asset2, asset3);
	}

	@Test
	public void should_store_assets_in_another_level_bundle() {
		assetStorage.store(asset);
		assetStorage.store(asset2);
		assetStorage.store(asset3, "another");
		assetStorage.store(asset4, "another_level", "another");

		assertThat(assetStorage.assetsFor("another_level")).hasSize(4).contains(asset, asset2, asset3, asset4);
	}

	@Test
	public void should_not_store_assets_with_same_bundle_but_not_parent_bundles() {
		expectedEx.expect(DandelionException.class);
		expectedEx.expect(new DandelionExceptionMatcher(AssetStorageError.PARENT_BUNDLE_INCOMPATIBILITY).set("bundle",
                "same_bundle").set("parentBundle", "parent_bundle"));

		assetStorage.store(asset, "parent_bundle");
		assetStorage.store(asset2, "another_parent_bundle");
		assetStorage.store(asset3, "same_bundle", "parent_bundle");
		assetStorage.store(asset4, "same_bundle", "another_parent_bundle");
	}

	@Test
	public void should_not_store_asset_with_unknown_parent_bundle() {
		expectedEx.expect(DandelionException.class);
		expectedEx.expect(new DandelionExceptionMatcher(AssetStorageError.UNDEFINED_PARENT_BUNDLE).set("parentBundle",
                "unknown_parent_bundle"));

		assetStorage.store(asset, "bundle", "unknown_parent_bundle");
	}

	@Test
	public void should_manage_assets_with_different_types() {
		Asset assetDifferentType = new Asset("name", "version", AssetType.css, locations);
		assetStorage.store(asset);
		assetStorage.store(assetDifferentType, "differentTypes");
		assertThat(assetStorage.assetsFor("differentTypes")).hasSize(2).contains(assetDifferentType);
	}

	@Test
	public void should_store_empty_bundle_by_workaround() {
		assetStorage.store(asset);
		assetStorage.setupEmptyParentBundle("empty_bundle");
		assetStorage.store(asset2, "not_empty_bundle", "empty_bundle");
		assertThat(assetStorage.assetsFor("not_empty_bundle")).hasSize(2).contains(asset, asset2);
	}

	@Test
	public void should_manage_override_assets() {
		Asset assetOverride = new Asset("name", "version2", AssetType.js, locations);
		assetStorage.store(asset);
		assetStorage.store(assetOverride, "override");
		assertThat(assetStorage.assetsFor("override")).hasSize(1).contains(assetOverride);
	}

	@Test
	public void should_detect_conflicts_before_storage() {
		expectedEx.expect(DandelionException.class);
		expectedEx.expect(new DandelionExceptionMatcher(AssetStorageError.ASSET_ALREADY_EXISTS_IN_BUNDLE).set(
                "originalAsset", asset));

		assetStorage.store(asset);
		assetStorage.store(assetConflict);
	}

	@Test
	public void should_manage_conflicts_on_demand() {
		assetStorage.store(asset2);
		assetStorage.store(asset, "bundle");
		assetStorage.store(assetConflict, "another_bundle");

		assertThat(assetStorage.assetsFor("bundle", "another_bundle")).hasSize(2).contains(asset, asset2);
	}

	@Test
	public void should_manage_priorities() {
		Asset assetPriority = asset.clone(false);
		assetPriority.setVersion("versionAssetPriority");

		assetStorage.store(asset);
		assetStorage.store(asset4);
		assetStorage.store(asset2);
		assetStorage.store(asset3);
		assetStorage.store(assetPriority, "bundle");

		assertThat(assetStorage.assetsFor("bundle")).hasSize(4).containsSequence(assetPriority, asset4, asset2, asset3);
	}

	@Test
	public void should_manage_detached_bundle() {
		Asset assetWithDetachedBundle = new Asset("detached", "version", AssetType.js, locations);

		assetStorage.store(asset);
		assetStorage.store(assetWithDetachedBundle, "bundle", "none");

		assertThat(assetStorage.assetsFor("bundle")).hasSize(1).contains(assetWithDetachedBundle);
		assertThat(assetStorage.assetsFor("default", "bundle")).hasSize(2).contains(assetWithDetachedBundle, asset);
	}

	@Test
	public void should_detach_bundle_not_override_other_assets() {
		Asset assetWithDetachedBundle = new Asset("name", "version", AssetType.js, locations);

		assetStorage.store(asset);
		assetStorage.store(assetWithDetachedBundle, "bundle", "none");

		assertThat(assetStorage.assetsFor("bundle")).hasSize(1).contains(assetWithDetachedBundle);
		assertThat(assetStorage.assetsFor("default", "bundle")).hasSize(2).contains(assetWithDetachedBundle, asset);
	}

	@Test
	public void should_not_allow_the_usage_of_detached_bundle_as_a_bundle() {
		expectedEx.expect(DandelionException.class);
		expectedEx.expect(new DandelionExceptionMatcher(AssetStorageError.DETACHED_BUNDLE_NOT_ALLOWED).set(
                "detachedBundle", "none"));

		assetStorage.store(asset, "none");
	}

	@Test
	public void should_merge_same_assets_with_distincts_locations() {
		assetStorage.store(asset);
		Map<String, String> assetCloneLocations = new HashMap<String, String>();
		assetCloneLocations.put("other", "otherURL");
		Asset assetClone = new Asset(asset.getName(), asset.getVersion(), asset.getType(), assetCloneLocations);
		assetStorage.store(assetClone);

		List<Asset> assets = assetStorage.assetsFor();
		assertThat(assets).hasSize(1).contains(asset);
		assertThat(assets.get(0).getLocations()).includes(MapAssert.entry("remote", "remoteURL"),
				MapAssert.entry("local", "localPath"), MapAssert.entry("other", "otherURL"));
	}

	@Test
	public void should_detect_conflicts_in_locations_before_storage() {
		Map<String, String> assetCloneLocations = new HashMap<String, String>();
		assetCloneLocations.put("remote", "otherURL");
		Asset assetClone = new Asset(asset.getName(), asset.getVersion(), asset.getType(), assetCloneLocations);

		expectedEx.expect(DandelionException.class);
		expectedEx.expect(new DandelionExceptionMatcher(AssetStorageError.ASSET_LOCATION_ALREADY_EXISTS_IN_BUNDLE).set(
                "locations", list("remote")).set("asset", assetClone));

		assetStorage.store(asset);
		assetStorage.store(assetClone);
	}

	@Test
	public void should_manage_tree_on_locations() {
		assetStorage.store(asset);
		Map<String, String> assetCloneLocations = new HashMap<String, String>();
		assetCloneLocations.put("other", "otherURL");
		Asset assetClone = new Asset(asset.getName(), asset.getVersion(), asset.getType(), assetCloneLocations);
		assetStorage.store(assetClone, "other");

		List<Asset> assets = assetStorage.assetsFor("other");
		assertThat(assets).hasSize(1).contains(assetClone);
		assertThat(assets.get(0).getLocations()).includes(MapAssert.entry("remote", "remoteURL"),
				MapAssert.entry("local", "localPath"), MapAssert.entry("other", "otherURL"));
	}
}
