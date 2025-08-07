package org.example.aigeneration.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.example.aigeneration.exception.BusinessException;
import org.example.aigeneration.exception.ErrorCode;
import org.example.aigeneration.mapper.UserMapper;
import org.example.aigeneration.model.dto.UserQueryRequest;
import org.example.aigeneration.model.entity.User;
import org.example.aigeneration.model.enums.UserRoleEnum;
import org.example.aigeneration.model.vo.LoginUserVO;
import org.example.aigeneration.model.vo.UserVO;
import org.example.aigeneration.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.aigeneration.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 服务层实现。
 *
 * @author <a href="https://gitee.com/kokoa123">kokoa123</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    public long userRegister(String userAccount, String userPassword, String checkPassword){
        //校验
        if( StrUtil.hasBlank(userAccount, userAccount, checkPassword) ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册信息不能为空");
        }
        if( userAccount.length() < 4 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if( userPassword.length() < 8 || checkPassword.length() < 8 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if( !userPassword.equals(checkPassword) ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        //判断用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if( count > 0 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户重复");
        }
        //密码加密
        String encryptPassword = encryptPassword(userPassword);
        //添加用户到数据库
        User user = User.builder()
                .userAccount(userAccount)
                .userPassword(encryptPassword)
                .userRole(UserRoleEnum.USER.getValue())
                .userName("默认用户")
                .createTime(LocalDateTime.now())
                .build();
        boolean save = this.save(user);
        if( !save ){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败, 数据库错误");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO getLoginUserVO(User user){
        if( user==null ){
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request){
        //校验参数
        if( StrUtil.hasBlank(userAccount, userAccount) ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册信息不能为空");
        }
        if( userAccount.length() < 4 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if( userPassword.length() < 8 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        //加密
        String encryptPassword = encryptPassword(userPassword);
        //查询数据库
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        if( user==null ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //记录用户登录
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        //封装VO对象返回给前端
        return getLoginUserVO(user);
    }

    @Override
    public String encryptPassword(String password){
        String salt = "kokoa";
        return DigestUtils.md5DigestAsHex((salt + password).getBytes());
    }

    public User getLoginUser(HttpServletRequest request){
        //取出用户信息并判断用户是否登录
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if( user==null || user.getId()==null ){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        //获取数据库中的最新用户信息
        User currentUser = this.getById(user.getId());
        if( currentUser==null ){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        return currentUser;
    }

    public boolean userLogout(HttpServletRequest request){
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if( user==null ){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }


    public UserVO getUserVO(User user){
        if( user==null ){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    public List<UserVO> getUserVOList(List<User> userList){
        if( CollUtil.isEmpty(userList) ){
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest){
        if( userQueryRequest==null ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        return QueryWrapper.create()
                .eq("id", id)
                .eq("userRole", userRole)
                .like("userAccount", userAccount)
                .like("userName", userName)
                .like("userProfile", userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }
}
