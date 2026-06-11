package EdDYON.guaniao.event;

import EdDYON.guaniao.content.bird.budgerigar.BudgerigarEntity;
import EdDYON.guaniao.content.bird.columbid.PigeonEntity;
import EdDYON.guaniao.content.bird.columbid.SpottedDoveEntity;
import EdDYON.guaniao.content.bird.nightheron.NightHeronEntity;
import EdDYON.guaniao.content.bird.sparrow.SparrowEntity;
import EdDYON.guaniao.registry.GuaniaoEntityTypes;
import EdDYON.guaniao.registry.GuaniaoItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="guaniao", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class GuaniaoModEvents {
    private GuaniaoModEvents() {
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put((EntityType)GuaniaoEntityTypes.NIGHT_HERON.get(), NightHeronEntity.createAttributes().build());
        event.put((EntityType)GuaniaoEntityTypes.SPARROW.get(), SparrowEntity.createAttributes().build());
        event.put((EntityType)GuaniaoEntityTypes.BUDGERIGAR.get(), BudgerigarEntity.createAttributes().build());
        event.put((EntityType)GuaniaoEntityTypes.SPOTTED_DOVE.get(), SpottedDoveEntity.createAttributes().build());
        event.put((EntityType)GuaniaoEntityTypes.PIGEON.get(), PigeonEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onRegisterSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register((EntityType)GuaniaoEntityTypes.NIGHT_HERON.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NightHeronEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register((EntityType)GuaniaoEntityTypes.SPARROW.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SparrowEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register((EntityType)GuaniaoEntityTypes.BUDGERIGAR.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BudgerigarEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register((EntityType)GuaniaoEntityTypes.SPOTTED_DOVE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpottedDoveEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register((EntityType)GuaniaoEntityTypes.PIGEON.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PigeonEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (CreativeModeTabs.SPAWN_EGGS.equals((Object)event.getTabKey())) {
            event.accept((ItemLike)GuaniaoItems.NIGHT_HERON_SPAWN_EGG.get());
            event.accept((ItemLike)GuaniaoItems.SPARROW_SPAWN_EGG.get());
            event.accept((ItemLike)GuaniaoItems.BUDGERIGAR_SPAWN_EGG.get());
            event.accept((ItemLike)GuaniaoItems.SPOTTED_DOVE_SPAWN_EGG.get());
            event.accept((ItemLike)GuaniaoItems.PIGEON_SPAWN_EGG.get());
        }
    }
}
