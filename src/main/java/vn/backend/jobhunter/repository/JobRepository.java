package vn.backend.jobhunter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import vn.backend.jobhunter.domain.Job;
import vn.backend.jobhunter.domain.Skill;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    List<Job> findBySkillsIn(List<Skill> skills);
}
