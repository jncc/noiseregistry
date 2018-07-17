package validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import models.NoiseProducer;
import play.db.jpa.JPA;
import play.i18n.Messages;

public class CheckNoiseProducerValidator implements ConstraintValidator<CheckNoiseProducer, NoiseProducer> {

	@Override
	public void initialize(CheckNoiseProducer arg0) {
		
	}

	@Override
	public boolean isValid(NoiseProducer anp,
			ConstraintValidatorContext context) {

	    if (anp != null && anp.getId() != null) {
	    	anp = JPA.em().find(NoiseProducer.class, anp.getId());
	    } else {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(Messages.get("validation.noiseproducer.null"))
		  	          .addConstraintViolation();
			return false;	    	
	    }
	    
	    if (anp != null && anp.getOrganisation() != null) {
	    	return true;
	    }

		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(Messages.get("validation.noiseproducer.invalid"))
	  	          .addConstraintViolation();
		return true;
	}
}
