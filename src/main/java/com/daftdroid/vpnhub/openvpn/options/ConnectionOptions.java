package com.daftdroid.vpnhub.openvpn.options;

public class ConnectionOptions extends OptionGroup {

    // Whether we are the "hub" node, that broadly equates to server
    private final boolean hub;

    public ConnectionOptions(boolean hub) {
        this.hub = hub;
    }
    @Override
    public String getGroupComment() {
        // TODO Auto-generated method stub
        return null;
    }

    class TransportOption extends Option {

        private final boolean udp;
        
        @Override
        protected String getOptionComment() {
            return "Tunnel transport protocol. UDP is preferred if network conditions support it.";
        }
        
        TransportOption(boolean udp) {
            this.udp = udp;
        }

        @Override
        protected String getOption() {
            if (udp) {
                return "proto udp";
            }
            else if (hub) {
                return "proto tcp-server";
            }
            else {
                return "proto tcp-client";
            }
        }
    }
    
    class TlsMode extends Option {

        @Override
        protected String getOptionComment() {
            return "TLS infers a client-server relationship. The hub is the server";
        }

        @Override
        protected String getOption() {
            if (hub) {
                return "tls-server";
            }
            else {
                return "tls-client";
            }
        }
    }
}
