package net.avizvul.esquissemod.network;

import net.avizvul.esquissemod.EsquisseMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChangeColorPayload(int colorIndex) implements CustomPacketPayload {
    public static final Type<ChangeColorPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "change_color"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeColorPayload> STREAM_CODEC = StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.INT, ChangeColorPayload::colorIndex,
            ChangeColorPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
