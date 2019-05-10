package com.vortexel.dangerzone.common.trade;

import com.google.gson.*;
import com.vortexel.dangerzone.common.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.val;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

import java.lang.reflect.Type;

@AllArgsConstructor
public class Offer {

    private static final String KEY_COST = "cost";
    private static final String KEY_COUNT = "count";
    private static final String KEY_META = "meta";
    private static final String KEY_ITEM = "item";
    private static final String KEY_TAG = "tag";

    public final int cost;
    public final ItemStack stack;

    public static class Serializer implements JsonSerializer<Offer>, JsonDeserializer<Offer> {
        @Override
        public JsonElement serialize(Offer src, Type typeOfSrc, JsonSerializationContext context) {
            val obj = new JsonObject();
            obj.addProperty(KEY_ITEM, src.stack.getItem().getRegistryName().toString());
            obj.addProperty(KEY_COUNT, src.stack.getCount());
            if (src.stack.getItemDamage() != 0) {
                obj.addProperty(KEY_META, src.stack.getItemDamage());
            }
            if (src.stack.getTagCompound() != null) {
                obj.add(KEY_TAG, JsonUtil.nbtToJson(src.stack.getTagCompound()));
            }
            obj.addProperty(KEY_COST, src.cost);
            return obj;
        }

        @Override
        public Offer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                val obj = json.getAsJsonObject();
                val item = obj.getAsJsonPrimitive(KEY_ITEM).getAsString();
                val count = JsonUtil.getOrDefault(obj, KEY_COUNT, 1);
                val damage = JsonUtil.getOrDefault(obj, KEY_META, 0);
                NBTTagCompound tag = null;
                if (obj.has(KEY_TAG)) {
                    tag = JsonToNBT.getTagFromJson(obj.get(KEY_TAG).toString());
                }
                val cost = obj.getAsJsonPrimitive(KEY_COST).getAsInt();
                val stack = new ItemStack(Item.getByNameOrId(item), count, damage);
                if (tag != null) {
                    stack.setTagCompound(tag);
                }
                return new Offer(cost, stack);
            } catch (NBTException e) {
                throw new JsonParseException(e);
            }
        }
    }
}
