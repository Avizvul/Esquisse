package net.avizvul.esquissemod.network;

import net.avizvul.esquissemod.EsquisseMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ToolSettingsPayload(int toolType, int size, int hardness, int rotation) implements CustomPacketPayload {
    public static final Type<ToolSettingsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "change_tool_settings"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToolSettingsPayload> STREAM_CODEC = StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.INT, ToolSettingsPayload::toolType,
            net.minecraft.network.codec.ByteBufCodecs.INT, ToolSettingsPayload::size,
            net.minecraft.network.codec.ByteBufCodecs.INT, ToolSettingsPayload::hardness,
            net.minecraft.network.codec.ByteBufCodecs.INT, ToolSettingsPayload::rotation,
            ToolSettingsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
