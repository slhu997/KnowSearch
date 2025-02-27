package com.didichuxing.datachannel.arius.admin.remote.zeus.bean.constant;

public enum EcmActionEnum {
                           START("start"),
                           //    SCALE("scale"),
                           //    UPGRADE("upgrade"),
                           //    RESTART("restart"),
                           REMOVE("remove"),
                           //    MODIFY("modify"),

                           PAUSE("pause"), CONTINUE("continue"), REDO_FAILED("redo"), SKIP_FAILED("ignore"), CANCEL("cancel")

    ;

    private String action;

    EcmActionEnum(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}