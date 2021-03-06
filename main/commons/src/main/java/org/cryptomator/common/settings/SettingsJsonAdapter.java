/*******************************************************************************
 * Copyright (c) 2017 Skymatic UG (haftungsbeschränkt).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the accompanying LICENSE file.
 *******************************************************************************/
package org.cryptomator.common.settings;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SettingsJsonAdapter extends TypeAdapter<Settings> {

	private static final Logger LOG = LoggerFactory.getLogger(SettingsJsonAdapter.class);

	private final VaultSettingsJsonAdapter vaultSettingsJsonAdapter = new VaultSettingsJsonAdapter();

	@Override
	public void write(JsonWriter out, Settings value) throws IOException {
		out.beginObject();
		out.name("directories");
		writeVaultSettingsArray(out, value.getDirectories());
		out.name("askedForUpdateCheck").value(value.askedForUpdateCheck().get());
		out.name("checkForUpdatesEnabled").value(value.checkForUpdates().get());
		out.name("startHidden").value(value.startHidden().get());
		out.name("port").value(value.port().get());
		out.name("numTrayNotifications").value(value.numTrayNotifications().get());
		out.name("preferredGvfsScheme").value(value.preferredGvfsScheme().get().name());
		out.name("debugMode").value(value.debugMode().get());
		out.name("preferredVolumeImpl").value(value.preferredVolumeImpl().get().name());
		out.name("theme").value(value.theme().get().name());
		out.endObject();
	}

	private void writeVaultSettingsArray(JsonWriter out, Iterable<VaultSettings> vaultSettings) throws IOException {
		out.beginArray();
		for (VaultSettings value : vaultSettings) {
			vaultSettingsJsonAdapter.write(out, value);
		}
		out.endArray();
	}

	@Override
	public Settings read(JsonReader in) throws IOException {
		Settings settings = new Settings();

		in.beginObject();
		while (in.hasNext()) {
			String name = in.nextName();
			switch (name) {
				case "directories":
					settings.getDirectories().addAll(readVaultSettingsArray(in));
					break;
				case "askedForUpdateCheck":
					settings.askedForUpdateCheck().set(in.nextBoolean());
					break;
				case "checkForUpdatesEnabled":
					settings.checkForUpdates().set(in.nextBoolean());
					break;
				case "startHidden":
					settings.startHidden().set(in.nextBoolean());
					break;
				case "port":
					settings.port().set(in.nextInt());
					break;
				case "numTrayNotifications":
					settings.numTrayNotifications().set(in.nextInt());
					break;
				case "preferredGvfsScheme":
					settings.preferredGvfsScheme().set(parseWebDavUrlSchemePrefix(in.nextString()));
					break;
				case "debugMode":
					settings.debugMode().set(in.nextBoolean());
					break;
				case "preferredVolumeImpl":
					settings.preferredVolumeImpl().set(parsePreferredVolumeImplName(in.nextString()));
					break;
				case "theme":
					settings.theme().set(parseUiTheme(in.nextString()));
					break;
				default:
					LOG.warn("Unsupported vault setting found in JSON: " + name);
					in.skipValue();
					break;
			}
		}
		in.endObject();

		return settings;
	}

	private VolumeImpl parsePreferredVolumeImplName(String nioAdapterName) {
		try {
			return VolumeImpl.valueOf(nioAdapterName.toUpperCase());
		} catch (IllegalArgumentException e) {
			LOG.warn("Invalid volume type {}. Defaulting to {}.", nioAdapterName, Settings.DEFAULT_PREFERRED_VOLUME_IMPL);
			return Settings.DEFAULT_PREFERRED_VOLUME_IMPL;
		}
	}

	private WebDavUrlScheme parseWebDavUrlSchemePrefix(String webDavUrlSchemeName) {
		try {
			return WebDavUrlScheme.valueOf(webDavUrlSchemeName.toUpperCase());
		} catch (IllegalArgumentException e) {
			LOG.warn("Invalid volume type {}. Defaulting to {}.", webDavUrlSchemeName, Settings.DEFAULT_GVFS_SCHEME);
			return Settings.DEFAULT_GVFS_SCHEME;
		}
	}

	private UiTheme parseUiTheme(String uiThemeName) {
		try {
			return UiTheme.valueOf(uiThemeName.toUpperCase());
		} catch (IllegalArgumentException e) {
			LOG.warn("Invalid volume type {}. Defaulting to {}.", uiThemeName, Settings.DEFAULT_THEME);
			return Settings.DEFAULT_THEME;
		}
	}

	private List<VaultSettings> readVaultSettingsArray(JsonReader in) throws IOException {
		List<VaultSettings> result = new ArrayList<>();
		in.beginArray();
		while (!JsonToken.END_ARRAY.equals(in.peek())) {
			result.add(vaultSettingsJsonAdapter.read(in));
		}
		in.endArray();
		return result;
	}

}