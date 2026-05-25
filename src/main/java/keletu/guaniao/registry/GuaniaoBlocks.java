package keletu.guaniao.registry;

import keletu.guaniao.content.feed.BreadcrumbPileBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class GuaniaoBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "guaniao");
    public static final RegistryObject<Block> BREADCRUMBS = BLOCKS.register("breadcrumbs", () ->
            new BreadcrumbPileBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.SAND)
                    .randomTicks()));

    private GuaniaoBlocks() {
    }
}
