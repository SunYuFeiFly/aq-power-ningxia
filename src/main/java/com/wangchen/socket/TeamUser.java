package com.wangchen.socket;

import lombok.Data;

@Data
public class TeamUser {
    private String openId;

    private String name;
    private String avatar;
    private int honorId;
    private String honorName;
    private String achievementName;
    private int level;
    private int type;
    private int teamGameCountToday;

    private TeamWebSock tws;
}
