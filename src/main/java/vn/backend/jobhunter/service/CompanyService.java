package vn.backend.jobhunter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.backend.jobhunter.domain.Company;
import vn.backend.jobhunter.domain.User;
import vn.backend.jobhunter.domain.response.ResultPaginationDTO;
import vn.backend.jobhunter.repository.CompanyRepository;
import vn.backend.jobhunter.repository.UserRepository;

@Service
public class CompanyService {
    private CompanyRepository companyRepository;
    private UserRepository userRepository;

    public CompanyService(CompanyRepository companyRepository, UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
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
        Optional<Company> companyOp = this.companyRepository.findById(id);
        if (companyOp.isPresent()) {
            Company com = companyOp.get();
            // fetch all user in company
            List<User> users = this.userRepository.findByCompany(com);
            this.userRepository.deleteAll(users);
        }

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

    public Optional<Company> findCompanyById(long id) {
        return this.companyRepository.findById(id);
    }

}
