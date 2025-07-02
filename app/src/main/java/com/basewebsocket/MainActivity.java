package com.basewebsocket;

import android.os.Bundle;

import com.basewebsocket.model.json_enviar.JsonEnviar;
import com.google.gson.Gson;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.net.URI;

public class MainActivity extends AppCompatActivity {

    private URI uri;
    private MeuWebSocket socket;
    private TextView status;

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

        status = findViewById(R.id.lblStatus);

        try {
            uri = new URI("ws://192.168.0.130:2450/"); // Certifique-se que é a porta certa!
            socket = new MeuWebSocket(uri, this); // passando a Activity agora usando a variável global
            socket.connect();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("testeWebSocket", "Erro ao conectar: " + e.getMessage());
        }
    }

    int repeticoes = 0; // Inicia um contador de vezes que se reconectou automaticamente
    // Função para reconectar automaticamente co ESP8266
    protected void reconectarWebSocket() {
        if(repeticoes < 3) { // Se o app ja se reconectou automaticamente 10 vezes
            Log.d("WebSocket", "Tentando reconectar..."); // Escreve no Logcat que a tentativa de reconexão começou (útil para debug)

            // Cria um atraso de 3000ms (3 segundos) antes de executar o código dentro
            // Isso evita tentar reconectar imediatamente, dando tempo para o ESP se estabilizar
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    // Verifica se o socket já existe e está conectado
                    if (socket != null && socket.isOpen()) {               // Se estiver, não faz nada (evita reconexões desnecessárias)
                        status.setText(getString(R.string.msgAppConnected));
                        return;                                            // Sai do método se já estiver conectado
                    }

                    // Se não estiver conectado, cria uma nova instância do WebSocket
                    socket = new MeuWebSocket(uri, MainActivity.this); // Passando o endereço do ESP (uri) e a Activity atual (this)
                    socket.connect();                                         // Inicia a tentativa de conexão com o servidor WebSocket
                } catch (
                        Exception e) {                                       // Se algo der errado (ex: URI inválido, falha de rede...), captura a exceção
                    e.printStackTrace();                                      // Imprime os detalhes do erro no Logcat
                    status.setText(getString(R.string.msgFailReconnect));                    // Atualiza a interface para informar que a reconexão falhou
                }
            }, 3000); // Tempo de espera antes da tentativa de reconexão: 3000 milissegundos (3 segundos)
            repeticoes++; // Incrementa o contador ao final de cada reconexão
        }
    }


    //-------------------------------- BOTÕES DA INTERFACE --------------------------------//

    // Botão de teste de enviar mensagem
    public void testar(View view) {
        Gson gson = new Gson();
        JsonEnviar comando = new JsonEnviar("LightFy", "Acionamento");
        String json = gson.toJson(comando);
        if (socket != null && socket.isOpen()) {
            socket.send(json);
        } else {
            status.setText(getString(R.string.msgSocketNotOpenYet));
        }
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
                return;                                                 // Sai do método se já estiver conectado
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
}