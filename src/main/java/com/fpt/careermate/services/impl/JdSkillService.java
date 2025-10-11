package com.fpt.careermate.services.impl;

import com.fpt.careermate.services.dto.response.JdSkillResponse;

import java.util.List;

public interface JdSkillService {
    void createSkill(String name);
    List<JdSkillResponse> getAllSkill();
}
