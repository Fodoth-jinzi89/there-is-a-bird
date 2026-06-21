package EdDYON.guaniao.content.camera;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class PhotographData {
    public static final int IMAGE_SIZE = 128;
    public static final String TAG_PHOTO_ID = "PhotoId";
    public static final String TAG_PHOTOGRAPHER = "Photographer";
    public static final String TAG_PHOTOGRAPHER_ID = "PhotographerId";
    public static final String TAG_GAME_TIME = "GameTime";
    public static final String TAG_WIDTH = "Width";
    public static final String TAG_HEIGHT = "Height";
    public static final String TAG_PIXELS = "Pixels";

    private PhotographData() {
    }

    public static boolean hasImage(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null
                && tag.contains(TAG_PHOTO_ID)
                && tag.contains(TAG_PIXELS)
                && tag.getIntArray(TAG_PIXELS).length == IMAGE_SIZE * IMAGE_SIZE;
    }

    public static String id(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? "" : tag.getString(TAG_PHOTO_ID);
    }

    public static String photographer(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? "" : tag.getString(TAG_PHOTOGRAPHER);
    }

    public static int[] pixels(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? new int[0] : tag.getIntArray(TAG_PIXELS);
    }

    public static void write(ItemStack stack, String id, String photographer, UUID photographerId, long gameTime, int[] pixels) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_PHOTO_ID, id);
        tag.putString(TAG_PHOTOGRAPHER, photographer);
        tag.putUUID(TAG_PHOTOGRAPHER_ID, photographerId);
        tag.putLong(TAG_GAME_TIME, gameTime);
        tag.putInt(TAG_WIDTH, IMAGE_SIZE);
        tag.putInt(TAG_HEIGHT, IMAGE_SIZE);
        tag.putIntArray(TAG_PIXELS, pixels);
    }

    public static void copyImage(ItemStack from, ItemStack to) {
        CompoundTag source = from.getTag();
        if (source == null) {
            return;
        }

        CompoundTag target = to.getOrCreateTag();
        if (source.contains(TAG_PHOTO_ID)) {
            target.putString(TAG_PHOTO_ID, source.getString(TAG_PHOTO_ID));
        }
        if (source.contains(TAG_PHOTOGRAPHER)) {
            target.putString(TAG_PHOTOGRAPHER, source.getString(TAG_PHOTOGRAPHER));
        }
        if (source.hasUUID(TAG_PHOTOGRAPHER_ID)) {
            target.putUUID(TAG_PHOTOGRAPHER_ID, source.getUUID(TAG_PHOTOGRAPHER_ID));
        }
        if (source.contains(TAG_GAME_TIME)) {
            target.putLong(TAG_GAME_TIME, source.getLong(TAG_GAME_TIME));
        }
        target.putInt(TAG_WIDTH, IMAGE_SIZE);
        target.putInt(TAG_HEIGHT, IMAGE_SIZE);
        if (source.contains(TAG_PIXELS)) {
            target.putIntArray(TAG_PIXELS, source.getIntArray(TAG_PIXELS));
        }
    }
}
