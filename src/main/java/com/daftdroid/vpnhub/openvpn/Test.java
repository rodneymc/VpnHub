package com.daftdroid.vpnhub.openvpn;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import com.daftdroid.vpnhub.openvpn.options.CommonOptions;

public class Test {
    public static void main(String args[]) throws Exception {
        System.out.println(new CommonOptions("dafttun").toString());
        
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(4096);
        KeyPair key = keyGen.generateKeyPair();
        
        
        System.out.println(OvpnTls.selfSign(key, "CN=demo.example.com"));
    }
}
