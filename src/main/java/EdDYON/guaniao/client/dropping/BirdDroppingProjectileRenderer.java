package EdDYON.guaniao.client.dropping;

import EdDYON.guaniao.content.dropping.BirdDroppingProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BirdDroppingProjectileRenderer extends GeoEntityRenderer<BirdDroppingProjectileEntity> {
    public BirdDroppingProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BirdDroppingProjectileModel());
        this.shadowRadius = 0.03F;
    }
}
