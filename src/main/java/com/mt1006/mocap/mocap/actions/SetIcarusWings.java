package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import dev.emi.trinkets.api.*;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SetIcarusWings implements ComparableAction {

    //private final boolean hasWings;
    private final Item wings;

    private ItemStack temp;

    public SetIcarusWings(Entity entity) {
        if (entity instanceof ServerPlayer) {
            Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent((ServerPlayer) entity);
            trinketComponent.ifPresent(component -> {
                component.getAllEquipped().forEach(tuple -> {
                    SlotReference slotReference = tuple.getA();
                    ItemStack itemStack = tuple.getB();
                    if ("chest".equals(slotReference.inventory().getSlotType().getGroup()) &&
                            "cape".equals(slotReference.inventory().getSlotType().getName())) {
                        temp = itemStack;
                        //System.out.println("Chest Cape Slot: " + itemStack);
                    }
                });
            });
        }
        if (temp == null || temp.isEmpty()) {
            //System.out.println("No Wings Found");
            //this.hasWings = false;
            this.wings = null;
        } else {
            //this.hasWings = true;
            this.wings = temp.getItem();
            TrinketComponent comp = TrinketsApi.getTrinketComponent((ServerPlayer) entity).get();
            //SlotType slotNumber = TrinketsApi.getTrinketComponent((ServerPlayer) entity).get().getInventory().get("chest").get("cape").getSlotType();
            //System.out.println("Slot Number: " + slotNumber);
        }
    }

    public SetIcarusWings(RecordingFiles.Reader reader) {
        //System.out.println("SetIcarusWings-:\n" + Registry.ITEM.get(ResourceLocation.tryParse(reader.readString())));
        this.wings = Registry.ITEM.get(ResourceLocation.tryParse(reader.readString()));
    }

    @Override
    public boolean differs(ComparableAction action) {
        return false;
        //return wings != ((SetIcarusWings) action).wings;
    }

    @Override
    public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action) {
        if (action != null && !differs(action)) { return; }

        writer.addByte(Type.SET_ICARUS_WINGS.id);

        ResourceLocation location = Registry.ITEM.getKey(wings);
        System.out.println("Write:\n" + location.toString());
        writer.addString(location.toString());
    }

    //Currently The player shows as they have the NBT data for the item, but they do not render with the wings on.
    //In here is shows that the entity has the old "TrinketComponent" wings, and they also get replace with the new wings.
    //But placing a debugger in Icarus "WingsFeatureRenderer", The "TrinketComponent" does not show that the entity has the wings.
    @Override
    public Result execute(PlayingContext ctx) {
        if (!(ctx.entity instanceof ServerPlayer entity)) {
            return Result.IGNORED;
        }
        //System.out.println("TAGS: " + entity.getTags());

        //Debugging test code, for NBT stuff
        /*CompoundTag nbtData = new CompoundTag();
        entity.saveWithoutId(nbtData);
        System.out.println("Entity NBT (without ID): " + nbtData);
        System.out.println("Wing NBT: " + nbtData.getCompound("cardinal_components").getCompound("trinkets:trinkets").getCompound("chest").getCompound("cape").getList("Items", 10).getCompound(0).getString("id"));
        ItemStack test = ItemStack.of(nbtData.getCompound("cardinal_components").getCompound("trinkets:trinkets").getCompound("chest").getCompound("cape").getList("Items", 10).getCompound(0));

        CompoundTag components = nbtData.getCompound("cardinal_components");
        CompoundTag trinkets = components.getCompound("trinkets:trinkets");
        CompoundTag chest = trinkets.getCompound("chest");
        CompoundTag cape = chest.getCompound("cape");
        ListTag items = cape.getList("Items", 10); // 10 is the tag type for CompoundTag

        if (!items.isEmpty()) {
            CompoundTag item = items.getCompound(0);
            String itemId = item.getString("id");
            System.out.println("Found item ID: " + itemId);
        }*/

        //System.out.println("Execute: " + wings);
        ItemStack wing = new ItemStack(wings);
        //System.out.println("SetIcarusWings: " + Trinkets);

        //More debugging test code
        /*TrinketComponent comp = TrinketsApi.getTrinketComponent((ServerPlayer) entity).get();
        SlotGroup slotGroupChest = comp.getGroups().get("chest");
        System.out.println("Slot Group: " + slotGroupChest);
        SlotType slotTypeWings = slotGroupChest.getSlots().get("cape");
        System.out.println("Slot Group: " + slotTypeWings);*/

        //Optional<TrinketComponent> componentOpt  = TrinketsApi.getTrinketComponent(entity);
        Optional<TrinketComponent> componentOpt  = TrinketsApi.getTrinketComponent((LivingEntity) ctx.entity);

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
                    inventory.setItem(0, wing);
                    //↓↓↓ Don't know what this does, just found it looking in the "TrinketInventory" class
                    inventory.markUpdate();//Does this do anything IDK
                    inventory.update();//Does this do anything IDK

                    //System.out.println("Equipped wing in chest/cape!");
                } else {
                    //System.out.println("No cape slot in chest group.");
                }
            } else {
                //System.out.println("No chest slot group.");
            }
        });

        /*TrinketComponent compnumber2 = TrinketsApi.getTrinketComponent(entity).get();//Nope NBT is still not sending
        compnumber2.getInventory().get("chest").get("cape").markUpdate();
        //comp.sync(); // This is the key method to sync Trinkets data

        // Force inventory update
        entity.containerMenu.broadcastChanges();*/

        /*SynchedEntityData data = entity.getEntityData(); //Nope :(
        //data.markDirty();

        ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData(), true);

        entity.connection.send(packet);

        for (ServerPlayer trackingPlayer : entity.getServer().getPlayerList().getPlayers()) {
            if (trackingPlayer != entity && trackingPlayer.level == entity.level) {
                trackingPlayer.connection.send(packet);
            }
        }*/

        //found in https://github.com/emilyploszaj/trinkets/blob/de1634115ed84cb20db2d5683a4970ecdd8bfdde/src/main/java/dev/emi/trinkets/mixin/PlayerInventoryMixin.java
        //It ticks, but still does not render
        /*TrinketsApi.getTrinketComponent(entity).ifPresent(trinkets ->
                trinkets.forEach((slotReference, itemStack) ->
                        TrinketsApi.getTrinket(itemStack.getItem()).tick(itemStack, slotReference, entity)));*/

        //EntitySlotLoader.CLIENT.sync(entity);//found in https://github.com/emilyploszaj/trinkets/blob/de1634115ed84cb20db2d5683a4970ecdd8bfdde/src/main/java/dev/emi/trinkets/mixin/PlayerManagerMixin.java
        //EntitySlotLoader.SERVER.sync(entity);//^ does not work

        //https://github.com/emilyploszaj/trinkets/blob/de1634115ed84cb20db2d5683a4970ecdd8bfdde/src/main/java/dev/emi/trinkets/mixin/LivingEntityMixin.java
        //Nope not this
        /*TrinketsApi.getTrinketComponent(entity).ifPresent(trinkets -> {
            trinkets.forEach((ref, stack) -> {
                //TrinketsApi.getTrinket(oldStack.getItem()).onUnequip(oldStack, ref, entity);
                TrinketsApi.getTrinket(stack.getItem()).onEquip(stack, ref, entity);
            });
        });*/

        //https://github.com/emilyploszaj/trinkets/blob/de1634115ed84cb20db2d5683a4970ecdd8bfdde/src/main/java/dev/emi/trinkets/mixin/LivingEntityMixin.java
        //Dont think will work so stop on it
        /*FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(entity.getId());
        CompoundTag tag = new CompoundTag();
        //tag.put(trinketInventory.getSlotType().getGroup() + "/" + trinketInventory.getSlotType().getName(), trinketInventory.getSyncTag());
        TrinketsApi.getTrinketComponent(entity).ifPresent(trinkets -> {
            trinkets.forEach((ref, stack) -> {
                TrinketInventory inventory = ref.inventory();
                for (TrinketInventory trinketInventory : inventoriesToSend) {
                    tag.put(trinketInventory.getSlotType().getGroup() + "/" + trinketInventory.getSlotType().getName(), trinketInventory.getSyncTag());
                }
            });
        });
        tag.put("chest/cape", trinketInventory.getSyncTag());
        buf.writeNbt(tag);
        for(ServerPlayer player : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(player, TrinketsNetwork.SYNC_INVENTORY, buf);
        }*/

        return Result.OK;
    }
}
