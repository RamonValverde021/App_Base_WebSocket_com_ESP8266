package com.basewebsocket.model.json_configuration;

public class JsonConfigSend {
    public String id = "aplicativo";
    public String designacao = "wi-fi";
    public Comando comando;

    public JsonConfigSend(String id, String designacao, String ssid, String password, String ip, String subnet, String gateway, String dns) {
        this.id = id;
        this.designacao = designacao;
        this.comando = new Comando(ssid, password, ip, subnet, gateway, dns);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesignacao() {
        return designacao;
    }

    public void setDesignacao(String designacao) {
        this.designacao = designacao;
    }

    public Comando getComando() {
        return comando;
    }

    public void setComando(Comando comando) {
        this.comando = comando;
    }

    public static class Comando {
        public String ssid = "admin";
        public String password = "admin";
        public String ip = "192.168.0.130";
        public String subnet = "255.255.255.0";
        public String gateway = "192.168.0.1";
        public String dns = "8.8.8.8";

        public Comando(String ssid, String password, String ip, String subnet, String gateway, String dns) {
            this.ssid = ssid;
            this.password = password;
            this.ip = ip;
            this.subnet = subnet;
            this.gateway = gateway;
            this.dns = dns;
        }

        public String getDns() {
            return dns;
        }

        public void setDns(String dns) {
            this.dns = dns;
        }

        public String getGateway() {
            return gateway;
        }

        public void setGateway(String gateway) {
            this.gateway = gateway;
        }

        public String getSubnet() {
            return subnet;
        }

        public void setSubnet(String subnet) {
            this.subnet = subnet;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getSsid() {
            return ssid;
        }

        public void setSsid(String ssid) {
            this.ssid = ssid;
        }
    }
}
