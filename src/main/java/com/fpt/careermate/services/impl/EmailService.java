package com.fpt.careermate.services.impl;

import com.fpt.careermate.util.ChangePassword;
import com.fpt.careermate.util.MailBody;

public interface EmailService {
    void sendSimpleEmail(MailBody mailBody);
    String verifyEmail(String email);
    String verifyOtp(String email, Integer otp);
    String changePassword(ChangePassword password, String email);
}
