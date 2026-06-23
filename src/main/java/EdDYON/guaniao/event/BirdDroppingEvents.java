package EdDYON.guaniao.event;

import EdDYON.guaniao.content.bird.budgerigar.BudgerigarEntity;
import EdDYON.guaniao.content.bird.columbid.AbstractColumbidEntity;
import EdDYON.guaniao.content.bird.nightheron.NightHeronEntity;
import EdDYON.guaniao.content.bird.sparrow.SparrowEntity;
import EdDYON.guaniao.content.dropping.BirdDroppingProjectileEntity;
import EdDYON.guaniao.content.dropping.BirdDroppingVariant;
import EdDYON.guaniao.registry.GuaniaoEntityTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "guaniao")
public final class BirdDroppingEvents {
    private static final int POOP_CHANCE_PER_TICK = 9000;

    private BirdDroppingEvents() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level) || !isBird(entity) || entity.tickCount < 200) {
            return;
        }
        if (entity.getRandom().nextInt(POOP_CHANCE_PER_TICK) != 0) {
            return;
        }

        BirdDroppingProjectileEntity dropping = new BirdDroppingProjectileEntity(level, entity, BirdDroppingVariant.random(entity.getRandom()));
        Vec3 motion = entity.getDeltaMovement().scale(0.18D).add(0.0D, -0.28D, 0.0D);
        dropping.setPos(entity.getX(), entity.getY(0.2D), entity.getZ());
        dropping.setDeltaMovement(motion);
        level.addFreshEntity(dropping);
    }

    private static boolean isBird(Entity entity) {
        return entity instanceof NightHeronEntity
                || entity instanceof SparrowEntity
                || entity instanceof BudgerigarEntity
                || entity instanceof AbstractColumbidEntity
                || entity.getType() == GuaniaoEntityTypes.NIGHT_HERON.get()
                || entity.getType() == GuaniaoEntityTypes.SPARROW.get()
                || entity.getType() == GuaniaoEntityTypes.BUDGERIGAR.get()
                || entity.getType() == GuaniaoEntityTypes.SPOTTED_DOVE.get()
                || entity.getType() == GuaniaoEntityTypes.PIGEON.get();
    }
}
