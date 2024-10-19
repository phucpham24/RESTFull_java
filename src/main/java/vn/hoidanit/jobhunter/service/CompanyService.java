package vn.hoidanit.jobhunter.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.dto.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.CompanyRepository;

@Service
public class CompanyService {
    private CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company handleSaveCompany(Company company) {
        return this.companyRepository.save(company);
    }

    public ResultPaginationDTO fetchAllCompany(Specification<Company> spec, Pageable pageable) {
        ResultPaginationDTO res = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        Page<Company> page = this.companyRepository.findAll(spec, pageable);

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(page.getNumberOfElements());
        mt.setTotal(page.getTotalElements());

        res.setMeta(mt);
        res.setResult(page.getContent());
        return res;
    }

    public void deleteCompany(long id) {
        this.companyRepository.deleteById(id);
    }

    public Company updateCompany(Company curCompany) {
        Optional<Company> companyOpt = this.companyRepository.findById(curCompany.getId());
        if (companyOpt.isPresent()) {
            Company c = companyOpt.get();
            c.setAddress(curCompany.getAddress());
            c.setDescription(curCompany.getDescription());
            c.setLogo(curCompany.getLogo());
            c.setName(curCompany.getName());

            return this.companyRepository.save(c);
        }
        return null;
    }

}
