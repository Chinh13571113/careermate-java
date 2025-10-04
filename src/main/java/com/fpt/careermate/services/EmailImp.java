package com.fpt.careermate.services;

import com.fpt.careermate.domain.Account;
import com.fpt.careermate.domain.ForgotPassword;
import com.fpt.careermate.repository.AccountRepo;
import com.fpt.careermate.repository.ForgotPasswordRepo;
import com.fpt.careermate.util.ChangePassword;
import com.fpt.careermate.util.MailBody;
import com.fpt.careermate.services.impl.EmailService;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class EmailImp implements EmailService {
    final JavaMailSender mailSender;
    @NonFinal
    @Value("${spring.mail.username}")
    String sender;

    AccountRepo accountRepo;
    ForgotPasswordRepo forgotPasswordRepo;
    PasswordEncoder passwordEncoder;


    @Override
    public void sendSimpleEmail(MailBody mailBody) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(mailBody.to());
        message.setSubject(mailBody.subject());
        message.setText(mailBody.text());

        mailSender.send(message);
    }

    @Override
    public String verifyEmail(String email) {
        String cleanEmail = email == null ? "" : email.trim().toLowerCase();
        validateEmail(cleanEmail);
        Account account = accountRepo.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        int otp = generateVerificationCode();
        MailBody mailBody = MailBody.builder()
                .to(email)
                .subject("Verification Code")
                .text("Your verification code is: " + otp)
                .build();
        ForgotPassword fp = ForgotPassword.builder()
                .otp(otp)
                .expiredAt(new Date(System.currentTimeMillis() + 70 * 1000))
                .account(account)
                .build();
        sendSimpleEmail(mailBody);
        forgotPasswordRepo.save(fp);
        return "Verification code sent to email";
    }

    private void validateEmail(String email) {
        if (email.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT);
        }
        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(regex)) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT);
        }
    }

    @Override
    public String verifyOtp(String email, Integer otp) {
        Account account = accountRepo.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        ForgotPassword fp =
                forgotPasswordRepo.findByOtpAndUser(otp,account).orElseThrow(()-> new AppException(ErrorCode.OTP_INVALID));
        if(fp.getExpiredAt().before(Date.from(Instant.now()))) {
            forgotPasswordRepo.deleteById(fp.getFpid());
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }
        return "Otp verified";
    }

    @Override
    public String changePassword(ChangePassword editPassword, String email) {
        if(!Objects.equals(editPassword.password(), editPassword.repeatPassword())){
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        String encodedPassword = passwordEncoder.encode(editPassword.password());
        accountRepo.updatePassword(email, encodedPassword);

        return "Password changed successfully";
    }


    private Integer generateVerificationCode() {
        Random random = new Random();
        return random.nextInt(100_000, 999_999);
    }
}
