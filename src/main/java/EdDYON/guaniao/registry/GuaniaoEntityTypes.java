package EdDYON.guaniao.registry;

import EdDYON.guaniao.content.bird.budgerigar.BudgerigarDefinition;
import EdDYON.guaniao.content.bird.budgerigar.BudgerigarEntity;
import EdDYON.guaniao.content.bird.columbid.PigeonDefinition;
import EdDYON.guaniao.content.bird.columbid.PigeonEntity;
import EdDYON.guaniao.content.bird.columbid.SpottedDoveDefinition;
import EdDYON.guaniao.content.bird.columbid.SpottedDoveEntity;
import EdDYON.guaniao.content.bird.crow.CrowDefinition;
import EdDYON.guaniao.content.bird.crow.CrowEntity;
import EdDYON.guaniao.content.bird.nightheron.NightHeronEntity;
import EdDYON.guaniao.content.bird.sparrow.SparrowDefinition;
import EdDYON.guaniao.content.bird.sparrow.SparrowEntity;
import EdDYON.guaniao.content.camera.PhotographEntity;
import EdDYON.guaniao.content.dropping.BirdDroppingProjectileEntity;
import EdDYON.guaniao.content.dropping.BirdDroppingSplatEntity;
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
    public static final RegistryObject<EntityType<BudgerigarEntity>> BUDGERIGAR = GuaniaoEntityTypes.registerCreature(BudgerigarDefinition.ENTITY_ID, BudgerigarEntity::new, BudgerigarDefinition.WIDTH, BudgerigarDefinition.HEIGHT);
    public static final RegistryObject<EntityType<SpottedDoveEntity>> SPOTTED_DOVE = GuaniaoEntityTypes.registerCreature(SpottedDoveDefinition.ENTITY_ID, SpottedDoveEntity::new, SpottedDoveDefinition.WIDTH, SpottedDoveDefinition.HEIGHT);
    public static final RegistryObject<EntityType<PigeonEntity>> PIGEON = GuaniaoEntityTypes.registerCreature(PigeonDefinition.ENTITY_ID, PigeonEntity::new, PigeonDefinition.WIDTH, PigeonDefinition.HEIGHT);
    public static final RegistryObject<EntityType<CrowEntity>> CROW = GuaniaoEntityTypes.registerCreature(CrowDefinition.ENTITY_ID, CrowEntity::new, CrowDefinition.WIDTH, CrowDefinition.HEIGHT);
    public static final RegistryObject<EntityType<PhotographEntity>> PHOTOGRAPH = ENTITY_TYPES.register("photograph", () ->
            EntityType.Builder.<PhotographEntity>of(PhotographEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .build(new ResourceLocation("guaniao", "photograph").toString()));
    public static final RegistryObject<EntityType<BirdDroppingProjectileEntity>> BIRD_DROPPING_PROJECTILE = ENTITY_TYPES.register("bird_dropping_projectile", () ->
            EntityType.Builder.<BirdDroppingProjectileEntity>of(BirdDroppingProjectileEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(new ResourceLocation("guaniao", "bird_dropping_projectile").toString()));
    public static final RegistryObject<EntityType<BirdDroppingSplatEntity>> BIRD_DROPPING_SPLAT = ENTITY_TYPES.register("bird_dropping_splat", () ->
            EntityType.Builder.<BirdDroppingSplatEntity>of(BirdDroppingSplatEntity::new, MobCategory.MISC)
                    .sized(0.45F, 0.12F)
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .build(new ResourceLocation("guaniao", "bird_dropping_splat").toString()));

    private GuaniaoEntityTypes() {
    }

    private static <T extends Mob> RegistryObject<EntityType<T>> registerCreature(String id, EntityType.EntityFactory<T> factory, float width, float height) {
        return ENTITY_TYPES.register(id, () -> EntityType.Builder.of((EntityType.EntityFactory)factory, (MobCategory)MobCategory.CREATURE).sized(width, height).clientTrackingRange(8).build(new ResourceLocation("guaniao", id).toString()));
    }
}
