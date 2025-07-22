package com.basewebsocket;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TinyDB {

    private static final String PREFS_NAME = "tinydb_android";
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    public TinyDB(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();
    }

    // ---------- String ----------
    public void put(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public String get(String key) {
        return sharedPreferences.getString(key, null);
    }

    public String get(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    // ---------- int ----------
    public void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    // ---------- boolean ----------
    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    // ---------- float ----------
    public void putFloat(String key, float value) {
        editor.putFloat(key, value);
        editor.apply();
    }

    public float getFloat(String key, float defaultValue) {
        return sharedPreferences.getFloat(key, defaultValue);
    }

    // ---------- long ----------
    public void putLong(String key, long value) {
        editor.putLong(key, value);
        editor.apply();
    }

    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    // ---------- List<String> ----------
    public void putListString(String key, List<String> list) {
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }

    public List<String> getListString(String key) {
        String json = sharedPreferences.getString(key, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // ---------- Atualizar (genérico) ----------
    public void update(String key, String value) {
        if (sharedPreferences.contains(key)) {
            put(key, value);
        }
    }

    public void updateInt(String key, int value) {
        if (sharedPreferences.contains(key)) {
            putInt(key, value);
        }
    }

    public void updateBoolean(String key, boolean value) {
        if (sharedPreferences.contains(key)) {
            putBoolean(key, value);
        }
    }

    // ---------- Outras funções ----------
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    public void remove(String key) {
        editor.remove(key);
        editor.apply();
    }

    public void clear() {
        editor.clear();
        editor.apply();
    }
}

/*
  🧪 Como usar:
  TinyDB tinyDB = new TinyDB(this);

  // Salvar dados
  tinyDB.put("nome", "Node MCU");
  tinyDB.putInt("porta", 2450);
  tinyDB.putBoolean("salvo", true);
  tinyDB.putFloat("tensao", 3.3f);
  tinyDB.putLong("timestamp", System.currentTimeMillis());

  // Salvar lista de strings
  List<String> redes = Arrays.asList("LAN_Solo", "LAN_Quarto");
  tinyDB.putListString("ssids", redes);

  // Recuperar dados
  String nome = tinyDB.get("nome", "Desconhecido");
  int porta = tinyDB.getInt("porta", 0);
  boolean salvo = tinyDB.getBoolean("salvo", false);
  List<String> listaSSIDs = tinyDB.getListString("ssids");
 */