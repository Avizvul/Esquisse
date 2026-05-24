package net.avizvul.esquissemod.network;

import net.avizvul.esquissemod.EsquisseMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TearPagePayload(int pageIndex) implements CustomPacketPayload {
    public static final Type<TearPagePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "tear_page"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TearPagePayload> STREAM_CODEC = StreamCodec.ofMember(
            TearPagePayload::write,
            TearPagePayload::new
    );

    public TearPagePayload(RegistryFriendlyByteBuf buf) {
        this(buf.readInt());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.pageIndex);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}