package validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import models.Organisation;
import models.Regulator;
import play.db.jpa.JPA;
import play.i18n.Messages;

public class CheckRegulatorValidator implements ConstraintValidator<CheckRegulator, Regulator> {

	@Override
	public void initialize(CheckRegulator arg0) {
		
	}

	@Override
	public boolean isValid(Regulator ar,
			ConstraintValidatorContext context) {
	    if (ar != null && ar.getId() != null) {
    		ar = JPA.em().find(Regulator.class, ar.getId());
	    } else {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(Messages.get("validation.regulator.null"))
		  	          .addConstraintViolation();
			return false;	    	
	    }
	    
	    if (ar != null && ar.getId() != null) {
	    	return true;
	    }

		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(Messages.get("validation.regulator.invalid"))
	  	          .addConstraintViolation();
		return false;
	}
}
