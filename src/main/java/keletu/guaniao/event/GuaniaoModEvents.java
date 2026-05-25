/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.SpawnPlacements$Type
 *  net.minecraft.world.item.CreativeModeTabs
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraftforge.event.BuildCreativeModeTabContentsEvent
 *  net.minecraftforge.event.entity.EntityAttributeCreationEvent
 *  net.minecraftforge.event.entity.SpawnPlacementRegisterEvent
 *  net.minecraftforge.event.entity.SpawnPlacementRegisterEvent$Operation
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package keletu.guaniao.event;

import keletu.guaniao.content.bird.nightheron.NightHeronEntity;
import keletu.guaniao.content.bird.sparrow.SparrowEntity;
import keletu.guaniao.registry.GuaniaoEntityTypes;
import keletu.guaniao.registry.GuaniaoItems;
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
    }

    @SubscribeEvent
    public static void onRegisterSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register((EntityType)GuaniaoEntityTypes.NIGHT_HERON.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NightHeronEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register((EntityType)GuaniaoEntityTypes.SPARROW.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SparrowEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (CreativeModeTabs.SPAWN_EGGS.equals((Object)event.getTabKey())) {
            event.accept((ItemLike)GuaniaoItems.NIGHT_HERON_SPAWN_EGG.get());
            event.accept((ItemLike)GuaniaoItems.SPARROW_SPAWN_EGG.get());
        }
    }
}
