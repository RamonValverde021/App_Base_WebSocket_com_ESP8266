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
    TextInputEditText ssidEdit, passwordEdit, ipEdit, subnetEdit, gatewayEdit, dnsEdit;
    TextInputLayout ssidLayout, passwordLayout, ipLayout, subnetLayout, gatewayLayout, dnsLayout;

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

        ssidEdit = findViewById(R.id.txtSSID);
        passwordEdit = findViewById(R.id.txtPassword);
        ipEdit = findViewById(R.id.txtIPAddress);
        subnetEdit = findViewById(R.id.txtSubnetMask);
        gatewayEdit = findViewById(R.id.txtDefaultGateway);
        dnsEdit = findViewById(R.id.txtDNS);

        // Chamada de função para limpar o alerta de erro dos campos de digitação
        clearTypingError(ssidLayout);
        clearTypingError(passwordLayout);
        clearTypingError(ipLayout);
        clearTypingError(subnetLayout);
        clearTypingError(gatewayLayout);
        clearTypingError(dnsLayout);
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
/*
        String ssid = ((EditText) findViewById(R.id.txtSSID)).getText().toString();
        String password = ((EditText) findViewById(R.id.txtPassword)).getText().toString();
        String ip = ((EditText) findViewById(R.id.txtIPAddress)).getText().toString();
        String subnet = ((EditText) findViewById(R.id.txtSubnetMask)).getText().toString();
        String gateway = ((EditText) findViewById(R.id.txtDefaultGateway)).getText().toString();
        String dns = ((EditText) findViewById(R.id.txtDNS)).getText().toString();

        String mensagem = getString(R.string.txtConfirmSSID) + "\n" + ssid + "\n\n"
                        + getString(R.string.txtConfirmPassword) + "\n" + password + "\n\n"
                        + getString(R.string.txtConfirmIP) + "\n" + ipAddress + "\n\n"
                        + getString(R.string.txtConfirmSubNet) + "\n" + subnetMask + "\n\n"
                        + getString(R.string.txtConfirmGateway) + "\n" + defaultGateway + "\n\n"
                        + getString(R.string.txtConfirmDNS) + "\n" + dns;

                                new MaterialAlertDialogBuilder(this, R.style.MeuDialogoCustomizado)
                                        .setTitle(getString(R.string.tltConfirmation))
                                        .setMessage(mensagem)
                                        .setPositiveButton(getString(R.string.txtBtnOk), (dialog, which) -> {
                                            // ação ao confirmar
                                            Toast.makeText(this, "Configurações salvas!", Toast.LENGTH_SHORT).show();
                                        })
                                        //.setNegativeButton("Outro", null)
                                        .setNeutralButton(getString(R.string.txtBtnCancel), null)
                                        .show();
                            }
                        }
                    }
                }
            } else { // Senha
                passwordLayout.setError("Campo Senha vázio");
            }
        }else{ // SSID
            ssidLayout.setError("Campo SSID vázio");
        }
 */

        String okSSID = validField(ssidLayout);
        String okPassword = validField(passwordLayout);
        String okIP = validField(ipLayout);
        String okSubnet = validField(subnetLayout);
        String okGateway = validField(gatewayLayout);
        String okDNS = validField(dnsLayout);

        if (okSSID != null) {
            if (okPassword != null) {
                if (okIP != null) {
                    if (okSubnet != null) {
                        if (okGateway != null) {
                            if (okDNS != null) {

                                String ssid = (ssidEdit != null && ssidEdit.getText() != null) ? ssidEdit.getText().toString().trim() : "";
                                String password = (passwordEdit != null && passwordEdit.getText() != null) ? passwordEdit.getText().toString().trim() : "";
                                String ip = (ipEdit != null && ipEdit.getText() != null) ? ipEdit.getText().toString().trim() : "";
                                String subnet = (subnetEdit != null && subnetEdit.getText() != null) ? subnetEdit.getText().toString().trim() : "";
                                String gateway = (gatewayEdit != null && gatewayEdit.getText() != null) ? gatewayEdit.getText().toString().trim() : "";
                                String dns = (dnsEdit != null && dnsEdit.getText() != null) ? dnsEdit.getText().toString().trim() : "";

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
                                            // ação ao confirmar
                                            Toast.makeText(this, "Configurações salvas!", Toast.LENGTH_SHORT).show();
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
    }


// Função para validar endereço IPv4

    /**
     * Valida o conteúdo de um campo TextInputLayout como um endereço IPv4.
     * Exibe erro no próprio campo em caso de valor inválido ou ausente.
     *
     * @param textInputLayout Campo com TextInputEditText de onde será lido o IP.
     * @return O IP válido como String, ou null se for inválido.
     */
    public String validField(TextInputLayout textInputLayout) {
        // Obtém o EditText que está dentro do TextInputLayout
        TextInputEditText editText = (TextInputEditText) textInputLayout.getEditText();

        // Verifica se o EditText existe
        if (editText != null) {
            int id = editText.getId();
            String nomeId = textInputLayout.getContext().getResources().getResourceEntryName(id);
            Log.d("idEditText", "ID: " + id + ", Nome: " + nomeId);

            if (Objects.equals(nomeId, "txtSSID")) {
                // Pega o texto digitado no campo, removendo espaços em branco das extremidades
                String ssid = Objects.requireNonNull(editText.getText()).toString().trim();
                if (ssid.isEmpty()) {
                    textInputLayout.setError("Campo obrigatório");
                    return null;
                } else {
                    return ssid;
                }
            } else if (Objects.equals(nomeId, "txtPassword")) {
                String password = Objects.requireNonNull(editText.getText()).toString().trim();
                if (password.isEmpty()) {
                    textInputLayout.setError("Campo obrigatório");
                    return null;
                } else {
                    return password;
                }
            } else {
                // Pega o texto digitado no campo, removendo espaços em branco das extremidades
                String ip = Objects.requireNonNull(editText.getText()).toString().trim();

                // Verifica se o campo está vazio
                if (TextUtils.isEmpty(ip)) {
                    textInputLayout.setError("Campo obrigatório");
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
                    textInputLayout.setError("Endereço IP inválido"); // Define erro se IP for inválido
                    return null;
                }
            }
        } else {
            textInputLayout.setError("Campo inválido");
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