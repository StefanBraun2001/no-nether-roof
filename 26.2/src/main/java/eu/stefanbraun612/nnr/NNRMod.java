package eu.stefanbraun612.nnr;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NNRMod implements ModInitializer {
	public static final String MOD_ID = "nnr";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static NNRConfig config;

	@Override
	public void onInitialize() {
		config = NNRConfig.load();

		ServerTickEvents.END_LEVEL_TICK.register(NNRMod::onLevelTick);
	}

	private static void onLevelTick(ServerLevel level) {
		if (!config.enabled || level.dimension() != Level.NETHER) {
			return;
		}

		// Copy the list first - teleporting a player off this level removes them
		// from level.players() mid-iteration, which throws a ConcurrentModificationException.
		for (ServerPlayer player : List.copyOf(level.players())) {
			if (player.getY() < config.yThreshold) {
				continue;
			}
			if (isIgnored(player)) {
				continue;
			}
			teleportOffRoof(player);
		}
	}

	private static boolean isIgnored(ServerPlayer player) {
		if (config.ignoreSpectators && player.isSpectator()) {
			return true;
		}
		if (config.ignoreCreativePlayers && player.isCreative()) {
			return true;
		}
		if (config.ignoreOps && player.level().getServer().getPlayerList().isOp(player.nameAndId())) {
			return true;
		}
		for (String name : config.whitelistedPlayers) {
			if (name.equalsIgnoreCase(player.getName().getString())) {
				return true;
			}
		}
		return false;
	}

	private static void teleportOffRoof(ServerPlayer player) {
		TeleportTransition transition = player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);

		// Safety net: if the player's own bed/anchor spawn is itself on/above the
		// roof, falling back there would just re-trigger this check immediately.
		if (transition.newLevel().dimension() == Level.NETHER && transition.position().y >= config.yThreshold) {
			ServerLevel overworld = player.level().getServer().getLevel(Level.OVERWORLD);
			if (overworld != null) {
				var spawnPos = player.adjustSpawnLocation(overworld, overworld.getRespawnData().pos());
				transition = new TeleportTransition(overworld,
						net.minecraft.world.phys.Vec3.atBottomCenterOf(spawnPos),
						net.minecraft.world.phys.Vec3.ZERO, 0f, 0f, TeleportTransition.DO_NOTHING);
			}
		}

		player.teleport(transition);
		// Plain literal, not translatable() - this is a server-only mod, so joining
		// clients never have the mod's lang file and would just see the raw key.
		player.sendSystemMessage(Component.literal("Usage of the Nether Roof is not allowed on this server.")
				.withStyle(ChatFormatting.RED));
		LOGGER.info("Teleported {} off the Nether roof", player.getName().getString());
	}
}
