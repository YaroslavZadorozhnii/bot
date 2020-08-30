package com.gmail.docfordja.model;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    /* @Transient // <<---- Это отменит создание колонки в БД*/
    private Boolean notified = false;
    private  int stateId;
    private String username;
    private String password;
    private String phone;
    private String phone2;
    private boolean role;
    @Email
    private String email;
    private Boolean admin;
    private Long chatId;

    @ElementCollection(targetClass = Artist.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private List<Artist> artists;



    private long redactor = 0;
    private long ins = -1;
    private long noMus = -1;




    public User() {
    }

    public User(Long chatId, Integer state) {
        this.chatId = chatId;
        this.stateId = state;
        this.artists = new LinkedList<>();
    }
    public User(Long chatId, Integer state, String username) {
        this.chatId = chatId;
        this.stateId = state;
        this.username = username;
        this.artists = new LinkedList<>();
    }

    public User(Long chatId, Integer stateId, Boolean admin) {
        this.chatId = chatId;
        this.stateId = stateId;
        this.admin = admin;
        this.artists = new LinkedList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Integer getStateId() {
        return stateId;
    }

    public void setStateId(Integer stateId) {
        this.stateId = stateId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Boolean getNotified() {
        return notified;
    }

    public void setNotified(Boolean notified) {
        this.notified = notified;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public void setArtists(List<Artist> artists) {
        this.artists = artists;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public boolean isRole() {
        return role;
    }

    public void setRole(boolean role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", phone='" + phone + '\'' +
                ", phone2='" + phone2 + '\'' +
                ", chatId=" + chatId +
                '}';
    }


    public long getRedactor() {
        return redactor;
    }

    public void setRedactor(long redactor) {
        this.redactor = redactor;
    }

    public long getIns() {
        return ins;
    }

    public void setIns(long ins) {
        this.ins = ins;
    }

    public long getNoMus() {
        return noMus;
    }

    public void setNoMus(long noMus) {
        this.noMus = noMus;
    }

}
