/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EntityType$Builder
 *  net.minecraft.world.entity.EntityType$EntityFactory
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package keletu.guaniao.registry;

import keletu.guaniao.content.bird.nightheron.NightHeronEntity;
import keletu.guaniao.content.bird.sparrow.SparrowDefinition;
import keletu.guaniao.content.bird.sparrow.SparrowEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class GuaniaoEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create((IForgeRegistry)ForgeRegistries.ENTITY_TYPES, (String)"guaniao");
    public static final RegistryObject<EntityType<NightHeronEntity>> NIGHT_HERON = GuaniaoEntityTypes.registerCreature("night_heron", NightHeronEntity::new, 0.8f, 1.9f);
    public static final RegistryObject<EntityType<SparrowEntity>> SPARROW = GuaniaoEntityTypes.registerCreature(SparrowDefinition.ENTITY_ID, SparrowEntity::new, SparrowDefinition.WIDTH, SparrowDefinition.HEIGHT);

    private GuaniaoEntityTypes() {
    }

    private static <T extends Mob> RegistryObject<EntityType<T>> registerCreature(String id, EntityType.EntityFactory<T> factory, float width, float height) {
        return ENTITY_TYPES.register(id, () -> EntityType.Builder.of((EntityType.EntityFactory)factory, (MobCategory)MobCategory.CREATURE).sized(width, height).clientTrackingRange(8).build(new ResourceLocation("guaniao", id).toString()));
    }
}
