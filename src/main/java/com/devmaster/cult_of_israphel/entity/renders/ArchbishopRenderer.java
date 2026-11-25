package com.devmaster.cult_of_israphel.entity.renders;

import com.devmaster.cult_of_israphel.entity.Archbishop;
import com.devmaster.cult_of_israphel.entity.Cultist;
import com.devmaster.cult_of_israphel.misc.Cult_of_Israphel;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ArchbishopRenderer extends HumanoidMobRenderer<Archbishop, HumanoidModel<Archbishop>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Cult_of_Israphel.MOD_ID, "textures/entity/archbishop.png");
    public static final ModelLayerLocation ARCHBISHOP_LAYER = new ModelLayerLocation(new ResourceLocation(Cult_of_Israphel.MOD_ID, "archbishop"), "main");

    public ArchbishopRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ARCHBISHOP_LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Archbishop entity) {
        return TEXTURE;
    }
}