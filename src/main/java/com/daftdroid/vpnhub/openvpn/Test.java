package com.daftdroid.vpnhub.openvpn;

import com.daftdroid.vpnhub.openvpn.options.CommonOptions;

public class Test {
    public static void main(String args[]) {
        System.out.println(new CommonOptions("dafttun").toString());
    }
}
