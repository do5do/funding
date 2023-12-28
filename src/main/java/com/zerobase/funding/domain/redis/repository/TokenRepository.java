package com.zerobase.funding.domain.redis.repository;

import com.zerobase.funding.domain.redis.entity.Token;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends CrudRepository<Token, String> {

    Optional<Token> findByAccessToken(String accessToken);
}
