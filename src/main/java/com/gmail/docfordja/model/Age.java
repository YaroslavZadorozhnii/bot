package com.gmail.docfordja.model;

import org.springframework.security.core.GrantedAuthority;

public enum Age implements GrantedAuthority {
    BABY, CHILD, JUNIOR, TEENAGER, PREADULT, ADULT, OLD;

    @Override
    public String getAuthority() {
        return name();
    }
    public static Age stringToAge(String age){
        if("5-6 лет".equals(age)){return BABY;}
        if("7-9 лет".equals(age)){return CHILD;}
        if("10-12 лет".equals(age)){return JUNIOR;}
        if("13-14 лет".equals(age)){return TEENAGER;}
        if("15-17 лет".equals(age)){return PREADULT;}
        if("18+ лет".equals(age)){return ADULT;}
        if("35+ лет".equals(age)){return OLD;}
        return null;
    }
    public static String ageToString(Age age){
        if(age != null) {
            if (BABY.toString().equals(age.toString())) {
                return "5-6 лет";
            }
            if (CHILD.toString().equals(age.toString())) {
                return "7-9 лет";
            }
            if (JUNIOR.toString().equals(age.toString())) {
                return "10-12 лет";
            }
            if (TEENAGER.toString().equals(age.toString())) {
                return "13-14 лет";
            }
            if (PREADULT.toString().equals(age.toString())) {
                return "15-17 лет";
            }
            if (ADULT.toString().equals(age.toString())) {
                return "18+ лет";
            }
            if (OLD.toString().equals(age.toString())) {
                return "35+ лет";
            }
        }
        return null;
    }
}
