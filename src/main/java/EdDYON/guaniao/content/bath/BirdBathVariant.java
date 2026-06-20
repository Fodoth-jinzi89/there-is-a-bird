package EdDYON.guaniao.content.bath;

import EdDYON.guaniao.GuaniaoMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public enum BirdBathVariant {
    BIRD_BATH("bird_bath", "geo/bird_bath.geo.json", "textures/block/bird_bath.png", "animations/bird_bath.animation.json",
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 24.0D, 15.0D)),
    BIRD_BATH_2("bird_bath_2", "geo/bird_bath_2.geo.json", "textures/block/bird_bath_2.png", "animations/bird_bath_2.animation.json",
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 24.0D, 15.0D));

    private final String id;
    private final ResourceLocation model;
    private final ResourceLocation texture;
    private final ResourceLocation animation;
    private final VoxelShape shape;

    BirdBathVariant(String id, String modelPath, String texturePath, String animationPath, VoxelShape shape) {
        this.id = id;
        this.model = new ResourceLocation(GuaniaoMod.MOD_ID, modelPath);
        this.texture = new ResourceLocation(GuaniaoMod.MOD_ID, texturePath);
        this.animation = new ResourceLocation(GuaniaoMod.MOD_ID, animationPath);
        this.shape = shape;
    }

    public String id() {
        return this.id;
    }

    public ResourceLocation model() {
        return this.model;
    }

    public ResourceLocation texture() {
        return this.texture;
    }

    public ResourceLocation animation() {
        return this.animation;
    }

    public VoxelShape shape() {
        return this.shape;
    }
}
