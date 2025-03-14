package vn.backend.jobhunter.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import vn.backend.jobhunter.util.constant.GenderEnum;

@Getter
@Setter
public class ResUpdateUserDTO {
    private Long id;

    private String name;
    private GenderEnum gender;
    private String address;

    private int age;
    private Instant updateAt;
    private CompanyUser company;

    @Getter
    @Setter
    public static class CompanyUser {
        private long id;
        private String name;
    }
}
