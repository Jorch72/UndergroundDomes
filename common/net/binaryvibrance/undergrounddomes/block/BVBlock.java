package net.binaryvibrance.undergrounddomes.block;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.binaryvibrance.undergrounddomes.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public abstract class BVBlock extends Block {

	public BVBlock(int id, Material material) {
		super(id, material);
		// TODO Auto-generated constructor stub
	}
	
	protected abstract String getBlockName();
	
	public void selfRegister() {
		GameRegistry.registerBlock(this, getModLocalizedName());
    	LanguageRegistry.addName(this, getBlockName());
	}

	public String getModLocalizedName() {
		// TODO Auto-generated method stub
		return Constants.Mod.MOD_ID + "." + getUnlocalizedName2();
	}
}
