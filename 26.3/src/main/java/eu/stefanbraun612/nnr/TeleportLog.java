package eu.stefanbraun612.nnr;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Plain text record of every roof teleport (config/nnr_teleports.txt), oldest lines dropped first. */
public final class TeleportLog {
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("nnr_teleports.txt");
	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

	private final List<String> lines = new ArrayList<>();
	private final int maxLines;

	public TeleportLog(int maxLines) {
		this.maxLines = Math.max(1, maxLines);
		if (Files.exists(PATH)) {
			try {
				for (String line : Files.readAllLines(PATH, StandardCharsets.UTF_8)) {
					if (!line.isBlank()) {
						lines.add(line);
					}
				}
			} catch (IOException e) {
				NNRMod.LOGGER.warn("Failed to read nnr_teleports.txt, starting empty", e);
			}
		}
	}

	public void add(String text) {
		lines.add(LocalDateTime.now().format(FORMAT) + " | " + text);
		int excess = lines.size() - maxLines;
		if (excess > 0) {
			lines.subList(0, excess).clear();
		}
		try {
			Files.createDirectories(PATH.getParent());
			Files.write(PATH, lines, StandardCharsets.UTF_8);
		} catch (IOException e) {
			NNRMod.LOGGER.warn("Failed to write nnr_teleports.txt", e);
		}
	}
}
