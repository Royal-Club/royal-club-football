package com.bjit.royalclub.royalclubfootball.security.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresCaptainOrAdmin {
    String value() default "This operation requires captain or admin role";
}
