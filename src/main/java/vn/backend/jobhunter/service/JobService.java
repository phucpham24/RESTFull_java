package vn.backend.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.backend.jobhunter.domain.Company;
import vn.backend.jobhunter.domain.Job;
import vn.backend.jobhunter.domain.Skill;
import vn.backend.jobhunter.domain.response.ResultPaginationDTO;
import vn.backend.jobhunter.domain.response.job.ResCreateJobDTO;
import vn.backend.jobhunter.domain.response.job.ResUpdateJobDTO;
import vn.backend.jobhunter.repository.CompanyRepository;
import vn.backend.jobhunter.repository.JobRepository;
import vn.backend.jobhunter.repository.SkillRepository;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final CompanyRepository companyRepository;

    public JobService(JobRepository jobRepository, SkillRepository skillRepository,
            CompanyRepository companyRepository) {
        this.jobRepository = jobRepository;
        this.skillRepository = skillRepository;
        this.companyRepository = companyRepository;
    }

    public ResCreateJobDTO handleCreateJob(Job job) {

        // find skillcurrentJob
        if (job.getSkills() != null) {
            List<Long> skills = job.getSkills().stream().map(x -> x.getId())
                    .collect(Collectors.toList());

            List<Skill> checkSkills = this.skillRepository.findByIdIn(skills);
            job.setSkills(checkSkills);
        }

        // check company
        if (job.getCompany() != null) {
            Optional<Company> COptional = this.companyRepository.findById(job.getCompany().getId());
            if (COptional.isPresent()) {
                job.setCompany(COptional.get());
            }
        }
        Job currentJob = this.jobRepository.save(job);

        ResCreateJobDTO resCreateJobDTO = new ResCreateJobDTO();
        resCreateJobDTO.setId(currentJob.getId());
        resCreateJobDTO.setName(currentJob.getName());
        resCreateJobDTO.setSalary(currentJob.getSalary());
        resCreateJobDTO.setQuantity(currentJob.getQuantity());
        resCreateJobDTO.setLocation(currentJob.getLocation());
        resCreateJobDTO.setLevel(currentJob.getLevel());
        resCreateJobDTO.setStartDate(currentJob.getStartDate());
        resCreateJobDTO.setEndDate(currentJob.getEndDate());
        resCreateJobDTO.setActive(currentJob.isActive());
        resCreateJobDTO.setCreatedAt(currentJob.getCreatedAt());
        resCreateJobDTO.setCreatedBy(currentJob.getCreatedBy());

        if (currentJob.getSkills() != null) {
            List<String> listSkills = currentJob.getSkills().stream().map(
                    item -> item.getName()).collect(Collectors.toList());
            resCreateJobDTO.setSkills(listSkills);
        }

        return resCreateJobDTO;
    }

    public ResultPaginationDTO handleFetchAll(Specification<Job> spec, Pageable pageable) {
        Page<Job> pageUser = this.jobRepository.findAll(spec, pageable);

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

    public ResUpdateJobDTO handleUpdate(Job j, Job jobInDB) {
        // check skills
        if (j.getSkills() != null) {
            List<Long> reqSkills = j.getSkills()
                    .stream().map(x -> x.getId())
                    .collect(Collectors.toList());

            List<Skill> dbSkills = this.skillRepository.findByIdIn(reqSkills);
            jobInDB.setSkills(dbSkills);
        }

        // check company
        if (j.getCompany() != null) {
            Optional<Company> COptional = this.companyRepository.findById(j.getCompany().getId());
            if (COptional.isPresent()) {
                jobInDB.setCompany(COptional.get());
            }
        }

        jobInDB.setName(j.getName());
        jobInDB.setSalary(j.getSalary());
        jobInDB.setQuantity(j.getQuantity());
        jobInDB.setLocation(j.getLocation());
        jobInDB.setLevel(j.getLevel());
        jobInDB.setStartDate(j.getStartDate());
        jobInDB.setEndDate(j.getEndDate());
        jobInDB.setActive(j.isActive());
        // update job
        Job currentJob = this.jobRepository.save(j);

        // convert response
        ResUpdateJobDTO dto = new ResUpdateJobDTO();
        dto.setId(currentJob.getId());
        dto.setName(currentJob.getName());
        dto.setSalary(currentJob.getSalary());
        dto.setQuantity(currentJob.getQuantity());
        dto.setLocation(currentJob.getLocation());
        dto.setLevel(currentJob.getLevel());
        dto.setStartDate(currentJob.getStartDate());
        dto.setEndDate(currentJob.getEndDate());
        dto.setActive(currentJob.isActive());
        dto.setUpdatedAt(currentJob.getUpdatedAt());
        dto.setUpdatedBy(currentJob.getUpdatedBy());

        if (currentJob.getSkills() != null) {
            List<String> skills = currentJob.getSkills()
                    .stream().map(item -> item.getName())
                    .collect(Collectors.toList());
            dto.setSkills(skills);
        }

        return dto;
    }

    public void handleDelete(long id) {
        this.jobRepository.deleteById(id);
    }

    public Optional<Job> findJobById(long id) {
        return this.jobRepository.findById(id);
    }

}
