package com.gmail.docfordja.model;

import com.gmail.docfordja.bot.Utils;

import javax.persistence.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Entity
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;
    private long doubleId;
    private long owner;
    private String adres;
    private String username;
    private String lastname;
    private String fathername;
    private String sity;
    private String studio;
    private boolean checks;
    private boolean pay;
    private boolean insurance;
    private boolean doubles;
    private long musicMessage;
    private String musicPath;
    private boolean sale;
    @Enumerated(EnumType.STRING)
    private Age age;
    @Enumerated(EnumType.STRING)
    private Level level;
    @Enumerated(EnumType.STRING)
    private View view;


    private int duration = -1;
    private boolean control;
    private boolean sendMusicCheck;

    public Artist(User user) {
        this.owner = user.getId();
        this.user = user;
    }

    public Artist() {
    }

    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

    public boolean isSale() {
        return sale;
    }

    public void setSale(boolean sale) {
        this.sale = sale;
    }

    public long getMusicMessage() {
        return musicMessage;
    }

    public void setMusicMessage(long musicMessage) {
        this.musicMessage = musicMessage;
    }

    public long getDoubleId() {
        return doubleId;
    }

    public void setDoubleId(long doubleId) {
        this.doubleId = doubleId;
    }

    public boolean isInsurance() {
        return insurance;
    }

    public void setInsurance(boolean insurance) {
        this.insurance = insurance;
    }

    public String getAdres() {
        return adres;
    }

    public void setAdres(String adres) {
        this.adres = adres;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSity() {
        return sity;
    }

    public void setSity(String sity) {
        this.sity = sity;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public long getOwner() {
        return owner;
    }

    public void setOwner(long owner) {
        this.owner = owner;
    }

    public boolean isPay() {
        return pay;
    }

    public void setPay(boolean pay) {
        this.pay = pay;
    }

    public boolean isChecks() {
        return checks;
    }

    public void setChecks(boolean checks) {
        this.checks = checks;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFathername() {
        return fathername;
    }

    public void setFathername(String fathername) {
        this.fathername = fathername;
    }

    public boolean isDoubles() {
        return doubles;
    }

    public void setDoubles(boolean doubles) {
        this.doubles = doubles;
    }

    public Age getAge() {
        return age;
    }

    public void setAge(Age age) {
        this.age = age;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    @Override
    public String toString() {
            String[] doubleName = new String[0];
            String perform = "Сольное выступление";
            String artistName = username + " " + fathername + " " + lastname;
            if (doubles) {
                perform = "Дуетное выступление";
                try {
                doubleName = (username + " / " + fathername + " / " + lastname).split(" / ");
                artistName = doubleName[0] + " " + doubleName[2] + " " + doubleName[4] + " / " +
                        doubleName[1] + " " + doubleName[3] + " " + doubleName[5];
            }catch (Exception e){}
        }
        return "Данные участника:\n" +
                perform + "\n" +
                artistName + "\n"  +
                "Город: " + sity + "\n"  +
                "Студия: " + studio + "\n"  +
                "Направлениe: " + view.toString().split("(?=\\p{Upper})")[0] + " " +
                view.toString().split("(?=\\p{Upper})")[1] + "\n" +
                "Возрастная группа: " + Age.ageToString(age) + "\n" +
                "Соревновательная группа: " + Level.levelToString(level);
    }
    public String getPath(){
        String response = "";
        try { String url = "https://api.telegram.org/bot1149330908:AAGvUyOopaFD3eaaAlnPtmWsVk8JKmwK4KE/getFile?file_id=" + this.getMusicPath();
            System.out.println(url.toString());
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        InputStreamReader in = new InputStreamReader(con.getInputStream());
        BufferedReader br = new BufferedReader(in);
        String text = "";
        while ((text = br.readLine()) != null) {
                response += text;}
        con.disconnect();
        in.close();
        }catch (Exception e){}
        return response.substring(response.indexOf("file_path\":\"") + "file_path\":\"".length(), response.length() -3);
    }


    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isControl() {
        return control;
    }

    public void setControl(boolean control) {
        this.control = control;
    }

    public boolean isSendMusicCheck() {
        return sendMusicCheck;
    }

    public void setSendMusicCheck(boolean sendMusicCheck) {
        this.sendMusicCheck = sendMusicCheck;
    }
}

