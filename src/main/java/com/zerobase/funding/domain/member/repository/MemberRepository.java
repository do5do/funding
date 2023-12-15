package com.zerobase.funding.domain.member.repository;

import com.zerobase.funding.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
