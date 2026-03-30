package com.cockatielmod.cockatiel.registry;

import com.cockatielmod.cockatiel.CockatielMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CockatielMod.MOD_ID);

    public static final Supplier<CreativeModeTab> COCKATIEL_TAB =
            CREATIVE_MODE_TABS.register("cockatiel_tab", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.cockatiel.tab"))
                            .icon(() -> new ItemStack(ModItems.COCKATIEL_SPAWN_EGG.get()))
                            .displayItems((params, output) -> {
                                // Spawn egg
                                output.accept(ModItems.COCKATIEL_SPAWN_EGG.get());
                                // Food / feeding
                                output.accept(ModItems.MILLET_SEEDS.get());
                                output.accept(ModItems.MILLET_BUNCH.get());
                                output.accept(ModItems.MILLET_SPRAY.get());
                                output.accept(ModItems.COCKATIEL_TREAT.get());
                                // Drops / rewards
                                output.accept(ModItems.COCKATIEL_FEATHER.get());
                                output.accept(ModItems.YELLOW_COCKATIEL_FEATHER.get());
                                output.accept(ModItems.WHITE_COCKATIEL_FEATHER.get());
                                output.accept(ModItems.COCKATIEL_POTION.get());
                                // Tools
                                output.accept(ModItems.COCKATIEL_WHISTLE.get());
                                // Blocks
                                output.accept(ModBlocks.PERCH.get());
                                output.accept(ModBlocks.COCKATIEL_CAGE.get());
                            })
                            .build()
            );

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
