package com.basewebsocket.model.json_enviar;

import com.google.gson.annotations.SerializedName;

public class JsonEnviar {
    @SerializedName("Id")
    String id;
    @SerializedName("Designação")
    String designacao;

    public JsonEnviar(String id, String designacao) {
        this.id = id;
        this.designacao = designacao;
    }
}
