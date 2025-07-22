package com.basewebsocket;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.net.URI;
import java.util.Objects;

public class ConfigurationWiFi extends AppCompatActivity {
    Intent mainScreen;
    Button btnEnviar;
    LinearLayout tutorial;
    ScrollView scrollViewMain;
    URI uri;
    MeuWebSocket socket;
    TextInputLayout ssidLayout, passwordLayout, ipLayout, subnetLayout, gatewayLayout, dnsLayout;
    TinyDB tinyDB;

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

        mainScreen = new Intent(getApplicationContext(), MainActivity.class);
        btnEnviar = findViewById(R.id.btnSaveConfig);
        tutorial = findViewById(R.id.viewTutorial);
        scrollViewMain = findViewById(R.id.ScrollViewPrincipal);

        ssidLayout = findViewById(R.id.txtSSIDLayout);
        passwordLayout = findViewById(R.id.txtPasswordLayout);
        ipLayout = findViewById(R.id.txtIPAddressLayout);
        subnetLayout = findViewById(R.id.txtSubnetMaskLayout);
        gatewayLayout = findViewById(R.id.txtDefaultGatewayLayout);
        dnsLayout = findViewById(R.id.txtDNSLayout);

        // Chamada de função para limpar o alerta de erro dos campos de digitação
        clearTypingError(ssidLayout);
        clearTypingError(passwordLayout);
        clearTypingError(ipLayout);
        clearTypingError(subnetLayout);
        clearTypingError(gatewayLayout);
        clearTypingError(dnsLayout);

        // Incializando Banco de dados
        tinyDB = new TinyDB(this);

        // Buscando dados do Wi-Fi na memoria
        String ssidSalve = tinyDB.get("ssid", getString(R.string.hntMyWiFiAddress));
        String passwordSalve = tinyDB.get("password", getString(R.string.hntMyWiFiPassword));
        String ipSalve = tinyDB.get("ip", "192.168.0.130");
        String subnetSalve = tinyDB.get("subnet", "255.255.255.0");
        String gatewaySalve = tinyDB.get("gateway", "192.168.0.1");
        String dnsSalve = tinyDB.get("dns", "8.8.8.8");

        // Atualizando placeholders dos campos
        ((TextInputLayout) findViewById(R.id.txtSSIDLayout)).setPlaceholderText(ssidSalve);
        //((TextInputLayout) findViewById(R.id.txtPasswordLayout)).setPlaceholderText(passwordSalve); // Não é muito seguro esse
        ((TextInputLayout) findViewById(R.id.txtIPAddressLayout)).setPlaceholderText(ipSalve);
        ((TextInputLayout) findViewById(R.id.txtSubnetMaskLayout)).setPlaceholderText(subnetSalve);
        ((TextInputLayout) findViewById(R.id.txtDefaultGatewayLayout)).setPlaceholderText(gatewaySalve);
        ((TextInputLayout) findViewById(R.id.txtDNSLayout)).setPlaceholderText(dnsSalve);

        // Observando dados salvos
        Log.d("debugIP", "SSID: " + ssidSalve + "\n" + "Senha: " + passwordSalve + "\n" + "IP: " + ipSalve + "\n" + "Mascara de Sub-Rede: " + subnetSalve + "\n" + "Gateway Padrão: " + gatewaySalve + "\n" + "DNS: " + dnsSalve);
    }

    public void returnMain(View view) {
        startActivity(mainScreen);
        // Aplica uma animação de entrada e de saída
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void connectWebsocket(View view) {
        try {
            uri = new URI("ws://192.168.4.1:2450"); // Certifique-se que é a porta certa!
            socket = new MeuWebSocket(uri, this);  // passando a Activity agora usando a variável global
            socket.connect();

            Log.d("debugWebSocket", "Tentando reconectar..."); // Escreve no Logcat que a tentativa de reconexão começou (útil para debug)
            try {
                if (socket != null && socket.isOpen()) {                       // Se o WebSocket estiver conectado, não faz nada (evita reconexões desnecessárias)
                    Toast.makeText(getApplicationContext(), getString(R.string.msgAppConnected), Toast.LENGTH_SHORT).show();       // Exibe um mensagem indicando app conectado
                } else {                                                       // Se não estiver conectado, cria uma nova instância do WebSocket
                    socket = new MeuWebSocket(uri, ConfigurationWiFi.this);  // Passando o endereço do ESP (uri) e a Activity atual (this)
                    socket.connect();                                          // Inicia a tentativa de conexão com o servidor WebSocket
                }
            } catch (
                    Exception e) {                                       // Se algo der errado (ex: URI inválido, falha de rede...), captura a exceção
                e.printStackTrace();                                      // Imprime os detalhes do erro no Logcat
                Toast.makeText(getApplicationContext(), getString(R.string.msgFailReconnect), Toast.LENGTH_SHORT).show();                    // Atualiza a interface para informar que a reconexão falhou
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), getString(R.string.msgErrorConnecting), Toast.LENGTH_SHORT).show();
            Log.d("debugWebSocket", "Falha ao conectar...");
        }
    }

    public void viewTutorial(View view) {
        int nightModeFlags = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        MaterialButton btnConnect = this.findViewById(R.id.btnDoubt);
        if (tutorial.getVisibility() != View.VISIBLE) {
            tutorial.setVisibility(View.VISIBLE); // Torna visível somente se ainda não estiver
            //scrollViewMain.post(() -> scrollViewMain.smoothScrollTo(0, tutorial.getBottom())); // Scroll suavemente até o último elemento

            scrollViewMain.post(() -> {
                int targetY = tutorial.getBottom();
                ObjectAnimator animator = ObjectAnimator.ofInt(scrollViewMain, "scrollY", scrollViewMain.getScrollY(), targetY);
                animator.setDuration(1000); // Duração em milissegundos (ajuste conforme o efeito desejado)
                animator.setInterpolator(new DecelerateInterpolator()); // Interpolador para suavidade
                animator.start();
            });

            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) { // Está no modo escuro
                // Altera a cor de fundo (background)
                btnConnect.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green_system)));
                // Altera a cor do texto
                btnConnect.setTextColor(ContextCompat.getColor(this, R.color.WHITE));
            } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) { // Está no modo claro
                btnConnect.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue_system)));
                btnConnect.setTextColor(ContextCompat.getColor(this, R.color.WHITE));
            }
        } else {
            tutorial.setVisibility(View.GONE); // Torna a ocultar
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) { // Está no modo escuro
                btnConnect.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.BLACK)));
                btnConnect.setTextColor(ContextCompat.getColor(this, R.color.WHITE));
            } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) { // Está no modo claro
                btnConnect.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.WHITE)));
                btnConnect.setTextColor(ContextCompat.getColor(this, R.color.darkGray));
            }
        }
    }

    public void saveConfig(View view) {
        if (socket != null && socket.isOpen()) {     // Verifica se o websocket esta conectado

            String ssid = validField(ssidLayout);
            String password = validField(passwordLayout);
            String ip = validField(ipLayout);
            String subnet = validField(subnetLayout);
            String gateway = validField(gatewayLayout);
            String dns = validField(dnsLayout);

            if (ssid != null) { // Verifica se o ssid não está vazio
                if (password != null) {
                    if (ip != null) { // Verifica se contem um código IP valido
                        if (subnet != null) {
                            if (gateway != null) {
                                if (dns != null) {

                                    String mensagem = getString(R.string.txtConfirmSSID) + "\n" + ssid + "\n\n"
                                            + getString(R.string.txtConfirmPassword) + "\n" + password + "\n\n"
                                            + getString(R.string.txtConfirmIP) + "\n" + ip + "\n\n"
                                            + getString(R.string.txtConfirmSubNet) + "\n" + subnet + "\n\n"
                                            + getString(R.string.txtConfirmGateway) + "\n" + gateway + "\n\n"
                                            + getString(R.string.txtConfirmDNS) + "\n" + dns;

                                    new MaterialAlertDialogBuilder(this, R.style.MeuDialogoCustomizado)
                                            .setTitle(getString(R.string.tltConfirmation))
                                            .setMessage(mensagem)
                                            .setPositiveButton(getString(R.string.txtBtnOk), (dialog, which) -> {
                                                // Ações de confirmação

                                                // Gravando dados no banco de dados
                                                tinyDB.put("ssid", ssid);
                                                tinyDB.put("password", password);
                                                tinyDB.put("ip", ip);
                                                tinyDB.put("subnet", subnet);
                                                tinyDB.put("gateway", gateway);
                                                tinyDB.put("dns", dns);

                                                // Exibindo mensagem de confirmação
                                                Toast.makeText(this, getString(R.string.msgSettingsSaved), Toast.LENGTH_SHORT).show();
                                            })
                                            //.setNegativeButton("Outro", null)
                                            .setNeutralButton(getString(R.string.txtBtnCancel), null)
                                            .show();
                                }
                            }
                        }
                    }

                }
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.msgConnectionToDeviceIsNotActive), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Valida o conteúdo de um campo TextInputLayout como um endereço IPv4 ou se está vazio.
     * Exibe erro no próprio campo em caso de valor inválido ou ausente.
     *
     * @param textInputLayout Campo com TextInputEditText de onde será lido o conteudo.
     * @return O conteudo válido como String, ou null se for inválido.
     */
    public String validField(TextInputLayout textInputLayout) {
        // Obtém o EditText que está dentro do TextInputLayout
        TextInputEditText editText = (TextInputEditText) textInputLayout.getEditText();

        // Verifica se o EditText existe
        if (editText != null) {
            int id = editText.getId();
            String nomeId = textInputLayout.getContext().getResources().getResourceEntryName(id);
            Log.d("idEditText", "ID: " + id + ", Nome: " + nomeId); // Para fins de observação

            if (Objects.equals(nomeId, "txtSSID")) {
                // Pega o texto digitado no campo, removendo espaços em branco das extremidades
                String ssid = Objects.requireNonNull(editText.getText()).toString().trim();
                if (ssid.isEmpty()) { // Se o campo estiver vazio
                    textInputLayout.setError(getString(R.string.altRequiredField)); // Informa no campo que está vazio
                    return null;
                } else { // Se o campo estiver preenchido
                    return Objects.requireNonNull(editText.getText()).toString(); // Retorna o valor do campo convertido em string
                }
            } else if (Objects.equals(nomeId, "txtPassword")) {
                String password = Objects.requireNonNull(editText.getText()).toString().trim();
                if (password.isEmpty()) {
                    textInputLayout.setError(getString(R.string.altRequiredField));
                    return null;
                } else {
                    return Objects.requireNonNull(editText.getText()).toString();
                }
            } else {
                // Pega o texto digitado no campo, removendo espaços em branco das extremidades
                String ip = Objects.requireNonNull(editText.getText()).toString().trim();

                // Verifica se o campo está vazio
                if (TextUtils.isEmpty(ip)) {
                    textInputLayout.setError(getString(R.string.altRequiredField));
                    return null;
                }

                // Expressão regular que valida um endereço IPv4 válido (0.0.0.0 a 255.255.255.255)
                String ipv4Pattern = "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}"
                        + "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$";

                // Verifica se o IP informado bate com o padrão da regex
                if (ip.matches(ipv4Pattern)) {
                    textInputLayout.setError(null); // Remove qualquer erro anterior
                    return ip; // Retorna o IP válido
                } else {
                    textInputLayout.setError(getString(R.string.altInvalidIPAddress)); // Define erro se IP for inválido
                    return null;
                }
            }
        } else {
            textInputLayout.setError(getString(R.string.altRequiredField));
            return null;
        }
    }


    /**
     * Adiciona um TextWatcher ao campo para remover a mensagem de erro
     * assim que o usuário começar a digitar algo no campo.
     *
     * @param inputLayout Campo com TextInputEditText que terá o erro limpo automaticamente.
     */
    private void clearTypingError(TextInputLayout inputLayout) {
        // Obtém o EditText de dentro do TextInputLayout
        TextInputEditText editText = (TextInputEditText) inputLayout.getEditText();

        if (editText != null) {
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Não precisa fazer nada antes de o texto mudar
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Assim que o usuário digitar algo, remove o erro do campo
                    inputLayout.setError(null);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Não precisa fazer nada depois que o texto mudar
                }
            });
        }
    }
}