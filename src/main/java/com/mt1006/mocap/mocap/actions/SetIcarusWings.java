package com.mt1006.mocap.mocap.actions;

import com.google.gson.Gson;
import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class SetIcarusWings implements ComparableAction {

    private static final Gson gson = new Gson();

    public static String serialize(ItemStack itemStack) {
        return gson.toJson(itemStack);
    }

    public static ItemStack deserialize(String json) {
        return gson.fromJson(json, ItemStack.class);
    }

    private final ItemStack wings;

    private ItemStack temp;

    public SetIcarusWings(Entity entity) {
        ItemStack wingsItem = null;
        if (entity instanceof LivingEntity) {
            Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent((LivingEntity) entity);
            trinketComponent.ifPresent(component -> {
                component.getAllEquipped().forEach(tuple -> {
                    SlotReference slotReference = tuple.getA();
                    ItemStack itemStack = tuple.getB();
                    if ("chest".equals(slotReference.inventory().getSlotType().getGroup()) &&
                            "cape".equals(slotReference.inventory().getSlotType().getName())) {
                        temp = itemStack;
                        System.out.println("Chest Cape Slot: " + itemStack);
                    }
                });
            });
        }
        this.wings = temp;
    }

    public SetIcarusWings(RecordingFiles.Reader reader) {
        this.wings = deserialize(reader.readString());
    }

    @Override
    public boolean differs(ComparableAction action) {
        return wings != ((SetIcarusWings) action).wings;
    }

    @Override
    public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action) {
        if (action != null && !differs(action)) { return; }

        String itemStackJson = serialize(wings);
        writer.addString(itemStackJson);
    }

    @Override
    public Result execute(PlayingContext ctx) {
        if (!(ctx.entity instanceof LivingEntity)) {
            return Result.IGNORED;
        }

        LivingEntity entity = (LivingEntity) ctx.entity;
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
        }

        return Result.OK;
    }
}
