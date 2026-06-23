package EdDYON.guaniao.content.dropping;

import EdDYON.guaniao.content.dropping.BirdDroppingProjectileEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BirdDroppingItem extends Item {
    private final BirdDroppingVariant variant;

    public BirdDroppingItem(BirdDroppingVariant variant, Properties properties) {
        super(properties);
        this.variant = variant;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.45F, 0.75F + level.random.nextFloat() * 0.25F);

        if (!level.isClientSide) {
            BirdDroppingProjectileEntity projectile = new BirdDroppingProjectileEntity(level, player, this.variant);
            ItemStack renderStack = stack.copy();
            renderStack.setCount(1);
            projectile.setItem(renderStack);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.85F, 1.0F);
            level.addFreshEntity(projectile);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, 12);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public BirdDroppingVariant variant() {
        return this.variant;
    }
}
