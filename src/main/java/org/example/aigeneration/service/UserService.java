package org.example.aigeneration.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.example.aigeneration.model.dto.UserQueryRequest;
import org.example.aigeneration.model.entity.User;
import org.example.aigeneration.model.vo.LoginUserVO;
import org.example.aigeneration.model.vo.UserVO;

import java.util.List;

/**
 * 服务层。
 *
 * @author <a href="https://gitee.com/kokoa123">kokoa123</a>
 */
public interface UserService extends IService<User>{

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    LoginUserVO getLoginUserVO(User user);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    String encryptPassword(String password);

    User getLoginUser(HttpServletRequest request);

    boolean userLogout(HttpServletRequest request);
}
