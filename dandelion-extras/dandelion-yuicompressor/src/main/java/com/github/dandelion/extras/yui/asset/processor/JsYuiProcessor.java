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
package com.github.dandelion.extras.yui.asset.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.processor.CompatibleAssetType;
import com.github.dandelion.core.asset.processor.spi.AbstractAssetProcessor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * <p>
 * JS processor based on YUI compressor.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
@CompatibleAssetType(types = AssetType.js)
public class JsYuiProcessor extends AbstractAssetProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(JsYuiProcessor.class);

	@Override
	public String getProcessorKey() {
		return "js-yui";
	}

	@Override
	protected void doProcess(Asset asset, Reader reader, Writer writer) throws Exception {

		try {
			JavaScriptCompressor compressor = new JavaScriptCompressor(reader, new YuiCompressorErrorReporter());
			compressor.compress(writer, -1, true, false, true, true);
		}
		catch (EvaluatorException e) {
			LOG.error("YUI compressor can't evaluate the content of {}", asset.toLog());
			throw DandelionException.wrap(e);
		}
		catch (IOException e) {
			LOG.error("YUI compressor can't access to the content of {}", asset.toLog());
			throw DandelionException.wrap(e);
		}
	}
}
