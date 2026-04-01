package com.cockatielmod.cockatiel.client.model;

import com.cockatielmod.cockatiel.CockatielMod;
import com.cockatielmod.cockatiel.entity.CockatielEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

public class CockatielGeoModel extends GeoModel<CockatielEntity> {

    @Override
    public ResourceLocation getModelResource(CockatielEntity entity, GeoRenderer<CockatielEntity> renderer) {
        return CockatielMod.rl("geo/cockatiel.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CockatielEntity entity, GeoRenderer<CockatielEntity> renderer) {
        return switch (entity.getVariant()) {
            case CockatielEntity.VARIANT_LUTINO -> CockatielMod.rl("textures/entity/cockatiel/cockatiel_lutino.png");
            case CockatielEntity.VARIANT_ALBINO -> CockatielMod.rl("textures/entity/cockatiel/cockatiel_albino.png");
            default -> CockatielMod.rl("textures/entity/cockatiel/cockatiel_grey.png");
        };
    }

    @Override
    public ResourceLocation getAnimationResource(CockatielEntity entity) {
        return CockatielMod.rl("animations/cockatiel.animation.json");
    }
}
