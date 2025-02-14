package com.mt1006.mocap.mocap.actions;

import com.google.gson.Gson;
import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SetIcarusWings implements ComparableAction {

    private static final Gson gson = new Gson();

    private final Item wings;

    private ItemStack temp;

    public SetIcarusWings(Entity entity) {
        if (entity instanceof LivingEntity) {
            Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent((LivingEntity) entity);
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
            this.wings = null;
        } else {
            this.wings = temp.getItem();
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
        //this.wings = deserialize(reader.readString());
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
        if (!(ctx.entity instanceof LivingEntity)) {
            return Result.IGNORED;
        }

        System.out.println("Execute: " + wings);

        /*LivingEntity entity = (LivingEntity) ctx.entity;
        Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent(entity);
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
