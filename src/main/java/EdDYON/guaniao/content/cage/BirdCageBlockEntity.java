package EdDYON.guaniao.content.cage;

import EdDYON.guaniao.registry.GuaniaoBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BirdCageBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public BirdCageBlockEntity(BlockPos pos, BlockState state) {
        super(GuaniaoBlockEntityTypes.BIRD_CAGE.get(), pos, state);
    }

    public BirdCageVariant variant() {
        if (this.getBlockState().getBlock() instanceof BirdCageBlock birdCageBlock) {
            return birdCageBlock.variant();
        }
        return BirdCageVariant.SMALL;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }
}
