package com.basewebsocket;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ConfigurationWiFi extends AppCompatActivity {

    Intent tela;
    Button btnEnviar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_configuration_wi_fi);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tela = new Intent(getApplicationContext(), MainActivity.class);
        btnEnviar = findViewById(R.id.btnSaveConfig);
    }

    public void returnMain(View view) {
        startActivity(tela);
    }

    public void saveConfig(View view) {
        String ssid = ((EditText) findViewById(R.id.txtSSID)).getText().toString();
        String password = ((EditText) findViewById(R.id.txtPassword)).getText().toString();
        String ipAddress = ((EditText) findViewById(R.id.txtIPAddress)).getText().toString();
        String subnetMask = ((EditText) findViewById(R.id.txtSubnetMask)).getText().toString();
        String defaultGateway = ((EditText) findViewById(R.id.txtDefaultGateway)).getText().toString();
        String dns = ((EditText) findViewById(R.id.txtDNS)).getText().toString();

        String mensagem = "Confirmação:\n\n"
                + "SSID: " + ssid + "\n"
                + "Senha: " + password + "\n"
                + "IP: " + ipAddress + "\n"
                + "Sub-rede: " + subnetMask + "\n"
                + "Gateway: " + defaultGateway + "\n"
                + "DNS: " + dns;
/*
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmação")
                .setMessage(mensagem)
                .setPositiveButton("OK", (dialog, which) -> {
                    Toast.makeText(this, "Configurações salvas!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
                */
        new AlertDialog.Builder(this)
                .setTitle("Confirmação")
                .setMessage(mensagem)
                .setPositiveButton("OK", (dialog, which) -> {
                    // ação ao confirmar
                    Toast.makeText(this, "Configurações salvas!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();

    }

    public void lookPassword(View view) {
    }

    public void viewTutorial(View view) {
    }
}