package eu.stefanbraun612.nnr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NNRConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("nnr.json");

	public boolean enabled = true;
	public int yThreshold = 128;
	public boolean ignoreSpectators = true;
	public boolean ignoreCreativePlayers = true;
	public boolean ignoreOps = true;
	public List<String> whitelistedPlayers = new ArrayList<>();

	public static NNRConfig load() {
		if (Files.exists(PATH)) {
			try (var reader = Files.newBufferedReader(PATH)) {
				NNRConfig config = GSON.fromJson(reader, NNRConfig.class);
				if (config != null) {
					return config;
				}
			} catch (IOException e) {
				NNRMod.LOGGER.warn("Failed to read nnr.json, using defaults", e);
			}
		}
		NNRConfig config = new NNRConfig();
		config.save();
		return config;
	}

	public void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (var writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException e) {
			NNRMod.LOGGER.warn("Failed to write nnr.json", e);
		}
	}
}
