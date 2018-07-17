package validators;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = CheckNoiseProducerValidator.class)
public @interface CheckNoiseProducer {
    String message() default "this is a message";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
