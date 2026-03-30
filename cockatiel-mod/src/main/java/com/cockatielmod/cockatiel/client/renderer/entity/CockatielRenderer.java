package com.cockatielmod.cockatiel.client.renderer.entity;

import com.cockatielmod.cockatiel.CockatielMod;
import com.cockatielmod.cockatiel.client.CockatielModel;
import com.cockatielmod.cockatiel.entity.CockatielEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class CockatielRenderer extends MobRenderer<CockatielEntity, CockatielModel> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CockatielMod.rl("cockatiel"), "main");

    // Texture paths for each variant
    private static final ResourceLocation TEXTURE_GREY =
            CockatielMod.rl("textures/entity/cockatiel/cockatiel_grey.png");
    private static final ResourceLocation TEXTURE_LUTINO =
            CockatielMod.rl("textures/entity/cockatiel/cockatiel_lutino.png");
    private static final ResourceLocation TEXTURE_ALBINO =
            CockatielMod.rl("textures/entity/cockatiel/cockatiel_albino.png");

    public CockatielRenderer(EntityRendererProvider.Context context) {
        super(context, new CockatielModel(context.bakeLayer(LAYER_LOCATION)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(CockatielEntity entity) {
        return switch (entity.getVariant()) {
            case CockatielEntity.VARIANT_LUTINO -> TEXTURE_LUTINO;
            case CockatielEntity.VARIANT_ALBINO -> TEXTURE_ALBINO;
            default -> TEXTURE_GREY;
        };
    }

    @Override
    protected float getBob(CockatielEntity entity, float partialTick) {
        // Gentle head bobbing when alive
        return entity.isFlying() ? Mth.sin(entity.tickCount * 0.3f) * 2.0f : 0.0f;
    }
}
