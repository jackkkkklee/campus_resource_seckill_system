package ind.lcw.campus_seckill.service.ServiceImpl;

import ind.lcw.campus_seckill.entity.User;
import ind.lcw.campus_seckill.service.UserService;
import ind.lcw.campus_seckill.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Override
    public User getUserById(Long id) {
        return userMapper.getById(id);
    }
}
