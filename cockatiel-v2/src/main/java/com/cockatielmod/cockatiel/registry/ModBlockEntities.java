package com.cockatielmod.cockatiel.registry;

import com.cockatielmod.cockatiel.CockatielMod;
import com.cockatielmod.cockatiel.blockentity.CockatielCageBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CockatielMod.MOD_ID);

    public static final Supplier<BlockEntityType<CockatielCageBlockEntity>> COCKATIEL_CAGE =
            BLOCK_ENTITIES.register("cockatiel_cage", () ->
                    BlockEntityType.Builder
                            .of(CockatielCageBlockEntity::new, ModBlocks.COCKATIEL_CAGE.get())
                            .build(null));
}
