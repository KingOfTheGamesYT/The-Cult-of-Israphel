package com.devmaster.cult_of_israphel.entity.renders;

import com.devmaster.cult_of_israphel.entity.Cultist;
import com.devmaster.cult_of_israphel.misc.Cult_of_Israphel;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CultistRenderer extends HumanoidMobRenderer<Cultist, HumanoidModel<Cultist>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Cult_of_Israphel.MOD_ID, "textures/entity/cultist.png");

    public static final ModelLayerLocation CULTIST_LAYER = new ModelLayerLocation(
                    new ResourceLocation(Cult_of_Israphel.MOD_ID, "cultist"),
                    "main"
            );

    public CultistRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(CULTIST_LAYER)), 0.5F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(Cultist entity) {
        return TEXTURE;
    }
}