package vn.backend.jobhunter.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.turkraft.springfilter.builder.FilterBuilder;
import com.turkraft.springfilter.converter.FilterSpecificationConverter;

import vn.backend.jobhunter.domain.Company;
import vn.backend.jobhunter.domain.Job;
import vn.backend.jobhunter.domain.Resume;
import vn.backend.jobhunter.domain.User;
import vn.backend.jobhunter.domain.response.ResultPaginationDTO;
import vn.backend.jobhunter.domain.response.job.ResCreateJobDTO;
import vn.backend.jobhunter.domain.response.job.ResUpdateJobDTO;
import vn.backend.jobhunter.domain.response.resume.ResCreateResumeDTO;
import vn.backend.jobhunter.domain.response.resume.ResUpdateResumeDTO;
import vn.backend.jobhunter.service.ResumeService;
import vn.backend.jobhunter.service.UserService;
import vn.backend.jobhunter.util.SecurityUtil;
import vn.backend.jobhunter.util.annotation.ApiMessage;
import vn.backend.jobhunter.util.error.IdInvalidException;

import com.turkraft.springfilter.boot.Filter;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("api/v1/")
public class ResumeController {
    private ResumeService resumeService;
    private UserService userService;

    private final FilterBuilder filterBuilder;
    private final FilterSpecificationConverter filterSpecificationConverter;

    public ResumeController(
            ResumeService resumeService,
            UserService userService,
            FilterBuilder filterBuilder,
            FilterSpecificationConverter filterSpecificationConverter) {
        this.resumeService = resumeService;
        this.userService = userService;
        this.filterBuilder = filterBuilder;
        this.filterSpecificationConverter = filterSpecificationConverter;
    }

    @PostMapping("/resumes")
    @ApiMessage("Create a resume")
    public ResponseEntity<ResCreateResumeDTO> createCV(@RequestBody Resume resume) throws IdInvalidException {
        if (resume == null) {
            throw new IdInvalidException("Resume can not be empty, add resume");
        }
        // check id exists
        boolean isIdExist = this.resumeService.checkResumeExistByUserAndJob(resume);
        if (!isIdExist) {
            throw new IdInvalidException("User id/Job id không tồn tại");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(this.resumeService.handleCreateCV(resume));
    }

    @PutMapping("/resumes")
    @ApiMessage("Update a resume")
    public ResponseEntity<ResUpdateResumeDTO> putMethodName(@RequestBody Resume resume) throws IdInvalidException {
        // check id exist
        Optional<Resume> reqResumeOptional = this.resumeService.fetchById(resume.getId());
        if (reqResumeOptional.isEmpty()) {
            throw new IdInvalidException("Resume với id = " + resume.getId() + " không tồn tại");
        }
        Resume currentResume = reqResumeOptional.get();
        currentResume.setStatus(resume.getStatus());

        return ResponseEntity.ok().body(this.resumeService.handleUpdateCV(currentResume));
    }

    @DeleteMapping("/resumes/{id}")
    @ApiMessage("Delete a resume by id")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) throws IdInvalidException {
        Optional<Resume> reqResumeOptional = this.resumeService.fetchById(id);
        if (reqResumeOptional.isEmpty()) {
            throw new IdInvalidException("Resume với id = " + id + " không tồn tại");
        }

        this.resumeService.handleDeleteCV(id);
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/resumes")
    @ApiMessage("Fetch all resume with paginate")
    public ResponseEntity<ResultPaginationDTO> fetchAll(
            @Filter Specification<Resume> spec,
            Pageable pageable) {
        List<Long> arrJobIds = new ArrayList<>();
        String email = SecurityUtil.getCurrentUserLogin().isPresent() == true ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        User currentUser = this.userService.handleFindUserByUsername(email);
        if (currentUser != null) {
            Company userCompany = currentUser.getCompany();
            if (userCompany != null) {
                List<Job> companyJobs = userCompany.getJobs();
                if (companyJobs != null && companyJobs.size() > 0) {
                    arrJobIds = companyJobs.stream().map(i -> i.getId()).collect(Collectors.toList());
                }
            }
        }
        // filter to job in domain folder
        Specification<Resume> jobInSpec = filterSpecificationConverter
                .convert(filterBuilder.field("job").in(filterBuilder.input(arrJobIds)).get());

        Specification<Resume> finalSpec = jobInSpec.and(spec);
        return ResponseEntity.ok().body(this.resumeService.fetchAllResume(finalSpec, pageable));
    }

    @PostMapping("/resumes/by-user")
    @ApiMessage("Get list resumes by user")
    public ResponseEntity<ResultPaginationDTO> fetchResumesByUser(Pageable pageable) {
        // TODO: process POST request

        return ResponseEntity.ok().body(this.resumeService.fetchResumeByUser(pageable));
    }

}
