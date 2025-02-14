package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class SetScaleData implements ComparableAction {

    private final float baseScale;
    private final float widthScale;
    private final float heightScale;
    private final float modelWidth;
    private final float modelHeight;

    public SetScaleData(Entity entity) {

        ScaleData baseScaleData = ScaleTypes.BASE.getScaleData(entity);
        ScaleData widthScaleData = ScaleTypes.WIDTH.getScaleData(entity);
        ScaleData heightScaleData = ScaleTypes.HEIGHT.getScaleData(entity);
        ScaleData modelWidthData = ScaleTypes.MODEL_WIDTH.getScaleData(entity);
        ScaleData modelHeightData = ScaleTypes.MODEL_HEIGHT.getScaleData(entity);

        this.baseScale = baseScaleData.getBaseScale();
        this.widthScale = widthScaleData.getBaseScale();
        this.heightScale = heightScaleData.getBaseScale();
        this.modelWidth = modelWidthData.getBaseScale();
        this.modelHeight = modelHeightData.getBaseScale();
    }

    public SetScaleData(RecordingFiles.Reader reader) {
        this.baseScale = reader.readFloat();
        this.widthScale = reader.readFloat();
        this.heightScale = reader.readFloat();
        this.modelWidth = reader.readFloat();
        this.modelHeight = reader.readFloat();
    }

    @Override
    public boolean differs(ComparableAction action) {
        if (!(action instanceof SetScaleData other)) {
            return true;
        }
        return this.baseScale != other.baseScale ||
                this.widthScale != other.widthScale ||
                this.heightScale != other.heightScale ||
                this.modelWidth != other.modelWidth ||
                this.modelHeight != other.modelHeight;
    }

    @Override
    public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action) {
        if (action != null && !differs(action)) {
            return;
        }

        writer.addByte(Type.SET_SCALE_DATA.id);
        writer.addFloat(baseScale);
        writer.addFloat(widthScale);
        writer.addFloat(heightScale);
        writer.addFloat(modelWidth);
        writer.addFloat(modelHeight);
    }

    @Override
    public Result execute(PlayingContext ctx) {
        if (ctx.entity == null) {
            return Result.IGNORED;
        }

        ScaleData baseScaleData = ScaleTypes.BASE.getScaleData(ctx.entity);
        ScaleData widthScaleData = ScaleTypes.WIDTH.getScaleData(ctx.entity);
        ScaleData heightScaleData = ScaleTypes.HEIGHT.getScaleData(ctx.entity);
        ScaleData modelWidthData = ScaleTypes.MODEL_WIDTH.getScaleData(ctx.entity);
        ScaleData modelHeightData = ScaleTypes.MODEL_HEIGHT.getScaleData(ctx.entity);

        baseScaleData.setScale(baseScale);
        widthScaleData.setScale(widthScale);
        heightScaleData.setScale(heightScale);
        modelWidthData.setScale(modelWidth);
        modelHeightData.setScale(modelHeight);

        return Result.OK;
    }
}