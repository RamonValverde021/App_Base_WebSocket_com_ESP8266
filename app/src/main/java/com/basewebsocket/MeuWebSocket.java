package com.basewebsocket;

import android.app.Activity; // Importa a classe Activity para podermos interagir com a interface do app
import android.content.res.Configuration;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.Log;

import com.basewebsocket.model.json_receber.JsonReceber;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class MeuWebSocket extends WebSocketClient { // Essa é a classe personalizada que estende (herda) WebSocketClient, da biblioteca Java-WebSocket

    private final Activity activity; // Armazena a referência da Activity (tela atual) para podermos atualizar a interface gráfica
    private final TextView status;  // Guarda a referência do TextView lblStatus
    private final TextView serial;  // Se quiser fazer o mesmo com o outro TextView

    // Construtor da classe MeuWebSocket
    public MeuWebSocket(URI serverUri, Activity activity) { // Recebe o endereço do servidor WebSocket (URI) e a Activity onde será usado
        super(serverUri);                                   // Chama o construtor da classe pai (WebSocketClient) com o endereço do servidor
        this.activity = activity;                           // Armazena a Activity para usar depois dentro dos métodos da classe

        // Inicializa os TextViews uma vez, sem repetir nas outras funções
        status = activity.findViewById(R.id.lblStatus); // Encontra o TextView com id lblStatus na tela
        serial = activity.findViewById(R.id.lblSerial); // Encontra o TextView com id lblSerial na tela
    }

    // Metodo que é automaticamente chamado quando o WebSocket recebe uma mensagem do servidor (ESP8266)
    @Override
    public void onMessage(String message) {
        Log.d("debugWebSocket", "WebSocket onMessage() \n    Message: " + message); // Mostra no Logcat a mensagem recebida (útil para depuração)
        // Como não podemos modificar a interface diretamente de uma thread de rede,
        // usamos runOnUiThread() para rodar esse código na thread principal (a da interface)
        activity.runOnUiThread(() -> {
            //serial.setText("ESP: " + message);                     // Atualiza o texto desse TextView com a mensagem recebida do ESP
            serial.append("ESP: " + message + "\n\n");               // Você pode até ir acumulando as mensagens se quiser ver o histórico:
            statusLed(message);
        });
    }

    // Os demais métodos como onOpen(), onClose() e onError() ficam no mesmo estilo,
    // e também podem chamar runOnUiThread() para atualizar a interface se necessário

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i("debugWebSocket", "WebSocket onOpen() \n    Server Handshake: " + handshakedata);
        send("Oi ESP!");  // Envia uma mensagem
        activity.runOnUiThread(() -> {
            ProgressBar iconeStatus = activity.findViewById(R.id.iconeStatus); // Pega o obejto do progressBar
            TextView status = activity.findViewById(R.id.lblStatus);           // Pega o obejto do textView status
            iconeStatus.setVisibility(View.INVISIBLE);                         // oculta o icone do progressBar
            status.setVisibility(View.VISIBLE);                                // exibe o textView do status
            status.setText(activity.getString(R.string.msgAppConnected));      // exibe a mensagem de status que o app conectou
        });
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.w("debugWebSocket", "WebSocket onClose() \n    Code: " + code + "\n    Reason: " + reason + "\n    Remote: " + remote);
        activity.runOnUiThread(() -> {
            status.setText(activity.getString(R.string.msgConnectionLostTryingToReconnect));
            if (code != -1) {
                Log.v("debugWebSocket", "WebSocket onClose() \n    Chamando reconectarWebSocket()");
                ((MainActivity) activity).reconnectWebSocket();  // chama a reconexão
            }
        });
    }

    @Override
    public void onError(Exception ex) {
        Log.e("debugWebSocket", "WebSocket onError() \n    Exception: " + ex);
        activity.runOnUiThread(() -> {
            status.setText(activity.getString(R.string.msgConnectionErrorTryingToReconnect));
        });
    }


    public void statusLed(String mensagemJson) {
        int nightModeFlags = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK; // Identica o tema do Smartphone, se está no modo claro ou escuro
        try {
            Gson gson = new Gson();
            JsonReceber mensagem = gson.fromJson(mensagemJson, JsonReceber.class);

            if (mensagem != null && mensagem.dados != null && mensagem.dados.sensor != null) {
                String id = mensagem.id;
                String designacao = mensagem.designacao;
                String sensor = mensagem.dados.sensor;
                Log.d("DEBUG", "ID: " + id + ", Designação: " + designacao + ", Sensor: [" + sensor + "]");

                ImageView led = activity.findViewById(R.id.imgLed);
                if ("LightFy".equals(id) && "Status".equals(designacao)) {
                    if ("Ligada".equals(sensor.trim())) {
                        if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) { // Se o smartphone estiver no modo claro
                            led.setImageResource(R.drawable.led_azul);
                        } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) { // Se o smartphone estiver no modo escuro
                            led.setImageResource(R.drawable.led_verde);
                        }
                    } else {
                        led.setImageResource(R.drawable.led_transparente);
                    }
                }
                Log.d("GSON", "Sensor seguro: " + sensor);
            } else {
                Log.w("GSON", "Algum campo nulo no caminho até 'Sensor'");
            }
        } catch (JsonSyntaxException e) {
            Log.e("GSON", "Erro ao interpretar JSON: " + e.getMessage());
        }
    }

}