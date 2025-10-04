package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.Account;
import com.fpt.careermate.services.dto.request.AccountCreationRequest;
import com.fpt.careermate.services.dto.response.AccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "status", ignore = true)
    Account toAccount(AccountCreationRequest request);

    AccountResponse toAccountResponse(Account account);
}
