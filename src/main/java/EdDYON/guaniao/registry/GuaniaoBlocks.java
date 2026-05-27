package EdDYON.guaniao.registry;

import EdDYON.guaniao.content.feed.BreadcrumbPileBlock;
import EdDYON.guaniao.content.cage.BirdCageBlock;
import EdDYON.guaniao.content.cage.BirdCageVariant;
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
    public static final RegistryObject<Block> SMALL_BIRD_CAGE = registerBirdCage(BirdCageVariant.SMALL);
    public static final RegistryObject<Block> MEDIUM_BIRD_CAGE = registerBirdCage(BirdCageVariant.MEDIUM);
    public static final RegistryObject<Block> LARGE_BIRD_CAGE = registerBirdCage(BirdCageVariant.LARGE);

    private GuaniaoBlocks() {
    }

    private static RegistryObject<Block> registerBirdCage(BirdCageVariant variant) {
        return BLOCKS.register(variant.id(), () -> new BirdCageBlock(variant, BlockBehaviour.Properties.of()
                .strength(1.5f)
                .sound(SoundType.WOOD)
                .noOcclusion()));
    }
}
