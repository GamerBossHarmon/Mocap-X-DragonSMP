package com.mt1006.mocap.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.cammiescorner.icarus.Icarus;
import dev.cammiescorner.icarus.client.renderers.WingsFeatureRenderer;
import dev.cammiescorner.icarus.common.items.WingItem;
import dev.cammiescorner.icarus.core.registry.ModItems;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(WingsFeatureRenderer.class)
public class WingsFeatureRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void beforeRender(
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            LivingEntity entity,
            float limbAngle,
            float limbDistance,
            float tickDelta,
            float animationProgress,
            float headYaw,
            float headPitch,
            CallbackInfo ci) {
        //1. Check if they have are tag if do update wings,
        //2. Check if they have the wings components if do ignore/return,
        //3. Check if they have NBT data for the wings, if do give them are tag.
        if (entity instanceof Player player) {
            if (entity.getTags().contains("MocapFakePlayerC") && !entity.getTags().contains("GivenWings")) {
                //System.out.println("has are tag");
                //CompoundTag nbtData = new CompoundTag();
                //entity.saveWithoutId(nbtData);
                //ItemStack wings = ItemStack.of(nbtData.getCompound("cardinal_components").getCompound("trinkets:trinkets").getCompound("chest").getCompound("cape").getList("Items", 10).getCompound(0));

                String entityName = entity.getName().getString();

                ItemStack wings = switch (entityName) {
                    case "FDragongirl1349" -> new ItemStack(ModItems.BLACK_DRAGON_WINGS);
                    case "FIris_034" -> new ItemStack(ModItems.LIGHT_BLUE_MECHANICAL_LEATHER_WINGS);
                    case "F_DuskShadow_" -> new ItemStack(ModItems.GREY_FEATHERED_WINGS);
                    case "FFeathyriarch" -> new ItemStack(ModItems.BLACK_FEATHERED_WINGS);
                    case "FRoyxl_Blue" -> new ItemStack(ModItems.BLUE_DRAGON_WINGS);
                    case "FProtogenAzul" -> new ItemStack(ModItems.LIGHT_GREY_FEATHERED_WINGS);
                    case "FSc00tch" -> new ItemStack(ModItems.WHITE_FEATHERED_WINGS);
                    case "FHarmonProto" -> new ItemStack(ModItems.RED_MECHANICAL_FEATHERED_WINGS);
                    case "FSwaggy_Banana12" -> new ItemStack(ModItems.YELLOW_FEATHERED_WINGS);
                    case "FOnyxOwO" -> new ItemStack(ModItems.BLUE_DRAGON_WINGS);
                    case "FGlitchi354" -> new ItemStack(ModItems.BLACK_LIGHT_WINGS);
                    case "FTheRubyNinja1" -> new ItemStack(ModItems.RED_MECHANICAL_FEATHERED_WINGS);
                    case "FShadowKent" -> new ItemStack(ModItems.MAGENTA_MECHANICAL_LEATHER_WINGS);
                    case "FChrissy283" -> new ItemStack(ModItems.BLACK_MECHANICAL_FEATHERED_WINGS);//Need to check
                    case "FMobpenguin" -> new ItemStack(ModItems.BLACK_FEATHERED_WINGS);
                    // case "Royxl_Blue" -> new ItemStack((ItemLike) null); // If I need to take someone wings off
                    default -> null;
                };

                if (wings != null) {
                    entity.addTag("GivenWings");
                    Optional<TrinketComponent> componentOpt  = TrinketsApi.getTrinketComponent(entity);
                    componentOpt.ifPresent(component -> {
                        // Access the full trinket inventory map
                        var inventoryMap = component.getInventory();

                        // Look for the chest/cape slot
                        if (inventoryMap.containsKey("chest")) {
                            var chestSlots = inventoryMap.get("chest");
                            if (chestSlots.containsKey("cape")) {
                                var inventory = chestSlots.get("cape");

                                //SlotReference slotRef = inventory
                                //System.out.println("Inventory: " + inventory);
                                inventory.setItem(0, wings);
                                //↓↓↓ Don't know what this does, just found it looking in the "TrinketInventory" class
                                inventory.markUpdate();//Does this do anything IDK
                                inventory.update();//Does this do anything IDK

                                //System.out.println("Equipped wing in chest/cape!");
                            } else {
                                System.out.println("No cape slot in chest group.");
                            }
                        } else {
                            System.out.println("No chest slot group.");
                        }
                    });
                }
            }

            AtomicBoolean hasWings = new AtomicBoolean(false);
            Optional<TrinketComponent> componentOpt  = TrinketsApi.getTrinketComponent(entity);

            componentOpt.ifPresent(component -> {
                if (!component.isEquipped((stack) -> stack.getItem() instanceof WingItem) && Icarus.HAS_POWERED_FLIGHT.test(entity)) {
                   hasWings.set(true);
                    //System.out.println("has wings");
                }
            });
            //Check if XpSeed is 0 && XpTotal = 0 && DeathTime = 0 && XpLevel = 0 (should be good to make sure it a fake player)
            if (!hasWings.get()) {
                /*System.out.println("no wings");
                CompoundTag nbtData = new CompoundTag();
                entity.saveWithoutId(nbtData);

                CompoundTag nbtData22 = new CompoundTag();
                entity.save(nbtData22);
                ItemStack wings222 = ItemStack.of(nbtData22.getCompound("cardinal_components").getCompound("trinkets:trinkets").getCompound("chest").getCompound("cape").getList("Items", 10).getCompound(0));

                CompoundTag nbtData33 = new CompoundTag();
                entity.readAdditionalSaveData(nbtData33);

                ItemStack wings = ItemStack.of(nbtData.getCompound("cardinal_components").getCompound("trinkets:trinkets").getCompound("chest").getCompound("cape").getList("Items", 10).getCompound(0));

                wings = new ItemStack(ModItems.BLACK_DRAGON_WINGS);*/

                /*CompoundTag nbtData = new CompoundTag(); //Not given to the client dua
                entity.saveWithoutId(nbtData);
                int XpSeed = nbtData.getInt("XpSeed");*/

                CompoundTag nbtData = new CompoundTag();
                entity.saveWithoutId(nbtData);
                int inventory = nbtData.getList("Inventory", 10).size();

                //if(wings.getItem() instanceof WingItem) {
                if(inventory == 0 && player.experienceProgress == 0 && player.experienceLevel == 0 && player.totalExperience == 0 && player.deathTime == 0 && player.getLastHurtByMobTimestamp() == 0) {
                    entity.addTag("MocapFakePlayerC");
                }
            }
        }
    }
}
