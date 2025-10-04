package com.fpt.careermate.services.impl;

import com.fpt.careermate.services.dto.request.AccountCreationRequest;
import com.fpt.careermate.services.dto.response.AccountResponse;
import com.fpt.careermate.services.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface AccountService {
    AccountResponse createAccount(AccountCreationRequest request) ;
    PageResponse<AccountResponse> getAccounts(Pageable pageable);
    void deleteAccount(int id);
}
