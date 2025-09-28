package com.fpt.careermate.services;

import com.fpt.careermate.constant.PredefineRole;
import com.fpt.careermate.domain.Account;
import com.fpt.careermate.domain.Role;
import com.fpt.careermate.repository.AccountRepo;
import com.fpt.careermate.repository.RoleRepo;
import com.fpt.careermate.services.dto.request.AccountCreationRequest;
import com.fpt.careermate.services.dto.response.AccountResponse;
import com.fpt.careermate.services.impl.AccountImp;
import com.fpt.careermate.services.mapper.AccountMapper;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AccountService implements AccountImp {
    AccountRepo accountRepo;
    RoleRepo roleRepo;
    AccountMapper accountMapper;
    PasswordEncoder passwordEncoder;
    @Override
    public AccountResponse createAccount(AccountCreationRequest request) {
        if (accountRepo.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.DUPLICATE_EMAIL);


        Account user = accountMapper.toAccount(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepo.findById(PredefineRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);

        user = accountRepo.save(user);

        return accountMapper.toAccountResponse(user);
    }

}
