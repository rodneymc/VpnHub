package com.daftdroid.vpnhub.openvpn;

import java.nio.file.Files;
import java.nio.file.Paths;
import com.daftdroid.vpnhub.openvpn.options.CommonOptions;

public class Test {
    public static void main(String args[]) throws Exception {
        
        System.out.println(new CommonOptions("dafttun").toString());

        System.out.println("Test read PEM data");
        OvpnTls t = new OvpnTls(null);
        String data = new String(Files.readAllBytes(Paths.get("testdata")));
        t.loadData(data);
        System.out.println(t.getKeyPem());
        System.out.println(t.getCertPem());

        System.out.println("Test generate CA");
        OvpnTls tls = OvpnTls.generateCA("testing.example.com", 5);
        System.out.println(tls.getKeyPem());
        System.out.println(tls.getCertPem());
        //tls.getCertPem();

    }
}
