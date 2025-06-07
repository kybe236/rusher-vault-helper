package de.kybe.vault;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultClientData;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.events.render.EventRender3D;
import org.rusherhack.client.api.events.world.EventChunk;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.render.IRenderer3D;
import org.rusherhack.client.api.setting.ColorSetting;
import org.rusherhack.client.api.utils.ChatUtils;
import org.rusherhack.client.api.utils.WorldUtils;
import org.rusherhack.core.event.stage.Stage;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.utils.ColorUtils;

import java.awt.*;
import java.util.ArrayList;


public class VaultHelperModule extends ToggleableModule {
	BooleanSetting sound = new BooleanSetting("sound", "Play sound when vault is found", true);

	BooleanSetting showNormal = new BooleanSetting("showNormal", "Show active vaults", true);
	ColorSetting normal = new ColorSetting("normal", "Color of active vaults", Color.ORANGE.getRGB());
	BooleanSetting fillNormal = new BooleanSetting("fillNormal", false);
	BooleanSetting outlineNormal = new BooleanSetting("outlineNormal", false);
	//BooleanSetting tracersNormal = new BooleanSetting("tracersNormal", true);

	BooleanSetting showOminous = new BooleanSetting("showOminous", "Show ominous vaults", true);
	ColorSetting ominous = new ColorSetting("ominous", "Color of ominous vaults", Color.YELLOW.getRGB());
	BooleanSetting fillOminous = new BooleanSetting("fillLooted", false);
	BooleanSetting outlineOminous = new BooleanSetting("outlineLooted", false);
	//BooleanSetting tracersOminous = new BooleanSetting("tracersOminous", true);


	public VaultHelperModule() {
		super("Vault Helper", "Helps looting vaults", ModuleCategory.CLIENT);


		showNormal.addSubSettings(normal, fillNormal, outlineNormal);
		showOminous.addSubSettings(ominous, fillOminous, outlineOminous);

		this.registerSettings(showNormal, showOminous, sound);
	}

	ArrayList<BlockPos> normalVaults = new ArrayList<>();
	ArrayList<BlockPos> ominousVaults = new ArrayList<>();

	@Subscribe
	private void onChunkLoad(EventChunk.Load event) {
		ChunkPos chunkPos = event.getChunkPos();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = -64; y < 320; y++) {
					BlockPos blockPos = new BlockPos(chunkPos.x * 16 + x, y, chunkPos.z * 16 + z);
					BlockState blockState = event.getChunk().getBlockState(blockPos);

					if (blockState.getBlock() == Blocks.VAULT) {
						boolean isOminous = blockState.getValue(VaultBlock.OMINOUS);
						if (sound.getValue()) {
							mc.player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 10.0F, 1.0F);
						}
						if (isOminous) {
							ominousVaults.add(blockPos);
						} else {
							normalVaults.add(blockPos);
						}
					}
				}
			}
		}
	}

	@Subscribe
	private void onChunkUnload(EventChunk.Unload event) {
		ChunkPos chunkPos = event.getChunkPos();
		normalVaults.removeIf(blockPos -> blockPos.getX() >= chunkPos.x * 16 && blockPos.getX() < (chunkPos.x + 1) * 16 &&
				blockPos.getZ() >= chunkPos.z * 16 && blockPos.getZ() < (chunkPos.z + 1) * 16);
		ominousVaults.removeIf(blockPos -> blockPos.getX() >= chunkPos.x * 16 && blockPos.getX() < (chunkPos.x + 1) * 16 &&
			blockPos.getZ() >= chunkPos.z * 16 && blockPos.getZ() < (chunkPos.z + 1) * 16);
	}

	@Subscribe
	private void onRender3D(EventRender3D event) {
		if (mc.player == null || mc.cameraEntity == null) return;
		final IRenderer3D renderer = event.getRenderer();

		final int normalColor = ColorUtils.transparency(normal.getValueRGB(), normal.getAlpha());
		final int ominousColor = ColorUtils.transparency(ominous.getValueRGB(), ominous.getAlpha());
		
		//begin renderer
		renderer.begin(event.getMatrixStack());

		for (BlockPos normalVault : normalVaults) {
			renderer.drawBox(normalVault, fillNormal.getValue(), outlineNormal.getValue(), normalColor);
			//if (tracersNormal.getValue()) {
				//renderer.drawLine(mc.cameraEntity.getEyePosition(), normalVault.getCenter(), normalColor);
			//}
		}

		for (BlockPos ominousVault : ominousVaults) {
			renderer.drawBox(ominousVault, fillOminous.getValue(), outlineOminous.getValue(), ominousColor);
			//if (tracersOminous.getValue()) {
				//renderer.drawLine(mc.cameraEntity.getEyePosition(), ominousVault.getCenter(), ominousColor);
			//}
		}

		//end renderer
		renderer.end();
	}

	@Override
	public void onEnable() {
		if (mc.level == null) return;
		int renderDistance = mc.options.renderDistance().get();
		ChunkPos playerChunk = mc.player.chunkPosition();
		for (int xo = -renderDistance; xo < renderDistance; xo++) {
			for (int zo = -renderDistance; zo < renderDistance; zo++) {
				ChunkPos chunkPos = new ChunkPos(playerChunk.x + xo, playerChunk.z + zo);
				LevelChunk chunk = mc.level.getChunk(playerChunk.x + xo, playerChunk.z + zo);
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						for (int y = -64; y < 320; y++) {
							BlockPos blockPos = new BlockPos(chunkPos.x * 16 + x, y, chunkPos.z * 16 + z);
							BlockState blockState = chunk.getBlockState(blockPos);

							if (blockState.getBlock() == Blocks.VAULT) {
								boolean isOminous = blockState.getValue(VaultBlock.OMINOUS);
								if (isOminous) {
									ominousVaults.add(blockPos);
								} else {
									normalVaults.add(blockPos);
								}
							}
						}
					}
				}
			}
		}
	}
}
