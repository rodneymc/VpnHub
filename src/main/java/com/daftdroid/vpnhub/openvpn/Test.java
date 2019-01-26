package com.daftdroid.vpnhub.openvpn;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import org.bouncycastle.asn1.x509.Certificate;

import com.daftdroid.vpnhub.openvpn.options.CommonOptions;

public class Test {
    public static void main(String args[]) throws Exception {
        System.out.println(new CommonOptions("dafttun").toString());
        
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(4096);
        KeyPair key = keyGen.generateKeyPair();
        
        
        //Certificate c = OvpnTls.selfSign(key, "CN=demo.example.com");
        //System.out.println(c.getEncoded().toString());
        System.out.println(OvpnTls.selfSign(key, "CN=demo.example.com"));
    }
}
