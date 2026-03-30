package com.cockatielmod.cockatiel.registry;

import com.cockatielmod.cockatiel.CockatielMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CockatielMod.MOD_ID);

    public static final Supplier<CreativeModeTab> COCKATIEL_TAB = TABS.register("cockatiel_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.cockatiel.tab"))
                    .icon(() -> new ItemStack(ModItems.COCKATIEL_SPAWN_EGG.get()))
                    .displayItems((params, out) -> {
                        out.accept(ModItems.COCKATIEL_SPAWN_EGG.get());
                        out.accept(ModItems.MILLET_SEEDS.get());
                        out.accept(ModItems.MILLET_BUNCH.get());
                        out.accept(ModItems.COCKATIEL_TREAT.get());
                        out.accept(ModItems.COCKATIEL_FEATHER.get());
                        out.accept(ModItems.YELLOW_COCKATIEL_FEATHER.get());
                        out.accept(ModItems.WHITE_COCKATIEL_FEATHER.get());
                        out.accept(ModItems.COCKATIEL_POTION.get());
                        out.accept(ModItems.COCKATIEL_WHISTLE.get());
                        out.accept(ModBlocks.PERCH.get());
                        out.accept(ModBlocks.COCKATIEL_CAGE.get());
                    })
                    .build());
}
