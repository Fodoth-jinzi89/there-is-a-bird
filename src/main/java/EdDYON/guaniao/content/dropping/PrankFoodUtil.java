package EdDYON.guaniao.content.dropping;

import EdDYON.guaniao.registry.GuaniaoItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PrankFoodUtil {
    public static final String TAG_PRANK_FOOD = "GuaniaoPrankFood";
    public static final String TAG_ORIGINAL_ITEM = "GuaniaoOriginalItem";
    public static final String TAG_DROPPING_VARIANT = "GuaniaoDroppingVariant";
    public static final String TAG_PRANK_NAME = "GuaniaoPrankName";

    private static final Map<String, String> ZH_PHRASE_REPLACEMENTS = new LinkedHashMap<>();
    private static final Map<Character, Character> ZH_CORE_REPLACEMENTS = new LinkedHashMap<>();
    private static final Map<Character, Character> ZH_MODIFIER_REPLACEMENTS = new LinkedHashMap<>();
    private static final Map<String, String> EN_REPLACEMENTS = new LinkedHashMap<>();

    static {
        ZH_PHRASE_REPLACEMENTS.put("\u9644\u9B54\u91D1\u82F9\u679C", "\u9644\u9B54\u5168\u82F9\u545C");
        ZH_PHRASE_REPLACEMENTS.put("\u91D1\u82F9\u679C", "\u5168\u82F9\u545C");
        ZH_PHRASE_REPLACEMENTS.put("\u5357\u74DC\u6D3E", "\u5357\u722A\u6CE5\u6D3E");

        ZH_CORE_REPLACEMENTS.put('\u8089', '\u5185');
        ZH_CORE_REPLACEMENTS.put('\u9E21', '\u53FD');
        ZH_CORE_REPLACEMENTS.put('\u9E2D', '\u5440');
        ZH_CORE_REPLACEMENTS.put('\u9E45', '\u997F');
        ZH_CORE_REPLACEMENTS.put('\u5154', '\u514D');
        ZH_CORE_REPLACEMENTS.put('\u9C7C', '\u6E14');
        ZH_CORE_REPLACEMENTS.put('\u867E', '\u5413');
        ZH_CORE_REPLACEMENTS.put('\u86CB', '\u866B');
        ZH_CORE_REPLACEMENTS.put('\u5305', '\u53E5');
        ZH_CORE_REPLACEMENTS.put('\u996D', '\u53CD');
        ZH_CORE_REPLACEMENTS.put('\u997C', '\u5E76');
        ZH_CORE_REPLACEMENTS.put('\u7CD5', '\u7F94');
        ZH_CORE_REPLACEMENTS.put('\u83DC', '\u8388');
        ZH_CORE_REPLACEMENTS.put('\u679C', '\u545C');
        ZH_CORE_REPLACEMENTS.put('\u74DC', '\u722A');
        ZH_CORE_REPLACEMENTS.put('\u85AF', '\u6691');

        ZH_MODIFIER_REPLACEMENTS.put('\u719F', '\u5B70');
        ZH_MODIFIER_REPLACEMENTS.put('\u70E4', '\u62F7');
        ZH_MODIFIER_REPLACEMENTS.put('\u751C', '\u7518');
        ZH_MODIFIER_REPLACEMENTS.put('\u8150', '\u5E9C');
        ZH_MODIFIER_REPLACEMENTS.put('\u5976', '\u4E43');
        ZH_MODIFIER_REPLACEMENTS.put('\u871C', '\u5BC6');
        ZH_MODIFIER_REPLACEMENTS.put('\u6C64', '\u70EB');
        ZH_MODIFIER_REPLACEMENTS.put('\u6839', '\u8DCB');

        EN_REPLACEMENTS.put("Cooked Beef", "Cooked Beaf");
        EN_REPLACEMENTS.put("Beef", "Beaf");
        EN_REPLACEMENTS.put("Cooked Porkchop", "Cooked Porkslop");
        EN_REPLACEMENTS.put("Porkchop", "Porkslop");
        EN_REPLACEMENTS.put("Cooked Chicken", "Cooked Chickin");
        EN_REPLACEMENTS.put("Chicken", "Chickin");
        EN_REPLACEMENTS.put("Cooked Mutton", "Cooked Muton");
        EN_REPLACEMENTS.put("Mutton", "Muton");
        EN_REPLACEMENTS.put("Cooked Rabbit", "Cooked Rabbut");
        EN_REPLACEMENTS.put("Rabbit Stew", "Rabbut Stew");
        EN_REPLACEMENTS.put("Rabbit", "Rabbut");
        EN_REPLACEMENTS.put("Bread", "Bred");
        EN_REPLACEMENTS.put("Cookie", "Cookle");
        EN_REPLACEMENTS.put("Cake", "Cacke");
        EN_REPLACEMENTS.put("Pumpkin Pie", "Pumpkin Poo");
        EN_REPLACEMENTS.put("Enchanted Golden Apple", "Enchanted Golden Appel");
        EN_REPLACEMENTS.put("Golden Apple", "Golden Appel");
        EN_REPLACEMENTS.put("Apple", "Appel");
        EN_REPLACEMENTS.put("Beetroot Soup", "Beetrot Soup");
        EN_REPLACEMENTS.put("Beetroot", "Beetrot");
        EN_REPLACEMENTS.put("Sweet Berries", "Sweet Berris");
        EN_REPLACEMENTS.put("Glow Berries", "Glow Berris");
        EN_REPLACEMENTS.put("Rotten Flesh", "Rotten Flesch");
    }

    private PrankFoodUtil() {
    }

    public static boolean isPrankFood(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return !stack.isEmpty() && tag != null && tag.getBoolean(TAG_PRANK_FOOD);
    }

    public static boolean isDropping(ItemStack stack) {
        return !stack.isEmpty()
                && (stack.is(GuaniaoItems.BIRD_DROPPING_1.get())
                || stack.is(GuaniaoItems.BIRD_DROPPING_2.get())
                || stack.is(GuaniaoItems.BIRD_DROPPING_3.get())
                || stack.is(GuaniaoItems.BIRD_DROPPING_4.get()));
    }

    public static int droppingVariant(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BirdDroppingItem droppingItem) {
            return droppingItem.variant().id();
        }
        if (stack.is(GuaniaoItems.BIRD_DROPPING_2.get())) {
            return BirdDroppingVariant.TWO.id();
        }
        if (stack.is(GuaniaoItems.BIRD_DROPPING_3.get())) {
            return BirdDroppingVariant.THREE.id();
        }
        if (stack.is(GuaniaoItems.BIRD_DROPPING_4.get())) {
            return BirdDroppingVariant.FOUR.id();
        }
        return BirdDroppingVariant.ONE.id();
    }

    public static boolean isEligibleFood(ItemStack stack) {
        return !stack.isEmpty() && stack.isEdible() && !isPrankFood(stack) && !isDropping(stack);
    }

    public static ItemStack makePrankFood(ItemStack food, ItemStack dropping) {
        if (!isEligibleFood(food) || !isDropping(dropping)) {
            return ItemStack.EMPTY;
        }

        ItemStack result = food.copy();
        result.setCount(1);
        ResourceLocation originalItem = ForgeRegistries.ITEMS.getKey(food.getItem());
        Component prankName = makePrankDisplayName(food.getHoverName());

        CompoundTag tag = result.getOrCreateTag();
        tag.putBoolean(TAG_PRANK_FOOD, true);
        tag.putString(TAG_ORIGINAL_ITEM, originalItem == null ? "unknown" : originalItem.toString());
        tag.putInt(TAG_DROPPING_VARIANT, droppingVariant(dropping));
        tag.putString(TAG_PRANK_NAME, prankName.getString());

        result.setHoverName(prankName);
        return result;
    }

    public static Component makePrankDisplayName(Component originalName) {
        String plainName = originalName.getString();
        String prankName = containsChinese(plainName) ? corruptChineseName(plainName) : corruptEnglishName(plainName);
        return Component.literal(prankName).withStyle(originalName.getStyle());
    }

    public static Component storedPrankDisplayName(ItemStack stack, Component fallbackOriginalName) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_PRANK_NAME)) {
            return Component.literal(tag.getString(TAG_PRANK_NAME)).withStyle(fallbackOriginalName.getStyle());
        }
        return makePrankDisplayName(fallbackOriginalName);
    }

    private static String corruptChineseName(String name) {
        for (Map.Entry<String, String> entry : ZH_PHRASE_REPLACEMENTS.entrySet()) {
            if (name.contains(entry.getKey())) {
                return name.replace(entry.getKey(), entry.getValue());
            }
        }

        int limit = replacementLimit(name.length());
        StringBuilder builder = new StringBuilder(name);
        int replaced = replaceChineseChars(builder, ZH_CORE_REPLACEMENTS, limit, 0);
        replaced = replaceChineseChars(builder, ZH_MODIFIER_REPLACEMENTS, limit, replaced);
        if (replaced > 0) {
            return builder.toString();
        }

        if (name.length() >= 4) {
            int left = Math.max(1, name.length() / 2 - 1);
            int right = left + 1;
            char leftChar = builder.charAt(left);
            builder.setCharAt(left, builder.charAt(right));
            builder.setCharAt(right, leftChar);
            return builder.toString();
        }
        return name + "\uFF1F";
    }

    private static int replaceChineseChars(StringBuilder builder, Map<Character, Character> replacements, int limit, int replaced) {
        for (int i = 0; i < builder.length() && replaced < limit; i++) {
            Character replacement = replacements.get(builder.charAt(i));
            if (replacement != null) {
                builder.setCharAt(i, replacement);
                replaced++;
            }
        }
        return replaced;
    }

    private static int replacementLimit(int length) {
        if (length <= 2) {
            return 1;
        }
        if (length <= 4) {
            return 2;
        }
        return 3;
    }

    private static String corruptEnglishName(String name) {
        String replacement = EN_REPLACEMENTS.get(name);
        return replacement == null ? name + "?" : replacement;
    }

    private static boolean containsChinese(String value) {
        for (int i = 0; i < value.length(); i++) {
            Character.UnicodeScript script = Character.UnicodeScript.of(value.charAt(i));
            if (script == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
    }
}
