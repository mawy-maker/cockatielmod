package com.cockatielmod.cockatiel.registry;

import com.cockatielmod.cockatiel.CockatielMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, CockatielMod.MOD_ID);
    // Menu types can be added here if needed in the future
}
