package com.breakupstories.service;

public interface OTPService {
    
    boolean sendOtp(String email);
    
    boolean verifyOtp(String email, String providedOtp);
} 