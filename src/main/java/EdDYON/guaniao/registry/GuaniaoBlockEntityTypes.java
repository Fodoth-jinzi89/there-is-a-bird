package EdDYON.guaniao.registry;

import EdDYON.guaniao.GuaniaoMod;
import EdDYON.guaniao.content.cage.BirdCageBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class GuaniaoBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, GuaniaoMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<BirdCageBlockEntity>> BIRD_CAGE = BLOCK_ENTITY_TYPES.register("bird_cage", () ->
            BlockEntityType.Builder.of(BirdCageBlockEntity::new,
                    GuaniaoBlocks.SMALL_BIRD_CAGE.get(),
                    GuaniaoBlocks.MEDIUM_BIRD_CAGE.get(),
                    GuaniaoBlocks.LARGE_BIRD_CAGE.get()).build(null));

    private GuaniaoBlockEntityTypes() {
    }
}
