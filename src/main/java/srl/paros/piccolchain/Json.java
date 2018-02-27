package srl.paros.piccolchain;

import com.google.gson.Gson;

public interface Json {

    Gson gson = new Gson();

    static String toJson(Object object) {
        return gson.toJson(object);
    }
}
