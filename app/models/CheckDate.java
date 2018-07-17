package models;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ FIELD, TYPE , ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = CheckDateValidator.class)
public @interface CheckDate {
    String message() default "this is a message";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
	String earliest() default "";
}
