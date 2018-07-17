package validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import models.ActivityLocation;
import play.Logger;

public class CheckActivityLocationValidator implements ConstraintValidator<CheckActivityLocation, ActivityLocation> {

	@Override
	public void initialize(CheckActivityLocation arg0) {
		
	}

	@Override
	public boolean isValid(ActivityLocation al,
			ConstraintValidatorContext context) {
		
		boolean bHasErrors = false;
		return !bHasErrors;
	}

}
