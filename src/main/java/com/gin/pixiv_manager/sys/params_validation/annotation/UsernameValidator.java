package com.gin.pixiv_manager.sys.params_validation.annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.gin.pixiv_manager.sys.params_validation.annotation.ValidatorUtils.changeMessage;


/**
 * @author bx002
 */
public class UsernameValidator implements ConstraintValidator<Username, String> {
    @Override
    public void initialize(Username constraint) {
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext context) {
        if (s == null || "".equals(s)) {
            changeMessage(context, "用户名不允许为空");
            return false;
        }
        if (s.length() < 6) {
            changeMessage(context, "用户名不能少于6位");
            return false;
        }

        return true;
    }

}
