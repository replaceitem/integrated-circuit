package net.replaceitem.integratedcircuit.client.config;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;

import java.lang.reflect.Type;

public class KeySerializer implements JsonSerializer<InputConstants.Key>, JsonDeserializer<InputConstants.Key> {
    @Override
    public InputConstants.Key deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if(jsonElement instanceof JsonObject jsonObject) {
            return this.migrate(jsonObject);
        }
        if(!(jsonElement instanceof JsonPrimitive jsonPrimitive && jsonPrimitive.isString())) return InputConstants.UNKNOWN;
        return InputConstants.getKey(jsonPrimitive.getAsString());
    }

    @Override
    public JsonElement serialize(InputConstants.Key key, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(key.getName());
    }

    private InputConstants.Key migrate(JsonObject object) {
        // Just a rough migration, I can't be bothered
        JsonElement codeElement = object.get("code");
        JsonElement typeElement = object.get("type");
        if(!(typeElement instanceof JsonPrimitive typePrimitive && typePrimitive.isNumber())) return InputConstants.UNKNOWN;
        if(!(codeElement instanceof JsonPrimitive codePrimitive && codePrimitive.isNumber())) return InputConstants.UNKNOWN;
        var type = typePrimitive.getAsInt();
        var code = codePrimitive.getAsInt();
        if(type == 0) { // KEYSYM
            if(code >= 65 && code <= 90) return InputConstants.Type.KEYBOARD.getOrCreate(code - 65 + InputConstants.KEY_A);
            return InputConstants.UNKNOWN;
        }
        if(type == 2) { // mouse
            if(code == 0) return InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT);
            if(code == 1) return InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT);
            if(code == 2) return InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_MIDDLE);
            return InputConstants.Type.MOUSE.getOrCreate(code + 1);
        }
        return InputConstants.UNKNOWN;
    }
}
