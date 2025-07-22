package com.basewebsocket;

import android.content.Intent;
import android.os.Bundle;

import com.basewebsocket.model.json_enviar.JsonEnviar;
import com.google.gson.Gson;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.net.URI;

public class MainActivity extends AppCompatActivity {

    Intent configScreen;
    private URI uri;
    private MeuWebSocket socket;
    private TextView serial, status;
    private EditText mensagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        configScreen = new Intent(getApplicationContext(), ConfigurationWiFi.class);
        serial = findViewById(R.id.lblSerial);
        status = findViewById(R.id.lblStatus);
        mensagem = findViewById(R.id.txtMessageText);

        try {
            uri = new URI("ws://192.168.0.130:2450/"); // Certifique-se que é a porta certa!
            socket = new MeuWebSocket(uri, this);  // passando a Activity agora usando a variável global
            socket.connect();
            // Se o app iniciar primeiro que o ESP, então ele tenta conectar automaticamente
            if (socket == null || !socket.isOpen()) reconnectWebSocket();
        } catch (Exception e) {
            e.printStackTrace();
            status.setText(getString(R.string.msgErrorConnecting));
            Log.d("debugWebSocket", "Falha ao conectar...");
        }
    }

    // Função para reconectar automaticamente ao ESP8266 em caso de desconexão de ambos os lados
    protected void reconnectWebSocket() {
        // Antes de tentar sair reconectando, verifica antes e sai da função por aqui mesmo se já estiver conectado
        if (socket != null && socket.isOpen()){
            Log.d("debugWebSocket", "Já conectado!");
        } else {
            Log.d("debugWebSocket", "Tentando reconectar..."); // Escreve no Logcat que a tentativa de reconexão começou (útil para debug)
            // Cria um atraso de 3000ms (3 segundos) antes de executar o código dentro
            // Isso evita tentar reconectar imediatamente, dando tempo para o ESP se estabilizar
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    if (socket != null && socket.isOpen()) {                       // Se o WebSocket estiver conectado, não faz nada (evita reconexões desnecessárias)
                        status.setText(getString(R.string.msgAppConnected));       // Exibe um mensagem indicando app conectado
                    } else {                                                       // Se não estiver conectado, cria uma nova instância do WebSocket
                        socket = new MeuWebSocket(uri, MainActivity.this);  // Passando o endereço do ESP (uri) e a Activity atual (this)
                        socket.connect();                                          // Inicia a tentativa de conexão com o servidor WebSocket
                    }
                } catch (
                        Exception e) {                                       // Se algo der errado (ex: URI inválido, falha de rede...), captura a exceção
                    e.printStackTrace();                                      // Imprime os detalhes do erro no Logcat
                    status.setText(getString(R.string.msgFailReconnect));                    // Atualiza a interface para informar que a reconexão falhou
                }
                reconnectWebSocket(); // ⚠️ Aqui o pulo do gato: Repete a reconexão automaticamente com pausas de 3 segundos do Handler
            }, 3000); // Tempo de espera antes da tentativa de reconexão: 3000 milissegundos (3 segundos)
        }
    }

    //-------------------------------- BOTÕES DA INTERFACE --------------------------------//

    // Botão de teste de enviar mensagem
    public void sendJSON(View view) {
        Gson gson = new Gson();
        JsonEnviar comando = new JsonEnviar("LightFy", "Acionamento");
        String json = gson.toJson(comando);
        if (socket != null && socket.isOpen()) { // Verifica se o websocket esta conectado
            socket.send(json);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.msgWebSocketConnectionIsNotActive), Toast.LENGTH_SHORT).show();
        }
    }

    // Botão de enviar mensagem JSON ao ESP8266
    public void sendText(View view) {
        String text = mensagem.getText().toString(); // Pega o texto de EditText e converte para string
        if (socket != null && socket.isOpen()) {     // Verifica se o websocket esta conectado
            if (!text.trim().isEmpty()) {            // Verifica se a mensagem não está vazia
                socket.send(text);                   // ✅ Pode enviar normalmente
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.msgEmptyField), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.msgWebSocketConnectionIsNotActive), Toast.LENGTH_SHORT).show();
        }
    }

    // Botão limpara dados da Serial
    public void limparSerial(View view) {
        serial.setText("");
    }

    // Botão de desconectar
    public void desconectar(View view) {
        try {
            if (socket != null && socket.isOpen()) {      // Verifica se o socket existe e está conectado
                socket.close();                           // Fecha a conexão com o ESP
                status.setText(getString(R.string.msgDisconnected));          // Atualiza a interface
            } else {
                status.setText(getString(R.string.msgAlreadyDisconnect));  // Se já estiver desconectado, apenas avisa
            }
        } catch (Exception e) {
            e.printStackTrace();                          // Em caso de erro, imprime e informa
            status.setText(getString(R.string.msgErrorDisconnecting));
        }
    }

    // Botão de conectar
    public void conectar(View view) {
        try {
            if (socket != null && socket.isOpen()) {                    // Verifica se o socket já está conectado
                status.setText(getString(R.string.msgAlreadyConnected));
                return;                                                 // Sai do metodo se já estiver conectado
            }
            // Basicamente repetindo a conexão automatica no onCreate
            socket = new MeuWebSocket(uri, MainActivity.this);  // Cria a instância do WebSocket passando o endereço e o contexto da Activity
            socket.connect();                                          // Inicia a conexão com o servidor (ESP)
            status.setText(getString(R.string.msgConnecting));                           // Atualiza a interface para indicar que está tentando conectar
        } catch (Exception e) {
            e.printStackTrace();                                       // Captura qualquer erro e exibe na interface e no log
            status.setText(getString(R.string.msgErrorConnecting));
        }
    }

    public void screenConfig(View view) {
        startActivity(configScreen);
    }
}