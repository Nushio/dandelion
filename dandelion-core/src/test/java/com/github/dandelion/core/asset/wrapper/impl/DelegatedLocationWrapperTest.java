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

package com.github.dandelion.core.asset.wrapper.impl;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.web.AssetRequestContext;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.singletonMap;
import static org.fest.assertions.Assertions.assertThat;

public class DelegatedLocationWrapperTest {
    DelegatedLocationWrapper wrapper = new DelegatedLocationWrapper();

    @Test
    public void should_can_wrap_location_and_get_it() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/context/page.html");
        request.setContextPath("/context");

        AssetRequestContext.get(request).addParameter("asset-delegated", DelegatedLocationWrapper.DELEGATED_CONTENT_PARAM, new DelegatedContent() {
            @Override
            public String getContent(HttpServletRequest request) {
                return "/* content */";
            }
        });

        Asset asset = new Asset("asset-delegated", "1.0", AssetType.js, singletonMap(wrapper.locationKey(), "asset.js"));
        String location = wrapper.wrapLocation(asset, request);
        assertThat(location).isEqualTo("http://localhost:80/context/dandelion-assets/0cf3fbac07aa31f38153ba45eca0c943d627ba8b-asset-delegated.js");

        asset = new Asset("asset-delegated", "1.0", AssetType.js, singletonMap(wrapper.locationKey(), location));
        String content = wrapper.getWrappedContent(asset, request);
        assertThat(content).isEqualTo("/* content */");
    }
}
