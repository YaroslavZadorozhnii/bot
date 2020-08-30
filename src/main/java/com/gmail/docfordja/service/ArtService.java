package com.gmail.docfordja.service;

import com.gmail.docfordja.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gmail.docfordja.repo.ArtRepo;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

@Service
public class ArtService {

    private final ArtRepo artRepo;

    public ArtService(ArtRepo artRepo) {
        this.artRepo = artRepo;
    }

    /*@Transactional
    public void updateArtist(Artist artist) {
        artRepo.update( artist.getId(),
            artist.getUsername(),
            artist.getLastname(),
            artist.getFathername(),
            artist.getSity(),
            artist.getStudio(),
            artist.isChecks(),
            artist.isPay(),
            artist.getFile(),
            artist.isDoubles(),
            artist.getAge(),
            artist.getLevel(),
            artist.getView()
                );
    }*/
    @Transactional
    public Artist getLastArtist(User user){return artRepo.getLastArtist(user.getId());}
    @Transactional
    public void addArtist(Artist artist) {
            artRepo.save(artist);
    }
    @Transactional
    public void updateArtist(Artist artist) {
        artRepo.save(artist);
    }

    @Transactional(readOnly = true)
    public List<Artist> findById(long id) {
        return artRepo.findByUserId(id);
    }
    @Transactional(readOnly = true)
    public List<Artist> findByDoubleId(long id, long did) {
        return artRepo.findByDoubleId(id, did);
    }
    @Transactional(readOnly = true)
    public List<Artist> findByPay(long id) {
        return artRepo.findByPay(id);
    }
    @Transactional
    public Artist byId(long id){return artRepo.findId(id);}
    @Transactional
    public void updateLevel(Level level, long did, long id){ artRepo.updateLevel(level, did, id);}
    @Transactional(readOnly = true)
    public List<Artist> findIsMusic(long id) {
        return artRepo.findIsMusic(id);
    }
    @Transactional(readOnly = true)
    public List<Artist> findIsNoMusic(long id) {
        return artRepo.findIsNoMusic(id);
    }

    public Artist working(User user){
        List<Artist> artists = artRepo.findByUserId(user.getId());
        Artist artist = new Artist(user);
        if(artists.size() > 0){
            for (Artist a : artists){
                if(!a.isPay() && a.getId() > artist.getId()){artist = a;}
            }
        }
        return artist;
    }
}
