package com.cockatielmod.cockatiel.item;

import com.cockatielmod.cockatiel.registry.ModEntities;
import net.minecraft.world.item.SpawnEggItem;

public class CockatielSpawnEggItem extends SpawnEggItem {
    // Grey body (#A0A0A0), yellow cheek (#FFD700)
    public CockatielSpawnEggItem(Properties props) {
        super(ModEntities.COCKATIEL.get(), 0xA0A0A0, 0xFFD700, props);
    }
}
