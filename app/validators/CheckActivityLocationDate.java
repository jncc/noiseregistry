package validators;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ TYPE , ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = CheckActivityLocationDateValidator.class)
public @interface CheckActivityLocationDate {
    String message() default "this is a message";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}