package com.daftdroid.vpnhub.openvpn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

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
    
    private void writeRaw(OvpnTls t) throws IOException {
        File out;
        String commonName = t.getCommonName();
        
        out = new File(outputDir, commonName+".key.bin");
        try (FileOutputStream fos = new FileOutputStream(out)) {       
            fos.write(t.getKeyBytes());
        }

        out = new File(outputDir, commonName+".crt.bin");
        try (FileOutputStream fos = new FileOutputStream(out)) {       
            fos.write(t.getCertBytes());
        }
    }
    private void write(OvpnTls t, boolean raw) throws IOException {
        if (raw) {
            writeRaw(t);
        }
        else {
            writeFile(t);
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

        long startTime, caTime, childTime;
        System.out.println("Test generate CA");
        startTime = System.currentTimeMillis();
        OvpnTls tls = OvpnTls.generateCA("testing.example.com", 10);
        caTime = System.currentTimeMillis() - startTime;
        write(tls, true);

        System.out.println("Test generate child node");
        startTime = System.currentTimeMillis();
        OvpnTls child = tls.createChildNode("node1", 5, 0);
        write(child, true);
        childTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Times " + caTime + " " + childTime);
        
    }
    
    public static void main(String args[]) throws Exception {
        new Test().main2(args);
        //new Test().main3(new File("/dev/shm/catest/testing.example.com.crt"), new File("/dev/shm/catest/testing.example.com.key.bin"));
    }
    public void main3(File crtFile, File keyFile) throws IOException {
        
        OvpnTls t = new OvpnTls(null);
        
        try (FileInputStream fos = new FileInputStream(crtFile)){
            t.loadCrt(fos);
        }
        
        try (FileInputStream fos = new FileInputStream(keyFile)){
            t.loadKey(fos);
        }
        
        
        System.out.println(t.getKeyPem());
        System.out.println(t.getCertPem());
    }
}
