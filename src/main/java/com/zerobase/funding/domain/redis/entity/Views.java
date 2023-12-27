package com.zerobase.funding.domain.redis.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

@Getter
@AllArgsConstructor
@RedisHash(value = "views")
public class Views {

    @Id
    private String id;

    private Integer count;

    public Views increaseCount() {
        count++;
        return this;
    }
}
