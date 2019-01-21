package com.daftdroid.vpnhub.openvpn;

import org.bouncycastle.cert.X509v3CertificateBuilder;


public class OvpnTls {

    private X509v3CertificateBuilder x;
    
    public boolean isCa() {
        return false;
    }

    public String getCertPem() {
        return null;
    }

    public String getKeyPem() {
        return null;
    }

    public OvpnTls getCa() {
        return null;
    }
}
