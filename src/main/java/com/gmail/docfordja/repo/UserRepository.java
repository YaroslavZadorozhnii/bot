package com.gmail.docfordja.repo;

import com.gmail.docfordja.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.notified = false " +
            "AND u.phone IS NOT NULL AND u.email IS NOT NULL")
    List<User> findNewUsers();
    List<User> findAll();
    User findByChatId(long id);
    @Query("select u from User u where u.id = :id")
    User findById(@Param("id") long id);
}
