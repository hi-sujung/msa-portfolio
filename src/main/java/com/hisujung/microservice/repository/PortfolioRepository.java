package com.hisujung.microservice.repository;


import com.hisujung.microservice.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    //@Query("SELECT p FROM Portfolio p WHERE p.member.id = :mid ORDER BY p.id DESC ")
    //List<Portfolio> findAllDesc(@Param("mid") Long mid);

    //List<Portfolio> findByMember(Member member);
}
