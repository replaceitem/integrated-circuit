package net.replaceitem.integratedcircuit.client.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.client.util.InputUtil;

import java.lang.reflect.Type;

public class KeySerializer implements JsonSerializer<InputUtil.Key>, JsonDeserializer<InputUtil.Key> {
    @Override
    public InputUtil.Key deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if(!jsonElement.isJsonObject()) return InputUtil.UNKNOWN_KEY;
        JsonElement codeElement = jsonElement.getAsJsonObject().get("code");
        JsonElement typeElement = jsonElement.getAsJsonObject().get("type");
        if(codeElement == null || !codeElement.isJsonPrimitive()) return InputUtil.UNKNOWN_KEY;
        InputUtil.Type inputType = typeElement != null && typeElement.isJsonPrimitive() ? InputUtil.Type.values()[typeElement.getAsInt()] : InputUtil.Type.KEYSYM;
        return inputType.createFromCode(codeElement.getAsInt());
    }

    @Override
    public JsonElement serialize(InputUtil.Key key, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject object = new JsonObject();
        object.addProperty("code", key.getCode());
        object.addProperty("type", key.getCategory().ordinal());
        return object;
    }
}
