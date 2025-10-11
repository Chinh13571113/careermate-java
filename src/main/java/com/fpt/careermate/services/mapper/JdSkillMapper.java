package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.JdSkill;
import com.fpt.careermate.services.dto.response.JdSkillResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JdSkillMapper {
    List<JdSkillResponse> toSetSkillResponse(List<JdSkill> jdSkill);
}
