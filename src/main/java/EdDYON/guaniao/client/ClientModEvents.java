package EdDYON.guaniao.client;

import EdDYON.guaniao.client.bath.BirdBathRenderer;
import EdDYON.guaniao.client.camera.PhotographEntityRenderer;
import EdDYON.guaniao.client.cage.BirdCageRenderer;
import EdDYON.guaniao.client.entity.budgerigar.BudgerigarRenderer;
import EdDYON.guaniao.client.entity.columbid.PigeonRenderer;
import EdDYON.guaniao.client.entity.columbid.SpottedDoveRenderer;
import EdDYON.guaniao.client.entity.nightheron.NightHeronRenderer;
import EdDYON.guaniao.client.entity.sparrow.SparrowRenderer;
import EdDYON.guaniao.registry.GuaniaoBlockEntityTypes;
import EdDYON.guaniao.registry.GuaniaoBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import EdDYON.guaniao.registry.GuaniaoEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid="guaniao", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer((EntityType)GuaniaoEntityTypes.NIGHT_HERON.get(), NightHeronRenderer::new);
        event.registerEntityRenderer((EntityType)GuaniaoEntityTypes.SPARROW.get(), SparrowRenderer::new);
        event.registerEntityRenderer((EntityType)GuaniaoEntityTypes.BUDGERIGAR.get(), BudgerigarRenderer::new);
        event.registerEntityRenderer((EntityType)GuaniaoEntityTypes.SPOTTED_DOVE.get(), SpottedDoveRenderer::new);
        event.registerEntityRenderer((EntityType)GuaniaoEntityTypes.PIGEON.get(), PigeonRenderer::new);
        event.registerEntityRenderer((EntityType)GuaniaoEntityTypes.PHOTOGRAPH.get(), PhotographEntityRenderer::new);
        event.registerBlockEntityRenderer(GuaniaoBlockEntityTypes.BIRD_CAGE.get(), BirdCageRenderer::new);
        event.registerBlockEntityRenderer(GuaniaoBlockEntityTypes.BIRD_BATH.get(), BirdBathRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemBlockRenderTypes.setRenderLayer(GuaniaoBlocks.BREADCRUMBS.get(), RenderType.cutout()));
    }
}
