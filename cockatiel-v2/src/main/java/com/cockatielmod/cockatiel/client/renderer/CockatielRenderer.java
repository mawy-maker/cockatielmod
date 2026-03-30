package com.cockatielmod.cockatiel.client.renderer;

import com.cockatielmod.cockatiel.client.model.CockatielGeoModel;
import com.cockatielmod.cockatiel.entity.CockatielEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CockatielRenderer extends GeoEntityRenderer<CockatielEntity> {

    public CockatielRenderer(EntityRendererProvider.Context context) {
        super(context, new CockatielGeoModel());
        this.shadowRadius = 0.25f;
    }
}
