package cn.jjxy.security.distributed.uaa.service;

import cn.jjxy.security.distributed.uaa.dao.UserDao;
import cn.jjxy.security.distributed.uaa.modle.UserDto;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author qinggu
 * @date 2021年09月01日14:39
 */
@Service
/*
* 实现security的 UserDetailsService
* */
public class SpringDataUserDetailsService implements UserDetailsService {
    @Autowired
    private UserDao userDao;
    /*
    * 根据账号查询用户信息
    * */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        /*
        * 构造一个user对象，看看我们重写的loadUserByUsername是被执行
        * */
//        UserDetails userDeatils = User.withUsername(username).password("$2a$10$gIMyIk4m7ID3XepOEy4okOZPlCCrDTZzzt2RSyVDJACLZiLI.f3Q.").authorities("p1").build();
//        return userDeatils;
        /*
        * 从数据库查询用户对象
        * */
        UserDto user = userDao.getUserByUsername(username);
        if (user == null){
            //当用户查询不到，返回null，由provider来抛出异常
            return null;
        }
        /*
        * 根据用户id查询用户权限
        * */
        List<String> permissionsList = userDao.findPermissionsByUserId(user.getId());
        String[] permissionArray = new String[permissionsList.size()];
        permissionsList.toArray(permissionArray);
        //直接将用户的全部信息注入到UserServiceDeatil的  userName属性中
        String principal = JSON.toJSONString(user);
        UserDetails userDetails = User.withUsername(principal).password(user.getPassword()).authorities(permissionArray).build();
        return userDetails;
    }
}
