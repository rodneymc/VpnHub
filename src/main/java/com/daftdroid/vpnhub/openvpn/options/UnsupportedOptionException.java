package com.daftdroid.vpnhub.openvpn.options;

public class UnsupportedOptionException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public UnsupportedOptionException() {
    }

    public UnsupportedOptionException(String message) {
        super(message);
    }

    public UnsupportedOptionException(Throwable cause) {
        super(cause);
    }

    public UnsupportedOptionException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
