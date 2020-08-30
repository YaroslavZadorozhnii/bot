package com.gmail.docfordja.repo;

import com.gmail.docfordja.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;


public interface ArtRepo extends CrudRepository<Artist, Long> {
   @Query("select a from Artist a where a.id = (select MAX(id) from Artist) and a.owner = :id and a.pay = false ")
   Artist getLastArtist(@Param("id") long id);
   @Query("select a from Artist a where a.owner = :id")
   List<Artist> findByUserId(@Param("id") long id);
   @Query("select a from Artist a where a.pay = true and a.owner = :id")
   List<Artist> findByPay(@Param("id") long id);
   @Query("select a from Artist a where a.id =:id")
   Artist findId(@Param("id")long id);
   @Query("select a from Artist a where a.doubleId = :did and a.owner = :id")
   List<Artist> findByDoubleId(@Param("id") long id, @Param("did") long did);
   @Query("update Artist a set a.level =:level where a.doubleId = :did and a.owner = :id")
   void updateLevel(@Param("level") Level level, @Param("did") long did, @Param("id") long id);
   @Query("select a from Artist a where a.musicPath is not null and a.owner = :id and a.pay = true")
   List<Artist> findIsMusic(@Param("id") long id);
   @Query("select a from Artist a where a.musicPath is null and a.owner = :id and a.pay = true")
   List<Artist> findIsNoMusic(@Param("id") long id);
 /* @Query("update Artist a set a.username = :username, a.lastname = :userFirstname," +
           "a.fathername = :userathername, a.sity = :sity, a.studio = :studio, a.checks = true," +
           "a.pay = true, a.file = :filePath, a.doubles = true, a.age = :partisipantAge," +
           "a.level = :partisipantLevel, a.view = :partisipantView where a.id = :id")
   void update(@Param("id") long id,
           @Param("username") String username,
           @Param("userFirstname") String userFirstname,
           @Param("userFathername") String userFathername,
           @Param("sity") String sity,
           @Param("studio") String studio,
           @Param("checks") boolean checks,
           @Param("pay") boolean pay,
           @Param("filePath") String filePath,
           @Param("isDouble") boolean isDouble,
           @Param("partisipantAge") Age partisipantAge,
           @Param("partisipantLevel") Level partisipantLevel,
           @Param("partisipantView") View partisipantView);
*/
}
