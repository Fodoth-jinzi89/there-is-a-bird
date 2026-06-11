package EdDYON.guaniao.client.entity.nightheron;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import EdDYON.guaniao.content.bird.nightheron.NightHeronEntity;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class NightHeronHeldFishLayer extends GeoRenderLayer<NightHeronEntity> {
    private static final Set<String> MOUTH_BONES = Set.of(
            "fish_anchor",
            "mouth_anchor",
            "beak",
            "mouth",
            "bill",
            "head",
            "upper_beak",
            "lower_beak");

    public NightHeronHeldFishLayer(GeoRenderer<NightHeronEntity> renderer) {
        super(renderer);
    }

    @Override
    public void renderForBone(PoseStack poseStack, NightHeronEntity nightHeron, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        ItemStack stack = nightHeron.getHeldFishForRendering();
        if (!MOUTH_BONES.contains(bone.getName()) || stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0, -0.03, -0.42);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        poseStack.scale(0.45f, 0.45f, 0.45f);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, nightHeron.level(), nightHeron.getId());
        poseStack.popPose();
    }
}
