package com.daftdroid.vpnhub.openvpn.options;

public abstract class Option {

    public Option() {
    }
    
    protected abstract String getOptionComment();
    protected abstract String getOption();
    

}
