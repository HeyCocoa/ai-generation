package org.example.aigeneration.aop;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.aigeneration.annotation.AuthCheck;
import org.example.aigeneration.exception.BusinessException;
import org.example.aigeneration.exception.ErrorCode;
import org.example.aigeneration.model.entity.User;
import org.example.aigeneration.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.example.aigeneration.constant.UserConstant.ADMIN_ROLE;

@Aspect
@Component
public class AuthInterceptor{

    @Resource
    private UserService userService;

    @Around ("@annotation(authCheck)")
    public Object Interceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable{
        //获取必要权限
        String mustRole = authCheck.mustRole();
        String mustUserId = authCheck.mustUserId();
        //若不需要权限则直接放行
        if( mustRole==null ) return joinPoint.proceed();
        //获取当前用户
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User user = userService.getLoginUser(request);
        String userId = String.valueOf(user.getId());
        //若用户没有权限，拒绝放行
        if( user.getUserRole()==null ){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //本人
        else if( !mustUserId.isEmpty() && !mustUserId.equals(userId) ){
            return joinPoint.proceed();
        }
        //如果不是本人，则要求有管理员权限，拒绝放行
        else if( mustRole.equals(ADMIN_ROLE) && user.getUserRole().equals(ADMIN_ROLE) ){
            return joinPoint.proceed();
        }
        //拒绝放行
        else{
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }
}
