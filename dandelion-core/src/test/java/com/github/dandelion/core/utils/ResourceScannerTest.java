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
package com.github.dandelion.core.utils;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

public class ResourceScannerTest {

	@Test
	public void should_scan_all_resources_recursively() throws IOException {
		assertThat(ResourceScanner.findResourcePaths("scanning", null, null, null, true)).hasSize(8);
	}

	@Test
	public void should_return_an_empty_set() throws IOException {
		assertThat(ResourceScanner.findResourcePaths("unknown-folder", null, null, null, true)).isEmpty();
	}

	@Test
	public void should_return_only_one_resource_filtered_by_name() throws IOException {
		assertThat(ResourceScanner.findResourcePath("scanning", "resource5.properties")).isEqualTo(
				"scanning/resource5.properties");
	}

	@Test
	public void should_filter_resources_by_suffix_non_recursively() throws IOException {
		assertThat(ResourceScanner.findResourcePaths("scanning", null, null, ".json", false)).hasSize(4);
		assertThat(ResourceScanner.findResourcePaths("scanning", null, null, ".properties", false)).hasSize(1);
	}

	@Test
	public void should_filter_resources_by_suffix_recursively() throws IOException {
		assertThat(ResourceScanner.findResourcePaths("scanning", null, null, ".json", true)).hasSize(6);
		assertThat(ResourceScanner.findResourcePaths("scanning", null, null, ".properties", true)).hasSize(2);
	}

	@Test
	public void should_filter_resources_by_prefix_non_recursively() throws IOException {
		assertThat(ResourceScanner.findResourcePaths("scanning", null, "resource1", null, false)).hasSize(1);
	}

	@Test
	public void should_filter_resources_by_prefix_recursively() throws IOException {
		assertThat(ResourceScanner.findResourcePaths("scanning", null, "resource1", null, true)).hasSize(2);
	}

	@Test
	public void should_filter_resources_by_name_non_recursively() throws IOException {
		assertThat(ResourceScanner.findResourcePaths("scanning", null, "resource5.properties", false)).hasSize(1);
	}

	@Test
	public void should_filter_resources_by_name_recursively() throws IOException {
		assertThat(ResourceScanner.findResourcePaths("scanning", null, "resource5.properties", true)).hasSize(2);
	}

	@Test
	public void should_filter_resources_with_excluded_folder() throws IOException {
		assertThat(ResourceScanner.findResourcePaths("scanning", Arrays.asList("scanning/subfolder"), null, null, true))
				.hasSize(5);
	}

	@Test
	public void should_filter_resources_by_suffix_without_recursivity() throws IOException {
		assertThat(ResourceScanner.findResourcePaths("scanning", null, null, ".json", false)).hasSize(4);
	}
}
