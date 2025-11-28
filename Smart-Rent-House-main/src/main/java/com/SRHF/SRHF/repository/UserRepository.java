package com.SRHF.SRHF.repository;

import com.SRHF.SRHF.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByemail(String email);

}
