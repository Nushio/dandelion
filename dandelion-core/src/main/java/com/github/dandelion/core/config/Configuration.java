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
package com.github.dandelion.core.config;

import java.util.Properties;

/**
 * <p>
 * Entry point for the whole Dandelion configuration.
 * 
 * <p>
 * The configuration is loaded only once using the configured instance of
 * {@link ConfigurationLoader}.
 * 
 * @author Thibault Duchateau
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public class Configuration {

	static Properties configuration;

	public static Properties getProperties() {
		if (configuration == null) {
			loadConfiguration();
		}
		return configuration;
	}

	public static String getProperty(String key) {
		return getProperties().getProperty(key);
	}

	public static String getProperty(String key, String defaultValue) {
		return getProperties().getProperty(key, defaultValue);
	}

	/**
	 * <p>
	 * Load the Dandelion configuration using the following strategy:
	 * <ul>
	 * <li>All default properties files are loaded (dandelion, webanalytics,
	 * ...)</li>
	 * <li>If it exists, the user properties are loaded using the bundle
	 * mechanism and override the default configuration</li>
	 * </ul>
	 */
	synchronized private static void loadConfiguration() {
		if (configuration == null) {

			ConfigurationLoader confLoader = DandelionConfigurator.getConfigurationLoader();
			Properties properties = new Properties();
			properties.putAll(confLoader.loadDefaultConfiguration());
			properties.putAll(confLoader.loadUserConfiguration());
			configuration = properties;
		}
	}
}
