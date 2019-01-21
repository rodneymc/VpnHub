package com.daftdroid.vpnhub.openvpn;

public class VpnNode {

    public enum OPTIONS {SEC_MODE, PROTOCOL, }

    private String ip;
    private int port; // Single port, multiple clients via TLS
    private OvpnTls nodeTls; // TLS config
    private OvpnTls ca;

    public String getConfig() {
        StringBuilder sb = new StringBuilder();

        return null;
    }
}
