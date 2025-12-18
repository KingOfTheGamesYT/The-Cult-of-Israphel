package com.devmaster.cult_of_israphel.entity.renders;

import com.devmaster.cult_of_israphel.entity.Israphel;
import com.devmaster.cult_of_israphel.misc.Cult_of_Israphel;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class IsraphelRenderer extends HumanoidMobRenderer<Israphel, HumanoidModel<Israphel>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Cult_of_Israphel.MOD_ID, "textures/entity/israphel.png");

    public static final ModelLayerLocation ISRAPHEL_LAYER = new ModelLayerLocation(
                    new ResourceLocation(Cult_of_Israphel.MOD_ID, "israphel"),
                    "main"
            );

    public IsraphelRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ISRAPHEL_LAYER)), 0.5F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(Israphel entity) {
        return TEXTURE;
    }
}

