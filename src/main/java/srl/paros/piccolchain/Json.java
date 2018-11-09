package srl.paros.piccolchain;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public interface Json {

    Gson gson = new Gson();

    static String toJson(Object object) {
        return gson.toJson(object);
    }

    static <T> T fromJson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    static <T> T fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }
}
