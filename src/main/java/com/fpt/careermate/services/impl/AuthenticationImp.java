package com.fpt.careermate.services.impl;

import com.fpt.careermate.domain.Account;
import com.fpt.careermate.services.dto.request.AuthenticationRequest;
import com.fpt.careermate.services.dto.request.IntrospecRequest;
import com.fpt.careermate.services.dto.request.LogoutRequest;
import com.fpt.careermate.services.dto.request.RefreshRequest;
import com.fpt.careermate.services.dto.response.AuthenticationResponse;
import com.fpt.careermate.services.dto.response.IntrospecResponse;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;

public interface AuthenticationImp {
    IntrospecResponse introspect(IntrospecRequest request) throws JOSEException, ParseException;
    SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException;
    AuthenticationResponse authenticate(AuthenticationRequest request);
    void logout(LogoutRequest request) throws ParseException, JOSEException;
    AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;
    String generateToken(Account account,boolean isRefresh);
    String buildScope(Account account);


}
