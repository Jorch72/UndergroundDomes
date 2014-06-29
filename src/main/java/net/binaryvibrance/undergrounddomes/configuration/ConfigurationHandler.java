package net.binaryvibrance.undergrounddomes.configuration;

//import net.binaryvibrance.undergrounddomes.Configuration;

import java.io.File;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.Mod;
import net.binaryvibrance.undergrounddomes.Constants;
import net.binaryvibrance.undergrounddomes.generation.GenACorridorGenerator;
import net.binaryvibrance.undergrounddomes.generation.GenADomeGenerator;
import net.binaryvibrance.undergrounddomes.generation.contracts.ICorridorGenerator;
import net.binaryvibrance.undergrounddomes.generation.contracts.IDomeGenerator;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**
 * Created by CodeWarrior on 30/06/2014.
 */
public class ConfigurationHandler {
	public static Configuration configuration;
	private static ConfigurationHandler _instance;
	private Property domeHeight;
	private Property multiThreadedWorldGen;

	private ConfigurationHandler (File configFile) {
		configuration = new Configuration(configFile);
		syncConfig();
	}

	public static ConfigurationHandler createInstance(File configFile) {
		_instance = new ConfigurationHandler(configFile);
		return _instance;
	}

	public static ConfigurationHandler instance() {
		return _instance;
	}

	private void syncConfig() {
		domeHeight = configuration.get(Configuration.CATEGORY_GENERAL, "domeHeight", 96, "Sets the domes to be generated higher than intended. This is useful if you don't want domes overwriting valuable resources like diamond or gold.");
		domeHeight.setLanguageKey(Constants.Mod.MOD_ID + ".config.domeHeight");
		multiThreadedWorldGen = configuration.get(Configuration.CATEGORY_GENERAL, "multiThreadedWorldGen", true, "Use a background thread for creating the domes.");
		domeHeight.setLanguageKey(Constants.Mod.MOD_ID + ".config.multiThreadedWorldGen");

		if (configuration.hasChanged()) {
			configuration.save();
		}
	}

	@Mod.EventHandler
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.modID.equals(Constants.Mod.MOD_ID)) {
			syncConfig();
		}
	}

	public int getDomeHeight() {
		return domeHeight.getInt();
	}

	public void setDomeHeight(int domeHeight) {
		this.domeHeight.set(domeHeight);
	}

	public boolean getMultiThreadedWorldGen() {
		return multiThreadedWorldGen.getBoolean();
	}

	//Non persisted settings
	public IDomeGenerator getDefaultDomeGenerator() {
		return new GenADomeGenerator();
	}

	public ICorridorGenerator getDefaultCorridorGenerator() {
		return new GenACorridorGenerator();
	}
}
