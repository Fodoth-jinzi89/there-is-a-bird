package EdDYON.guaniao.network;

import EdDYON.guaniao.GuaniaoMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class GuaniaoNetwork {
    private static final String PROTOCOL = "1";
    private static int packetId;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(GuaniaoMod.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    private GuaniaoNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(PhotographTakenPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PhotographTakenPacket::encode)
                .decoder(PhotographTakenPacket::decode)
                .consumerMainThread(PhotographTakenPacket::handle)
                .add();
    }
}
