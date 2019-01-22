package com.daftdroid.vpnhub.openvpn.options;

import java.util.ArrayList;
import java.util.List;

public abstract class OptionGroup {
    private final List<Option> group;
    private final String GROUP_COMMENT_START_END = "##################################\n";
    
    public OptionGroup() {
        group = new ArrayList<Option>();
    }
    public void addOption(Option o) {
        group.add(o);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(GROUP_COMMENT_START_END);
        sb.append(getGroupComment()+"\n"); // TODO could be multi-line?
        sb.append(GROUP_COMMENT_START_END);
        for (Option o: group) {
            String comment = o.getOptionComment();
            String val = o.getOption();
            
            if (comment != null) {
                sb.append("#"+comment+"\n"); // TODO could be multi-line?
            }
            
            if (val != null) {
                sb.append(val+"\n");
            }
        }
        sb.append("\n");
        
        return sb.toString();
    }
    
    public abstract String getGroupComment();
    
}
