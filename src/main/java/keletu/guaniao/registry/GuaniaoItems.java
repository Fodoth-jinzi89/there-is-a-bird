/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraftforge.common.ForgeSpawnEggItem
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package keletu.guaniao.registry;

import java.util.function.Supplier;
import keletu.guaniao.content.guide.BirdGuideItem;
import keletu.guaniao.content.bird.sparrow.SparrowDefinition;
import keletu.guaniao.content.feed.BreadcrumbItem;
import keletu.guaniao.registry.GuaniaoEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class GuaniaoItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.ITEMS, (String)"guaniao");
    public static final RegistryObject<Item> NIGHT_HERON_SPAWN_EGG = GuaniaoItems.registerSpawnEgg("night_heron_spawn_egg", GuaniaoEntityTypes.NIGHT_HERON, 6121331, 14198125);
    public static final RegistryObject<Item> SPARROW_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(SparrowDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.SPARROW, SparrowDefinition.SPAWN_EGG_BASE_COLOR, SparrowDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final RegistryObject<Item> BREADCRUMBS = ITEMS.register("breadcrumbs", () -> new BreadcrumbItem(new Item.Properties()));
    public static final RegistryObject<Item> BIRD_GUIDE = ITEMS.register("bird_guide", () -> new BirdGuideItem(new Item.Properties().stacksTo(1)));

    private GuaniaoItems() {
    }

    private static RegistryObject<Item> registerSpawnEgg(String id, Supplier<? extends EntityType<? extends Mob>> entityTypeSupplier, int baseColor, int spotColor) {
        return ITEMS.register(id, () -> new ForgeSpawnEggItem(entityTypeSupplier, baseColor, spotColor, new Item.Properties()));
    }
}
