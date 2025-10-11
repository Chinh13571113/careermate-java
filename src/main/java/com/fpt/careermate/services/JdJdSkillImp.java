package com.fpt.careermate.services;

import com.fpt.careermate.domain.JdSkill;
import com.fpt.careermate.repository.JdSkillRepo;
import com.fpt.careermate.services.dto.response.JdSkillResponse;
import com.fpt.careermate.services.impl.JdSkillService;
import com.fpt.careermate.services.mapper.JdSkillMapper;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JdJdSkillImp implements JdSkillService {

    JdSkillRepo jdSkillRepo;
    JdSkillMapper jdSkillMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public void createSkill(String name) {
        // Check jdSkill name
        Optional<JdSkill> exSkill = jdSkillRepo.findSkillByName(name);
        if (exSkill.isPresent()) throw new AppException(ErrorCode.SKILL_EXISTED);

        JdSkill jdSkill = new JdSkill();
        jdSkill.setName(name);
        jdSkillRepo.save(jdSkill);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<JdSkillResponse> getAllSkill() {
        return jdSkillMapper.toSetSkillResponse(jdSkillRepo.findAll());
    }

}
