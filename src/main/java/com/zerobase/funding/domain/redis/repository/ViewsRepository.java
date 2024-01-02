package com.zerobase.funding.domain.redis.repository;

import com.zerobase.funding.domain.redis.entity.Views;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViewsRepository extends CrudRepository<Views, String> {

}
