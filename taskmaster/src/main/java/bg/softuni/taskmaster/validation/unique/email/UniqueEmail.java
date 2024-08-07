package bg.softuni.taskmaster.validation.unique.email;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueEmailValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UniqueEmail {

    String message() default "{validation.field.unique}";

    boolean checkForLoggedUser() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
