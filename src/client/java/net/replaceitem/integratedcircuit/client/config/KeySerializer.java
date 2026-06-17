package net.replaceitem.integratedcircuit.client.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.blaze3d.platform.InputConstants;
import java.lang.reflect.Type;

public class KeySerializer implements JsonSerializer<InputConstants.Key>, JsonDeserializer<InputConstants.Key> {
    @Override
    public InputConstants.Key deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if(!jsonElement.isJsonObject()) return InputConstants.UNKNOWN;
        JsonElement codeElement = jsonElement.getAsJsonObject().get("code");
        JsonElement typeElement = jsonElement.getAsJsonObject().get("type");
        if(codeElement == null || !codeElement.isJsonPrimitive()) return InputConstants.UNKNOWN;
        InputConstants.Type inputType = typeElement != null && typeElement.isJsonPrimitive() ? InputConstants.Type.values()[typeElement.getAsInt()] : InputConstants.Type.KEYSYM;
        return inputType.getOrCreate(codeElement.getAsInt());
    }

    @Override
    public JsonElement serialize(InputConstants.Key key, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject object = new JsonObject();
        object.addProperty("code", key.getValue());
        object.addProperty("type", key.getType().ordinal());
        return object;
    }
}
