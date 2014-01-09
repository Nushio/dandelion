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
package com.github.dandelion.core.asset;

import com.github.dandelion.core.DandelionError;

/**
 * Possible Errors for 'Assets Storage'
 */
public enum AssetsStorageError implements DandelionError {
    /**
     * An asset can't be added twice in the same scope (same name but different versions)
     */
    ASSET_ALREADY_EXISTS_IN_SCOPE(100),
    /**
     * An asset can't be added with a 'Detached Scope',
     * 'Detached Scope' is only allowed as a Parent Scope
     */
    DETACHED_SCOPE_NOT_ALLOWED(101),
    /**
     * An asset can't have a couple of Scope/Parent Scope
     * when its scope is already associated to another parent scope
     */
    PARENT_SCOPE_INCOMPATIBILITY(102),
    /**
     * An asset can't have a parent scope that doesn't already exist
     */
    UNDEFINED_PARENT_SCOPE(103),
    /**
     * A location can't be used twice in the same scope by a similar asset
     */
    ASSET_LOCATION_ALREADY_EXISTS_IN_SCOPE(104),
    /**
     * A attribute can't be used twice in the same scope by a similar asset
     */
    ASSET_ATTRIBUTE_ALREADY_EXISTS_IN_SCOPE(105),
    /**
     * A DOM position must be equals for merging two assets with same name
     */
    ASSET_DOM_POSITION_ALREADY_EXISTS_IN_SCOPE(106);

    private final int number;

    private AssetsStorageError(int number) {
        this.number = number;
    }

    @Override
    public int getNumber() {
        return number;
    }

}
