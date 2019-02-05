package com.daftdroid.vpnhub.openvpn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import com.daftdroid.vpnhub.openvpn.options.CommonOptions;

public class Test {
    
    private File workingDir;
    private File outputDir;
    
    private void writeFile(OvpnTls t) throws FileNotFoundException {
        File out;
        String commonName = t.getCommonName();
        
        out = new File(outputDir, commonName+".pem");
        try (PrintWriter outfile = new PrintWriter(out)) {
            outfile.write(t.getKeyPem());
        }
        
        out = new File(outputDir, commonName+".crt");
        try (PrintWriter outfile = new PrintWriter(out)) {
            outfile.write(t.getCertPem());
        }

    }
    public void main2(String args[]) throws Exception {
        
        System.out.println(new CommonOptions("dafttun").toString());

        workingDir = new File(args[0]);
        if (!workingDir.isDirectory()) {
            throw new IllegalArgumentException("arg0 should be a directory");
        }
        outputDir = new File(workingDir, args[1]);

        outputDir.mkdir();

        /*
        System.out.println("Test read PEM data");
        OvpnTls t = new OvpnTls(null);
        String data = new String(Files.readAllBytes(Paths.get("testdata")));
        t.loadData(data);
        System.out.println(t.getKeyPem());
        System.out.println(t.getCertPem());
*/
        long startTime, caTime, childTime;
        System.out.println("Test generate CA");
        startTime = System.currentTimeMillis();
        OvpnTls tls = OvpnTls.generateCA("testing.example.com", 10);
        caTime = System.currentTimeMillis() - startTime;
        writeFile(tls);

        System.out.println("Test generate child node");
        startTime = System.currentTimeMillis();
        OvpnTls child = tls.createChildNode("node1", 5, 0);
        writeFile(child);
        childTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Times " + caTime + " " + childTime);
        
    }
    
    public static void main(String args[]) throws Exception {
        new Test().main2(args);
    }
}
