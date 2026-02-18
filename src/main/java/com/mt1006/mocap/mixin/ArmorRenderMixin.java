package com.mt1006.mocap.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class ArmorRenderMixin {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onRenderArmor(PoseStack matrices, MultiBufferSource vertexConsumers, int light,
                               LivingEntity entity, float limbAngle, float limbDistance,
                               float tickDelta, float animationProgress, float headYaw, float headPitch,
                               CallbackInfo ci) {
        if (false) { // Simple toggle in case I need to remove armor form someone again
            if (entity instanceof Player player) {
                if (entity.getName().getString().equals("Achilles")) {
                    ci.cancel();
                }
            }
        }
    }
}