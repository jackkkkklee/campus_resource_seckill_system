package ind.lcw.campus_seckill.entity;

import lombok.Data;

import java.util.List;
@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private boolean enabled;
    private List<Role> roles;
    private String email;

}
