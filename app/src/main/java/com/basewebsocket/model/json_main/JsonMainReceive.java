package com.basewebsocket.model.json_main;

import com.google.gson.annotations.SerializedName;

public class JsonMainReceive {
    @SerializedName("Id")
    public String id;
    @SerializedName("Designação")
    public String designacao;
    @SerializedName("Dados")
    public Dados dados;

    public static class Dados {
        @SerializedName("Sensor")
        public String sensor;
    }
}




