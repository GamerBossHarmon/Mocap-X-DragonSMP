package com.mt1006.mocap.mocap.actions;

import bl4ckscor3.mod.sit.SitEntity;
import bl4ckscor3.mod.sit.SitUtil;
import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import static bl4ckscor3.mod.sit.Sit.SIT_ENTITY_TYPE;
import static bl4ckscor3.mod.sit.SitUtil.*;

public class Sit implements ComparableAction {

    private final boolean isSitting;
    private final BlockPos sitPos;

    public Sit(Entity entity) {
        boolean isSittingTemp = false;
        if (entity instanceof ServerPlayer) {
            isSittingTemp = isPlayerSitting((ServerPlayer) entity);
            if (isSittingTemp && entity.getVehicle() != null) {
                this.sitPos = getSitEntity(entity.level, entity.getVehicle().blockPosition()).blockPosition();
            } else {
                this.sitPos = null;
                if (isSittingTemp) {
                    System.out.println("Error, player is sitting but is not riding an entity.\nisSitting: " + isSittingTemp + "\nEntity: " + entity + "\nVehicle: " + entity.getVehicle());
                }
            }
        } else {
            this.sitPos = null;
        }
        if (entity instanceof ServerPlayer) {
            System.out.println("Sit entity: " + entity);
            System.out.println("Sit: " + isSittingTemp);
            System.out.println("Sit player riding entity: " + entity.getVehicle());
            if (entity.getVehicle() != null) {
                System.out.println("Sit vehicle: " + getSitEntity(entity.level, entity.getVehicle().blockPosition()));
            } else {
                System.out.println("Sit vehicle: " + null);
            }
        }
        this.isSitting = isSittingTemp;
    }

    public Sit(RecordingFiles.Reader reader) {
        this.isSitting = reader.readBoolean();
        if (this.isSitting) {
            this.sitPos = new BlockPos(reader.readInt(), reader.readInt(), reader.readInt());
        } else {
            this.sitPos = null;
        }
    }

    @Override
    public boolean differs(ComparableAction action) {
        if (isSitting != ((Sit) action).isSitting || sitPos != ((Sit) action).sitPos) {
            return true;
        }
        return false;
    }

    @Override
    public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action) {
        if (action != null && !differs(action)) {
            return;
        }

        writer.addByte(Type.SIT.id);
        if (isSitting) {
            writer.addBoolean(true);
            writer.addInt(sitPos.getX());
            writer.addInt(sitPos.getY());
            writer.addInt(sitPos.getZ());
        } else {
            writer.addBoolean(false);
        }
    }

    @Override
    public Result execute(PlayingContext ctx) {
        if (!(ctx.entity instanceof ServerPlayer entity)) { return Result.IGNORED; }

        System.out.println("Sit execute: " + isSitting);
        System.out.println("Sit Pos: " + sitPos);

        if (isSitting && sitPos != null) {
            if (getSitEntity(entity.level, sitPos) == null) {
                //entity.startRiding(getSitEntity(entity.level, sitPos));
                SitEntity sit = SIT_ENTITY_TYPE.create(entity.level);
                sit.absMoveTo(sitPos.getX() + 0.5D, sitPos.getY() + 0.25D, sitPos.getZ() + 0.5D);
                if (SitUtil.addSitEntity(entity.level, sitPos, sit, entity.blockPosition())) {
                    entity.level.addFreshEntity(sit);
                    entity.startRiding(sit);
                    //return InteractionResult.SUCCESS;
                }
            }
        } else {
            if (entity.getVehicle() != null) {//The sitEntity is still there got to remove it//NVM it is removed but does not get remove if we stop early
                SitEntity sitEntity = SitUtil.getSitEntity(entity.level, entity.getVehicle().blockPosition());
                if (sitEntity != null) {
                    SitUtil.removeSitEntity(entity.level, sitEntity.blockPosition());
                    sitEntity.ejectPassengers();
                }
            }
        }



        return Result.OK;
    }
}
