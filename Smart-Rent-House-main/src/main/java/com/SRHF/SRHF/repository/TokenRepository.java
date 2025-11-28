package com.SRHF.SRHF.repository;

import com.SRHF.SRHF.entity.Token;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TokenRepository extends CrudRepository<Token, Long> {

    Optional<Token> findById(String token);

    Optional<Token> findByToken(String token);
}
