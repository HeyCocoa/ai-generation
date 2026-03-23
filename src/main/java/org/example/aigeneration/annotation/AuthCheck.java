package org.example.aigeneration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


//在方法上匹配切点
@Target (ElementType.METHOD)
//在运行时生效
@Retention (RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须有某个角色
     */
    String mustRole() default "";

    String mustUserId() default "";
}
