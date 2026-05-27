package net.avizvul.esquissemod.block.entity;

import net.avizvul.esquissemod.component.SketchData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SketchedPageBlockEntity extends BlockEntity {
    private SketchData sketchData = SketchData.fromArray(new byte[][]{});
    private int rotation = 0; // Переменная вращения

    public SketchedPageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SKETCHED_PAGE_BE.get(), pos, state);
    }

    public void setSketchData(SketchData data) {
        this.sketchData = data;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public SketchData getSketchData() {
        return sketchData;
    }

    public int getRotation() {
        return this.rotation;
    }

    public void setRotation(int rot) {
        this.rotation = rot % 4;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Rotation", this.rotation); // Сохраняем вращение

        SketchData.CODEC.encodeStart(NbtOps.INSTANCE, this.sketchData)
                .resultOrPartial()
                .ifPresent(encoded -> tag.put("SketchData", encoded));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.rotation = tag.getInt("Rotation"); // Загружаем вращение

        if (tag.contains("SketchData")) {
            SketchData.CODEC.parse(NbtOps.INSTANCE, tag.get("SketchData"))
                    .resultOrPartial()
                    .ifPresent(decoded -> this.sketchData = decoded);
        }
    }

    // Правильная реализация синхронизации NBT для 1.21
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Получаем базовый тег со всеми метаданными от родительского класса
        CompoundTag tag = super.getUpdateTag(registries);
        // Записываем наши пиксели и угол поворота
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}