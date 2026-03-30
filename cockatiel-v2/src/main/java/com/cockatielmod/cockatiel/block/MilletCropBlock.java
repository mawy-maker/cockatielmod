package com.cockatielmod.cockatiel.block;

import com.cockatielmod.cockatiel.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class MilletCropBlock extends CropBlock {

    public MilletCropBlock(Properties props) {
        super(props);
    }

    @Override
    protected ItemStack getBaseSeedId() {
        return new ItemStack(ModItems.MILLET_SEEDS.get());
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return 7;
    }
}
