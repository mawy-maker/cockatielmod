package com.cockatielmod.cockatiel;

import com.cockatielmod.cockatiel.entity.CockatielEntity;
import com.cockatielmod.cockatiel.registry.ModEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = CockatielMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CommonSetup {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.COCKATIEL.get(), CockatielEntity.createAttributes().build());
    }
}
