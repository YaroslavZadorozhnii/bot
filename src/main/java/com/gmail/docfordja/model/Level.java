package com.gmail.docfordja.model;

import org.springframework.security.core.GrantedAuthority;

public enum Level implements GrantedAuthority {
    BEGINNER, AMATEUR, PROFESSIONAL, ELITE;

    @Override
    public String getAuthority() {
        return name();
    }
    public static Level stringToLevel(String level){
        if("Начинающие".equals(level)){return BEGINNER;}
        if("Аматоры".equals(level)){return AMATEUR;}
        if("Профессионалы".equals(level)){return PROFESSIONAL;}
        if("Профессионалы-Элит".equals(level)){return ELITE;}
        return null;
    }
    public static String levelToString(Level level){
        if(level != null) {
            if (BEGINNER.toString().equals(level.toString())) {
                return "Начинающие";
            }
            if (AMATEUR.toString().equals(level.toString())) {
                return "Аматоры";
            }
            if (PROFESSIONAL.toString().equals(level.toString())) {
                return "Профессионалы";
            }
            if (ELITE.toString().equals(level.toString())) {
                return "Профессионалы-Элит";
            }
        }
        return null;
    }
}
