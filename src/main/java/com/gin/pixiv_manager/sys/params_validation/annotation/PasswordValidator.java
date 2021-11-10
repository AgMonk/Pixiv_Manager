package com.gin.pixiv_manager.sys.params_validation.annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.gin.pixiv_manager.sys.params_validation.annotation.ValidatorUtils.changeMessage;

/**
 * @author bx002
 */
public class PasswordValidator implements ConstraintValidator<Password, String> {
    String prefix;

    @Override
    public void initialize(Password constraint) {
        this.prefix = constraint.prefix();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext context) {
        if (s == null || "".equals(s)) {
            changeMessage(context, prefix + "密码不允许为空");
            return false;
        }
        if (s.length() < 6) {
            changeMessage(context, prefix + "密码不能少于6位");
            return false;
        }

        return true;
    }
}
