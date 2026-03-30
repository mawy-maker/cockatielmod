package com.cockatielmod.cockatiel.client;

import com.cockatielmod.cockatiel.CockatielMod;
import com.cockatielmod.cockatiel.client.renderer.entity.CockatielRenderer;
import com.cockatielmod.cockatiel.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = CockatielMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.COCKATIEL.get(), CockatielRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                CockatielRenderer.LAYER_LOCATION,
                CockatielModel::createBodyLayer
        );
    }
}
