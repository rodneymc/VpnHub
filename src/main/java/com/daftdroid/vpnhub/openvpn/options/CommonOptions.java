package com.daftdroid.vpnhub.openvpn.options;

public class CommonOptions extends OptionGroup {

    // Default set of option
    public CommonOptions(String interfaceName) {
        super();
        
        addOption(new DeviceType());
        addOption(new PersistKey());
        addOption(new CompLZO());
        addOption(new Cipher());
        addOption(new InterfaceName(interfaceName));
    }

    @Override
    public String getGroupComment() {
        // TODO Auto-generated method stub
        return null;
    }
}

class DeviceType extends Option {

    DeviceType(boolean tun) {
        if (!tun) {
            throw new UnsupportedOptionException("TUN devices only");
        }
    }
    DeviceType() {
        this(true);
    }
    @Override
    protected String getOptionComment() {
        return "Device type - tun = layer 3 device";
    }

    @Override
    protected String getOption() {
        return "dev-type tun";
    }
}

class PersistKey extends Option {
    final boolean persist;
    PersistKey(boolean persist) {
        this.persist = persist;
    }
    
    PersistKey() {
        this(true);
    }
    
    @Override
    protected String getOptionComment() {
        return persist ? "Keep the encryption key when connection established" : null;
    }
    
    @Override
    protected String getOption() {
        return persist ? "persist-key" : null;
    }
}

class CompLZO extends Option {
    final boolean compression;
    CompLZO(boolean compression) {
        this.compression = compression;
    }
    CompLZO() {
        this(true);
    }
    
    @Override
    protected String getOptionComment() {
        return compression ? "Use zip like compression" : null;
    }
    @Override
    protected String getOption() {
        return compression ? "comp-lzo" : null;
    }
}

class Cipher extends Option {
    private static String CIPHER_RECOMMEND = "AES-256-CBC";
    private static String CIPHER_RECOMMEND_COMMENT = "Recommended cipher, https://openvpn.net/community-resources/how-to/#security latest update Dec 2018";
    private final String cipher;
    
    public Cipher(String cipher) {
        // TODO validate name of supplied Cipher
        this.cipher = cipher;
    }
    public Cipher() {
        this (CIPHER_RECOMMEND);
    }
    @Override
    protected String getOptionComment() {
        return cipher.equals(CIPHER_RECOMMEND) ?
                CIPHER_RECOMMEND_COMMENT : "Cipher";
    }
    @Override
    protected String getOption() {
        return "cipher "+ cipher;
    }
}

class InterfaceName extends Option {

    private final String name;
    
    public InterfaceName(String name) {
        // TODO validate interface name
        this.name = name;
    }
    @Override
    protected String getOptionComment() {
        return "Network interface name (shows up in ifconfig, ipconfig etc)";
    }

    @Override
    protected String getOption() {
        return "dev " + name;
    }
}
