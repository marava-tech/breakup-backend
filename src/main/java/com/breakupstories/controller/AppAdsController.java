package com.breakupstories.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppAdsController {

    @GetMapping(value = "/app-ads.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getAppAds() {
        return "google.com, pub-3718276999761951, DIRECT, f08c47fec0942fa0\n";
    }
}
