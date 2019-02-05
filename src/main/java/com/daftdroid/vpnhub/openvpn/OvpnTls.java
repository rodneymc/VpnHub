package com.daftdroid.vpnhub.openvpn;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.cert.CertificateEncodingException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;


public class OvpnTls {

    
    static {
        java.security.Security.addProvider(
                new org.bouncycastle.jce.provider.BouncyCastleProvider()
       );
    }

    private final OvpnTls parent;
    
    public OvpnTls(OvpnTls parent) {
        this.parent = parent;
    }
    
    private X509Certificate certificate;
    private PrivateKey privateKey;
    private String commonName;

    public String getCertPem() {
        try {
            StringWriter s = new StringWriter();
            JcaPEMWriter p = new JcaPEMWriter(s);
            p.writeObject(certificate);
            p.close();
            return s.toString();
        } catch (IOException e) {
            // None of the input/output to this method are files, network connections or whatnot,
            // an IOException simply shouldn't occur???
            
            throw new RuntimeException("Unexpected IOException", e); // TODO improve me?
        }
    }

    public String getKeyPem() {
        try {
            StringWriter s = new StringWriter();
            JcaPEMWriter p = new JcaPEMWriter(s);
            p.writeObject(privateKey);
            p.close();
            return s.toString();
        } catch (IOException e) {
            // None of the input/output to this method are files, network connections or whatnot,
            // an IOException simply shouldn't occur???
            
            throw new RuntimeException("Unexpected IOException", e); // TODO improve me?
        }
    }
    public byte[] getKeyBytes() {
        return privateKey.getEncoded();
    }
    public byte[] getCertBytes() {
        try {
            return certificate.getEncoded();
        } catch (CertificateEncodingException e) {
            return null;
        }
    }
    
    public void loadData(String data) throws IOException {
        
        PemObject ob;
        
        try (PemReader reader = new PemReader(new StringReader(data));) {
            while ((ob = reader.readPemObject()) != null) {

                String type = ob.getType();
                byte databytes[] = ob.getContent();
                
                if ("RSA PRIVATE KEY".equals(type)) {

                    loadKey(databytes);
                } else if ("CERTIFICATE".equals(type)) {
                    loadCrt(databytes);
                }
                
            }
        } 
    }
    // TODO password
    public void loadKey(byte [] data) throws IOException {
        KeyFactory kf;

        try {
            kf = KeyFactory.getInstance("RSA"); }
        catch (NoSuchAlgorithmException e) {
            throw new IOException ("RSA Not supported????", e); //TODO
        }
        
        try {
            
            PKCS8EncodedKeySpec keyspec = new PKCS8EncodedKeySpec(data);
            privateKey = kf.generatePrivate(keyspec);
        } catch (InvalidKeySpecException e) {
            throw new IOException("Invalid key format", e);
        }
    }
    public void loadKey(InputStream stream) throws IOException {
        byte [] data = new byte[19000]; // TODO hard coded for 4096 bit key. Is it always this size???
        stream.read(data, 0, data.length);
        
        if (stream.read() > 0) {
            throw new IOException("Key data unexpectedly large");
        }
        
        loadKey(data);
    }
    
    public void loadCrt(byte[] data) throws IOException {
        loadCrt(new ByteArrayInputStream(data));
    }
    public void loadCrt(InputStream stream) throws IOException {
        try {
            final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) certFactory.generateCertificate(stream);
        } catch (CertificateException e) {
            throw new IOException("Invalid certificate", e);
        }
    }

    /*
     * Gets the parent node.
     */
    public OvpnTls getParent() {
        return parent;
    }
    /*
     * Walks back through the certificate chain to the root, (returns this if this is the root)
     */
    public OvpnTls getCa() {
        OvpnTls nextParent = this;
        
        while (nextParent.parent != null) {
            nextParent = nextParent.parent;
        }
        
        return nextParent;
    }

    public static OvpnTls generateCA (String commonName, int years)
    {
        // TODO sanity check the common name
        try {
            long now = System.currentTimeMillis();
            Date startDate = new Date(now);
            String subjectDN = "CN="+commonName;
    
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(4096);
            KeyPair keyPair = keyGen.generateKeyPair();
            
            X500Name dnName = new X500Name(subjectDN);
            BigInteger certSerialNumber = new BigInteger(Long.toString(now)); // <-- Using the current timestamp as the certificate serial number
    
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.YEAR, years);
    
            Date endDate = calendar.getTime();
    
            JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(dnName, certSerialNumber, startDate, endDate, dnName, keyPair.getPublic());
    
            // Extensions --------------------------
    
            // Basic Constraints
            BasicConstraints basicConstraints = new BasicConstraints(true); // <-- true for CA, false for EndEntity
    
            certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints); // Basic Constraints is usually marked as critical.
    
            ContentSigner signer = new JcaContentSignerBuilder("SHA512withRSA")
                    .build(keyPair.getPrivate());
            
            X509CertificateHolder h = certBuilder.build(signer);
             
            OvpnTls ovpnTls = new OvpnTls(null); // null = no parent
            ovpnTls.certificate = new JcaX509CertificateConverter().setProvider( "BC" )
                    .getCertificate(h);
            ovpnTls.privateKey = keyPair.getPrivate();
            ovpnTls.commonName = commonName;
            
            return ovpnTls;
        } catch (NoSuchAlgorithmException | OperatorCreationException  | CertIOException | CertificateException e) {

            // TODO some sort of "this code can't create cert on tis platform" status.
            return null;
        }
    }
    public OvpnTls createChildNode(String subdomain, int years, int days) {
        // TODO sanity check the subdomain
        
        try {
            
            String newCommonName = subdomain+"."+commonName;
            long now = System.currentTimeMillis();
            Date startDate = new Date(now);
            String subjectDN = "CN="+newCommonName;
    
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(4096);
            KeyPair keyPair = keyGen.generateKeyPair();
            
            X500Name dnName = new X500Name(subjectDN);
            BigInteger certSerialNumber = new BigInteger(Long.toString(now)); // <-- Using the current timestamp as the certificate serial number
    
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            
            // Add the years, then the days. Note the order is important(ish) it could make one day's difference in the event of a leap year.
            calendar.add(Calendar.YEAR, years);
            calendar.add(Calendar.DATE, days);
    
            Date endDate = calendar.getTime();
    
            JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(certificate, 
                    certSerialNumber, 
                    startDate, 
                    endDate, 
                    dnName, 
                    keyPair.getPublic());
    
            
            // Extensions --------------------------
    
            // Basic Constraints
            BasicConstraints basicConstraints = new BasicConstraints(false); // <-- true for CA, false for EndEntity
    
            certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints); // Basic Constraints is usually marked as critical.
    
            ContentSigner signer = new JcaContentSignerBuilder("SHA512withRSA")
                    .build(privateKey);
            
            X509CertificateHolder h = certBuilder.build(signer);
             
            OvpnTls ovpnTls = new OvpnTls(null); // null = no parent
            ovpnTls.certificate = new JcaX509CertificateConverter().setProvider( "BC" )
                    .getCertificate(h);
            ovpnTls.privateKey = keyPair.getPrivate();
            ovpnTls.commonName = newCommonName;
            
            return ovpnTls;
        } catch (NoSuchAlgorithmException | OperatorCreationException  | CertIOException | CertificateException e) {

            // TODO some sort of "this code can't create cert on tis platform" status.
            return null;
        }
    }
    public String getCommonName() {
        return commonName;
    }
}
