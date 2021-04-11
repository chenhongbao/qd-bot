package org.quantdirect.bot.tool;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

public class CtpTraderConfiguration implements Serializable {
    private String brokerId;
    private String userId;
    private String password;
    private String appId;
    private String authCode;
    private Collection<String> fronts;

    public CtpTraderConfiguration() {
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

    public String getAppId() {
        return appId;
    }

    public String getAuthCode() {
        return authCode;
    }

    public Collection<String> getFronts() {
        return fronts;
    }
}
