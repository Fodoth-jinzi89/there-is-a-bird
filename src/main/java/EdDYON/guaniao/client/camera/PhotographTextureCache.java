package EdDYON.guaniao.client.camera;

import EdDYON.guaniao.GuaniaoMod;
import EdDYON.guaniao.content.camera.PhotographData;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class PhotographTextureCache {
    private static final ResourceLocation FALLBACK = new ResourceLocation(GuaniaoMod.MOD_ID, "textures/item/photograph.png");
    private static final Map<String, ResourceLocation> TEXTURES = new HashMap<>();

    private PhotographTextureCache() {
    }

    public static ResourceLocation textureFor(ItemStack stack) {
        try {
            if (!PhotographData.hasImage(stack)) {
                return FALLBACK;
            }

            int[] pixels = PhotographData.pixels(stack);
            if (pixels.length != PhotographData.IMAGE_SIZE * PhotographData.IMAGE_SIZE) {
                return FALLBACK;
            }

            String key = safe(PhotographData.id(stack)) + "_" + Integer.toUnsignedString(Arrays.hashCode(pixels));
            return TEXTURES.computeIfAbsent(key, ignored -> {
                NativeImage image = toNativeImage(pixels);
                DynamicTexture texture = new DynamicTexture(image);
                texture.upload();
                return Minecraft.getInstance().getTextureManager().register("guaniao_photo/" + key, texture);
            });
        } catch (RuntimeException exception) {
            return FALLBACK;
        }
    }

    public static Path export(ItemStack stack) throws IOException {
        if (!PhotographData.hasImage(stack)) {
            throw new IOException("Photograph has no pixel data.");
        }

        Path directory = Minecraft.getInstance().gameDirectory.toPath().resolve("guaniao_photos");
        Files.createDirectories(directory);
        Path file = directory.resolve(safe(PhotographData.id(stack)) + ".png");
        try (NativeImage image = toNativeImage(PhotographData.pixels(stack))) {
            image.writeToFile(file);
        }
        return file;
    }

    private static NativeImage toNativeImage(int[] pixels) {
        NativeImage image = new NativeImage(PhotographData.IMAGE_SIZE, PhotographData.IMAGE_SIZE, false);
        for (int y = 0; y < PhotographData.IMAGE_SIZE; y++) {
            for (int x = 0; x < PhotographData.IMAGE_SIZE; x++) {
                image.setPixelRGBA(x, y, pixels[y * PhotographData.IMAGE_SIZE + x]);
            }
        }
        return image;
    }

    private static String safe(String id) {
        String safe = id.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
        return safe.isEmpty() ? "photograph" : safe;
    }
}
