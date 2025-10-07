package com.project.paypass_renewal.repository;

import com.project.paypass_renewal.domain.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    UserAddress findByNumber(String number);

    @Query("SELECT u.homeStreetAddress FROM UserAddress u WHERE u.number = :number")
    String findHomeStreetAddressByNumber(@Param("number") String number);

}
