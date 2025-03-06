package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import dev.emi.trinkets.api.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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


        /*ResourceLocation location = Registry.ITEM.getKey(wings.getItem());

        System.out.println(location);

        String itemString = wings.getItem().toString();
        String locationString = location.toString();
        System.out.println(itemString);
        System.out.println(locationString);
        Item item = Registry.ITEM.get(ResourceLocation.tryParse(locationString));
        System.out.println(item);*/
    }

    public SetIcarusWings(RecordingFiles.Reader reader) {
        //System.out.println("SetIcarusWings-:\n" + Registry.ITEM.get(ResourceLocation.tryParse(reader.readString())));
        this.wings = Registry.ITEM.get(ResourceLocation.tryParse(reader.readString()));
    }

    @Override
    public boolean differs(ComparableAction action) {
        return wings != ((SetIcarusWings) action).wings;
    }

    @Override
    public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action) {
        if (action != null && !differs(action)) { return; }

        writer.addByte(Type.SET_ICARUS_WINGS.id);

        ResourceLocation location = Registry.ITEM.getKey(wings);
        System.out.println("Write:\n" + location.toString());
        writer.addString(location.toString());
    }

    @Override
    public Result execute(PlayingContext ctx) {
        if (!(ctx.entity instanceof ServerPlayer entity)) {
            return Result.IGNORED;
        }

        System.out.println("Execute: " + wings);

        //System.out.println("SetIcarusWings: " + Trinkets);

        //TrinketComponent comp = TrinketsApi.getTrinketComponent((ServerPlayer) entity).get();
        //SlotGroup slotGroup = comp.getGroups().getOrDefault(group, null);

        //boolean canPlace = comp.getInventory().get("chest").get("cape").canPlaceItem(0, new ItemStack(wings));

        //comp.getInventory().get("chest").get("cape").setStack(0, stack.createStack(amount, true));
        //comp.getInventory().get("chest").get("cape").setItem(1, new ItemStack(wings));//46 is the slot number for a real player

        //TrinketsApi.getTrinketComponent((ServerPlayer) entity).get().getInventory().get("chest").get("cape").setItem(0, new ItemStack(wings));
        //System.out.println("Slot Number: " + TrinketsApi.getTrinketComponent(entity).get().getInventory().get("chest").get("cape").getSlotType(););
        //entity.getInventory().setItem(0, new ItemStack(wings));

        //entity.getInventory().setItem(46, new ItemStack(wings));

        /*Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent(entity);
        if (trinketComponent.isPresent()) {
            TrinketComponent component = trinketComponent.get();
            component.getAllEquipped().forEach(tuple -> {
                SlotReference slotReference = tuple.getA();
                if ("chest".equals(slotReference.inventory().getSlotType().getGroup()) &&
                        "cape".equals(slotReference.inventory().getSlotType().getName())) {
                    //slotReference.inventory().setStack(slotReference.index(), wings);
                    System.out.println("Set Chest Cape Slot: " + wings);
                }
            });
        }*/

        return Result.OK;
    }
}
