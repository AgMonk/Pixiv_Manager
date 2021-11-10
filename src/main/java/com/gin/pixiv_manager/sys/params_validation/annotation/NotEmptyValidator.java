package com.gin.pixiv_manager.sys.params_validation.annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

import static com.gin.pixiv_manager.sys.params_validation.annotation.ValidatorUtils.changeMessage;


/**
 * @author bx002
 */
public class NotEmptyValidator implements ConstraintValidator<NotEmpty, Object> {
    private String prefix;

    @Override
    public void initialize(NotEmpty constraint) {
        this.prefix = constraint.value();
    }


    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj == null || "".equals(obj)) {
            changeMessage(context, prefix + "不允许为空");
            return false;
        }
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            if (list.size() == 0) {
                changeMessage(context, prefix + "不允许为空");
                return false;
            }
        }
        return true;
    }
}
