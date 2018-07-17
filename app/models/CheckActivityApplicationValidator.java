package models;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CheckActivityApplicationValidator implements ConstraintValidator<CheckActivityApplication, ActivityApplication> {

	@Override
	public void initialize(CheckActivityApplication arg0) {
	}

	/**
	 * DOESN'T check whether the ActivityApplication is valid.  We need to unset the unwanted
	 * parts of the ActivityApplication before further processing.  In this version of Play, an
	 * error gets thrown unless true is returned. 
	 */
	@Override
	public boolean isValid(ActivityApplication aa,
			ConstraintValidatorContext context) {
		aa.unsetNotNeeded();
		return true;
	}

}
