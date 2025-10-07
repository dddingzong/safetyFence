package com.project.paypass_renewal.util;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

class ZipCodeToLatLogUtilsTest {

    @Test
    @DisplayName("우편번호_위도경도_변환_테스트")
    void getLatLogFromZipCode() {
        // given
        String zipCode = "01675";
        // when
        Map<String, BigDecimal> latLogFromZipCode = ZipCodeToLatLogUtils.getLatLogFromZipCode(zipCode);

        System.out.println("latLogFromZipCode = " + latLogFromZipCode);

        BigDecimal latitude = latLogFromZipCode.get("latitude");
        BigDecimal longitude = latLogFromZipCode.get("longitude");

        // then
        System.out.println("latitude = " + latitude);
        System.out.println("longitude = " + longitude);


    }




}
