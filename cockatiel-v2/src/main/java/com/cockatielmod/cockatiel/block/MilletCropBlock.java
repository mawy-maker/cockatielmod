package com.cockatielmod.cockatiel.block;

import com.cockatielmod.cockatiel.registry.ModItems;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class MilletCropBlock extends CropBlock {

    public MilletCropBlock(Properties props) {
        super(props);
    }

    // 1.21.1: returns ItemLike, not ItemStack
    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.MILLET_SEEDS.get();
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
