package com.daftdroid.vpnhub.openvpn;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.cert.Certificate;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
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
    
    private Certificate certificate;
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
    // TODO password
    public void loadData(String data) {
        
        PemObject ob;
        
        try (PemReader reader = new PemReader(new StringReader(data));) {
            while ((ob = reader.readPemObject()) != null) {

                String type = ob.getType();
                byte databytes[] = ob.getContent();
                KeyFactory kf;
                if ("RSA PRIVATE KEY".equals(type)) {
                    try {
                        kf = KeyFactory.getInstance("RSA"); }
                    catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException("RSA Not supported????", e); //TODO
                    }
                    
                    try {
                        
                        PKCS8EncodedKeySpec keyspec = new PKCS8EncodedKeySpec(databytes);
                        privateKey = kf.generatePrivate(keyspec);
                    } catch (InvalidKeySpecException e) {
                        System.out.println("Yikes");// TODO
                        e.printStackTrace();
                    }
                } else if ("CERTIFICATE".equals(type)) {
                    final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                    certificate = certFactory.generateCertificate(new ByteArrayInputStream(databytes));
                }
                
            }
        } catch (IOException | CertificateException e) {
            // TODO
            throw new RuntimeException("TODO Checked exceptions", e);
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
    
            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                    .build(keyPair.getPrivate());
            
            X509CertificateHolder h = certBuilder.build(signer);
             
            OvpnTls ovpnTls = new OvpnTls(null); // null = no parent
            ovpnTls.certificate = new JcaX509CertificateConverter().setProvider( "BC" )
                    .getCertificate(h);
            ovpnTls.privateKey = keyPair.getPrivate();
            return ovpnTls;
        } catch (NoSuchAlgorithmException | OperatorCreationException  | CertIOException | CertificateException e) {

            // TODO some sort of "this code can't create cert on tis platform" status.
            return null;
        }
    }
}
