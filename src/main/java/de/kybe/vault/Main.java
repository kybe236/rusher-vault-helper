package de.kybe.vault;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

public class Main extends Plugin {
	
	@Override
	public void onLoad() {
		final VaultHelperModule vaultHelperModule = new VaultHelperModule();
		RusherHackAPI.getModuleManager().registerFeature(vaultHelperModule);
	}
	
	@Override
	public void onUnload() {

	}
	
}