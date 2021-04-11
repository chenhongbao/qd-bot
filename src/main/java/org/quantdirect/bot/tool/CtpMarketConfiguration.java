package org.quantdirect.bot.tool;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

public class CtpMarketConfiguration implements Serializable {
    private String brokerId;
    private String userId;
    private String password;
    private Collection<String> fronts;

    public CtpMarketConfiguration() {
        fronts = new LinkedList<>();
    }

    public String getBrokerId() {
        return brokerId;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public Collection<String> getFronts() {
        return fronts;
    }
}
