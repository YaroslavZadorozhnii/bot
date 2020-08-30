package com.gmail.docfordja.model;
import org.springframework.security.core.GrantedAuthority;

public enum View implements GrantedAuthority {
    PoleSport, PoleArt, PoleShow, AerialSilks, AerialHoop, OriginalGenre;

    @Override
    public String getAuthority() {
        return name();
    }

    public static View stringToView(String view){
        if("Pole Sport".equals(view)){return PoleSport;}
        if("Pole Art".equals(view)){return PoleArt;}
        if("Aerial Hoop".equals(view)){return AerialHoop;}
        if("Aerial Silks".equals(view)){return AerialSilks;}
        if("Original Genre".equals(view)){return OriginalGenre;}
        return null;
    }
}
