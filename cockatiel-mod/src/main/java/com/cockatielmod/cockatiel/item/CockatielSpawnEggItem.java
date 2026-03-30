package com.cockatielmod.cockatiel.item;

import com.cockatielmod.cockatiel.registry.ModEntities;
import net.minecraft.world.item.SpawnEggItem;

public class CockatielSpawnEggItem extends SpawnEggItem {
    public CockatielSpawnEggItem(Properties properties) {
        super(ModEntities.COCKATIEL.get(), 0xC8C8C8, 0xFFDD00, properties);
    }
}
