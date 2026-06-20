package EdDYON.guaniao.registry;

import java.util.function.Supplier;
import EdDYON.guaniao.content.guide.BirdGuideItem;
import EdDYON.guaniao.content.bird.budgerigar.BudgerigarDefinition;
import EdDYON.guaniao.content.bird.columbid.PigeonDefinition;
import EdDYON.guaniao.content.bird.columbid.SpottedDoveDefinition;
import EdDYON.guaniao.content.bird.sparrow.SparrowDefinition;
import EdDYON.guaniao.content.bath.BirdBathItem;
import EdDYON.guaniao.content.bath.BirdBathVariant;
import EdDYON.guaniao.content.cage.BirdCageItem;
import EdDYON.guaniao.content.cage.BirdCageVariant;
import EdDYON.guaniao.content.feed.BreadcrumbItem;
import EdDYON.guaniao.registry.GuaniaoEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class GuaniaoItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.ITEMS, (String)"guaniao");
    public static final RegistryObject<Item> NIGHT_HERON_SPAWN_EGG = GuaniaoItems.registerSpawnEgg("night_heron_spawn_egg", GuaniaoEntityTypes.NIGHT_HERON, 6121331, 14198125);
    public static final RegistryObject<Item> SPARROW_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(SparrowDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.SPARROW, SparrowDefinition.SPAWN_EGG_BASE_COLOR, SparrowDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final RegistryObject<Item> BUDGERIGAR_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(BudgerigarDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.BUDGERIGAR, BudgerigarDefinition.SPAWN_EGG_BASE_COLOR, BudgerigarDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final RegistryObject<Item> SPOTTED_DOVE_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(SpottedDoveDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.SPOTTED_DOVE, SpottedDoveDefinition.SPAWN_EGG_BASE_COLOR, SpottedDoveDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final RegistryObject<Item> PIGEON_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(PigeonDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.PIGEON, PigeonDefinition.SPAWN_EGG_BASE_COLOR, PigeonDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final RegistryObject<Item> BREADCRUMBS = ITEMS.register("breadcrumbs", () -> new BreadcrumbItem(new Item.Properties()));
    public static final RegistryObject<Item> BIRD_GUIDE = ITEMS.register("bird_guide", () -> new BirdGuideItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SMALL_BIRD_CAGE = GuaniaoItems.registerBirdCageItem(BirdCageVariant.SMALL, GuaniaoBlocks.SMALL_BIRD_CAGE);
    public static final RegistryObject<Item> MEDIUM_BIRD_CAGE = GuaniaoItems.registerBirdCageItem(BirdCageVariant.MEDIUM, GuaniaoBlocks.MEDIUM_BIRD_CAGE);
    public static final RegistryObject<Item> LARGE_BIRD_CAGE = GuaniaoItems.registerBirdCageItem(BirdCageVariant.LARGE, GuaniaoBlocks.LARGE_BIRD_CAGE);
    public static final RegistryObject<Item> WOODEN_BIRD_BATH = GuaniaoItems.registerBirdBathItem(BirdBathVariant.WOODEN_BIRD_BATH, GuaniaoBlocks.WOODEN_BIRD_BATH);
    public static final RegistryObject<Item> STONE_BIRD_BATH = GuaniaoItems.registerBirdBathItem(BirdBathVariant.STONE_BIRD_BATH, GuaniaoBlocks.STONE_BIRD_BATH);
    public static final RegistryObject<Item> BIRD_BATH = GuaniaoItems.registerBirdBathItem(BirdBathVariant.BIRD_BATH, GuaniaoBlocks.BIRD_BATH);
    public static final RegistryObject<Item> WOODEN_BIRD_BATH_2 = GuaniaoItems.registerBirdBathItem(BirdBathVariant.WOODEN_BIRD_BATH_2, GuaniaoBlocks.WOODEN_BIRD_BATH_2);
    public static final RegistryObject<Item> STONE_BIRD_BATH_2 = GuaniaoItems.registerBirdBathItem(BirdBathVariant.STONE_BIRD_BATH_2, GuaniaoBlocks.STONE_BIRD_BATH_2);
    public static final RegistryObject<Item> BIRD_BATH_2 = GuaniaoItems.registerBirdBathItem(BirdBathVariant.BIRD_BATH_2, GuaniaoBlocks.BIRD_BATH_2);

    private GuaniaoItems() {
    }

    private static RegistryObject<Item> registerSpawnEgg(String id, Supplier<? extends EntityType<? extends Mob>> entityTypeSupplier, int baseColor, int spotColor) {
        return ITEMS.register(id, () -> new ForgeSpawnEggItem(entityTypeSupplier, baseColor, spotColor, new Item.Properties()));
    }

    private static RegistryObject<Item> registerBirdCageItem(BirdCageVariant variant, Supplier<? extends Block> blockSupplier) {
        return ITEMS.register(variant.id(), () -> new BirdCageItem(variant, blockSupplier.get(), new Item.Properties()));
    }

    private static RegistryObject<Item> registerBirdBathItem(BirdBathVariant variant, Supplier<? extends Block> blockSupplier) {
        return ITEMS.register(variant.id(), () -> new BirdBathItem(variant, blockSupplier.get(), new Item.Properties()));
    }
}
