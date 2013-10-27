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
package com.github.dandelion.core.html;

/**
 * Plain old HTML <code>script</code> tag.
 * 
 * @author Thibault Duchateau
 */
public class ScriptTag extends HtmlTag {

	/**
	 * Plain old HTML <code>src</code> attribute.
	 */
	private String src;
    private boolean async = false;
    private boolean deferred = false;
	
	public ScriptTag(){
	}
	
	public ScriptTag(String src) {
		this.src = src;
	}

    public ScriptTag(String src, boolean async, boolean deferred) {
        this.src = src;
        this.async = async;
        this.deferred = deferred;
    }

    /**
	 * {@inheritDoc}
	 */
	@Override
	public String toHtml(){
		StringBuffer html = new StringBuffer();
		html.append("<script");
		
		if(this.src != null){
			html.append(" src=\"");
			html.append(this.src);
			html.append("\"");
            if(async) html.append(" async");
            if(deferred) html.append(" defer");
		}

        html.append(attributesToHtml());
		
		html.append("></script>");
		
		return html.toString();
	}
	
	public String getSrc() {
		return src;
	}
	public void setSrc(String src) {
		this.src = src;
	}
    public boolean isAsync() {
        return async;
    }
    public void setAsync(boolean async) {
        this.async = async;
    }
    public boolean isDeferred() {
        return deferred;
    }
    public void setDeferred(boolean deferred) {
        this.deferred = deferred;
    }
}