package com.devmaster.cult_of_israphel.misc;

import com.devmaster.cult_of_israphel.entity.renders.IsraphelRenderer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Cult_of_Israphel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                RegistryHandler.ISRAPHEL.get(),
                IsraphelRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                IsraphelRenderer.ISRAPHEL_LAYER,
                () -> LayerDefinition.create(
                        HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F),
                        64,
                        64
                )
        );
    }
}
