package com.devmaster.cult_of_israphel.entity.renders;

import com.devmaster.cult_of_israphel.entity.Adept;
import com.devmaster.cult_of_israphel.misc.Cult_of_Israphel;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AdeptRenderer extends HumanoidMobRenderer<Adept, HumanoidModel<Adept>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Cult_of_Israphel.MOD_ID, "textures/entity/adept.png");
    public static final ModelLayerLocation ADEPT_LAYER = new ModelLayerLocation(new ResourceLocation(Cult_of_Israphel.MOD_ID, "adept"), "main");

    public AdeptRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ADEPT_LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Adept entity) {
        return TEXTURE;
    }
}