package com.bjit.royalclub.royalclubfootball;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RoyalClubFootballApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoyalClubFootballApplication.class, args);
    }

}
