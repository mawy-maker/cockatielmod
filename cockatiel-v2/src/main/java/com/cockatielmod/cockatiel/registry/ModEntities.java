package com.cockatielmod.cockatiel.registry;

import com.cockatielmod.cockatiel.CockatielMod;
import com.cockatielmod.cockatiel.entity.CockatielEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, CockatielMod.MOD_ID);

    public static final Supplier<EntityType<CockatielEntity>> COCKATIEL =
            ENTITIES.register("cockatiel", () ->
                    EntityType.Builder.<CockatielEntity>of(CockatielEntity::new, MobCategory.CREATURE)
                            .sized(0.45f, 0.65f)
                            .clientTrackingRange(10)
                            .build("cockatiel"));
}
