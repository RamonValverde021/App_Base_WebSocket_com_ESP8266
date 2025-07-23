package com.basewebsocket.model.json_main;

import com.google.gson.annotations.SerializedName;

public class JsonMainSend {
    @SerializedName("Id")
    String id;
    @SerializedName("Designação")
    String designacao;

    public JsonMainSend(String id, String designacao) {
        this.id = id;
        this.designacao = designacao;
    }
}
