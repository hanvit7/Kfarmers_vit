package com.leadplatform.kfarmers.model;

/**
 * App 중요 상태 정의
 */
public enum AppState {
    NONE(""),
    IS_RUNNING("");

    private String type = null;

    /**
     * 생성자.
     * 
     * @param t String
     */
    private AppState(final String t) {
        type = t;
    }

    @Override
    public String toString() {
        return type;
    }
}
