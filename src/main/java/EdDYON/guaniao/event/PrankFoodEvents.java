package EdDYON.guaniao.event;

import EdDYON.guaniao.GuaniaoMod;
import EdDYON.guaniao.content.dropping.BirdDroppingMessageUtil;
import EdDYON.guaniao.content.dropping.PrankFoodUtil;
import EdDYON.guaniao.registry.GuaniaoSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

public final class PrankFoodEvents {
    private static final double MESSAGE_RADIUS = 48.0D;
    private static final double MESSAGE_RADIUS_SQR = MESSAGE_RADIUS * MESSAGE_RADIUS;

    private PrankFoodEvents() {
    }

    @Mod.EventBusSubscriber(modid = GuaniaoMod.MOD_ID)
    public static final class Common {
        private Common() {
        }

        @SubscribeEvent
        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            ItemStack stack = event.getItemStack();
            Player player = event.getEntity();
            if (!PrankFoodUtil.isPrankFood(stack) || !stack.isEdible() || player.isUsingItem()) {
                return;
            }

            player.startUsingItem(event.getHand());
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onFoodFinished(LivingEntityUseItemEvent.Finish event) {
            ItemStack stack = event.getItem();
            if (!PrankFoodUtil.isPrankFood(stack)) {
                return;
            }

            LivingEntity entity = event.getEntity();
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
            entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 160, 0));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));

            if (!(entity.level() instanceof ServerLevel level)) {
                return;
            }

            RandomSource random = entity.getRandom();
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SLIME_BLOCK_HIT, SoundSource.PLAYERS, 0.75F, 0.7F + random.nextFloat() * 0.25F);
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.6F, 0.75F + random.nextFloat() * 0.35F);
            level.sendParticles(ParticleTypes.POOF, entity.getX(), entity.getY(0.65D), entity.getZ(), 8, 0.35D, 0.25D, 0.35D, 0.02D);
            level.sendParticles(ParticleTypes.SPLASH, entity.getX(), entity.getY(0.6D), entity.getZ(), 5, 0.2D, 0.12D, 0.2D, 0.01D);

            playLaughingBirds(level, entity, random);

            if (entity instanceof Player player) {
                broadcastNearby(level, player);
                player.displayClientMessage(BirdDroppingMessageUtil.randomComponent("message.guaniao.prank_food_review", random), true);
            }
        }

        private static void playLaughingBirds(ServerLevel level, LivingEntity entity, RandomSource random) {
            SoundEvent[] sounds = new SoundEvent[] {
                    GuaniaoSoundEvents.SPARROW_AMBIENT.get(),
                    GuaniaoSoundEvents.BUDGERIGAR_AMBIENT.get(),
                    GuaniaoSoundEvents.PIGEON_AMBIENT.get()
            };
            int count = 2 + random.nextInt(2);
            for (int i = 0; i < count; i++) {
                SoundEvent sound = sounds[random.nextInt(sounds.length)];
                double x = entity.getX() + (random.nextDouble() - 0.5D) * 4.0D;
                double y = entity.getY(0.6D) + random.nextDouble() * 1.4D;
                double z = entity.getZ() + (random.nextDouble() - 0.5D) * 4.0D;
                level.playSound(null, x, y, z, sound, SoundSource.AMBIENT, 0.65F, 0.85F + random.nextFloat() * 0.45F);
            }
        }

        private static void broadcastNearby(ServerLevel level, Player eater) {
            Component message = Component.translatable("message.guaniao.prank_food_eaten", eater.getDisplayName());
            for (ServerPlayer nearby : level.players()) {
                if (nearby.distanceToSqr(eater) <= MESSAGE_RADIUS_SQR) {
                    nearby.sendSystemMessage(message);
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = GuaniaoMod.MOD_ID, value = Dist.CLIENT)
    public static final class Client {
        private Client() {
        }

        @SubscribeEvent
        public static void onTooltip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            if (!PrankFoodUtil.isPrankFood(stack)) {
                return;
            }

            List<Component> tooltip = event.getToolTip();
            if (!tooltip.isEmpty()) {
                tooltip.set(0, PrankFoodUtil.storedPrankDisplayName(stack, tooltip.get(0)));
            }
            if (Screen.hasShiftDown()) {
                tooltip.add(BirdDroppingMessageUtil.stableTooltip("tooltip.guaniao.prank_food_suspicious", stack).copy().withStyle(ChatFormatting.GRAY));
            }
        }

        @SubscribeEvent
        public static void onDroppingTooltip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            if (!PrankFoodUtil.isDropping(stack)) {
                return;
            }

            event.getToolTip().add(Component.translatable("tooltip.guaniao.bird_dropping_fertilizer").withStyle(ChatFormatting.GRAY));
            if (Screen.hasShiftDown()) {
                event.getToolTip().add(BirdDroppingMessageUtil.stableTooltip("tooltip.guaniao.dropping_evidence", stack).copy().withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
