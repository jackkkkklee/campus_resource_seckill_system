package ind.lcw.campus_seckill.entity;

import lombok.Data;

@Data
public class Role {
    private Long id;
    private String name;

    public Role(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}

