package vn.backend.jobhunter.service;

import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.backend.jobhunter.domain.Job;
import vn.backend.jobhunter.domain.Skill;
import vn.backend.jobhunter.domain.User;
import vn.backend.jobhunter.domain.response.ResultPaginationDTO;
import vn.backend.jobhunter.repository.SkillRepository;

@Service
public class SkillService {
    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public Skill handleCreateSkill(Skill skill) {
        return this.skillRepository.save(skill);
    }

    public boolean checkSkillName(String name) {
        return this.skillRepository.existsByName(name);
    }

    public Skill handleFindSkillById(long id) {
        return this.skillRepository.findById(id);
    }

    public Skill handleUpdateSkill(Skill skill) {
        Skill SkillOp = this.skillRepository.findById(skill.getId());
        if (SkillOp != null) {
            SkillOp.setName(skill.getName());
        }
        return this.skillRepository.save(SkillOp);
    }

    public ResultPaginationDTO findAllSkill(
            Specification<Skill> specification,
            Pageable pageable) {
        Page<Skill> pageUser = this.skillRepository.findAll(specification, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageUser.getTotalPages());
        mt.setTotal(pageUser.getTotalElements());
        rs.setMeta(mt);

        rs.setResult(pageUser.getContent());
        return rs;

    }

    public void handleDeteleSkill(long id) {
        // delete job (inside job skill table)
        Skill currentSkill = this.skillRepository.findById(id);
        List<Job> jobs = currentSkill.getJobs();
        for (Job job : jobs) {
            job.getSkills().remove(currentSkill);
        }

        // delete subscriber (inside subscriber_skill table)
        currentSkill.getSubscribers().forEach(subs -> subs.getSkills().remove(currentSkill));

        // delete skill
        this.skillRepository.delete(currentSkill);
    }

}
