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
package com.github.dandelion.core.asset.loader.spi;

import java.util.List;

import com.github.dandelion.core.asset.AssetsComponent;
import com.github.dandelion.core.asset.loader.AssetsLoaderSystem;

/**
 * Interface that all assets loader should implement.
 */
public interface AssetsLoader {

	/**
	 * <p>
	 * Load assets by scanning the classpath starting from the configured
	 * folder.
	 * 
	 * @return a list of {@link AssetsComponent}.
	 */
	List<AssetsComponent> loadAssets();

	/**
	 * TODO aujourd'hui uniquement utilisé pour loguer (cf
	 * {@link AssetsLoaderSystem}).
	 * 
	 * @return
	 */
	String getType();

	/**
	 * <p>
	 * Indicates whether the asset loader must scan for its resources
	 * recursively inside the configured folder or not.
	 * 
	 * @return {@code true} if the scanning is recursive, otherwise
	 *         {@code false}
	 */
	boolean isRecursive();
}
