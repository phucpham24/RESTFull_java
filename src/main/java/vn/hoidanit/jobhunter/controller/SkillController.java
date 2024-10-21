package vn.hoidanit.jobhunter.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import vn.hoidanit.jobhunter.domain.Skill;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.SkillService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import com.turkraft.springfilter.boot.Filter;

@Controller
@RequestMapping("/api/v1")
public class SkillController {
    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping("/skills")
    @ApiMessage("create a skill")
    public ResponseEntity<Skill> createASkill(@RequestBody Skill reqskill) throws IdInvalidException {
        // TODO: process POST request
        if (this.skillService.checkSkillName(reqskill.getName())) {
            throw new IdInvalidException(
                    "SkilName " + reqskill.getName() + " is existed, please using another name.");
        }
        Skill skill = this.skillService.handleCreateSkill(reqskill);
        return ResponseEntity.ok().body(skill);
    }

    @PutMapping("/skills")
    @ApiMessage("update a skill")
    public ResponseEntity<Skill> updateASkill(@RequestBody Skill reqskill) throws IdInvalidException {
        // check id
        if (this.skillService.handleFindSkillById(reqskill.getId()) == null) {
            throw new IdInvalidException(
                    "Skil " + reqskill.getName() + " is not existed, please using another skill.");
        }
        // check name
        if (this.skillService.checkSkillName(reqskill.getName())) {
            throw new IdInvalidException(
                    "SkilName " + reqskill.getName() + " is existed, please using another name.");
        }
        Skill DBskill = this.skillService.handleUpdateSkill(reqskill);
        return ResponseEntity.ok().body(DBskill);
    }

    @GetMapping("/skills")
    @ApiMessage("fetch all skills")
    public ResponseEntity<ResultPaginationDTO> fetchAllSkills(@Filter Specification<Skill> spec, Pageable pageable) {
        ResultPaginationDTO res = this.skillService.findAllSkill(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @DeleteMapping("/skills/{id}")
    @ApiMessage("delete a skill")
    public ResponseEntity<Void> deleteSkill(@PathVariable("id") long id) throws IdInvalidException {
        // check id
        Skill currentSkill = this.skillService.handleFindSkillById(id);
        if (currentSkill == null) {
            throw new IdInvalidException("Skill id = " + id + " is not existed");
        }

        this.skillService.handleDeteleSkill(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

}
