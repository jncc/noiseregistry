package validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import models.ActivityApplication;
import models.ActivityLocationDate;
import play.Logger;
import play.db.jpa.JPA;

public class CheckActivityLocationDateValidator implements ConstraintValidator<CheckActivityLocationDate, ActivityLocationDate> {

	@Override
	public void initialize(CheckActivityLocationDate arg0) {
		
	}

	@Override
	public boolean isValid(ActivityLocationDate ald,
			ConstraintValidatorContext context) {
		
		boolean bHasErrors = false;
		ActivityApplication aa = ald.getActivitylocation().getAa();
	    if (aa != null) {
	    	if (aa.getId() != null && aa.getDate_start()==null) {
	    		aa = JPA.em().find(ActivityApplication.class, aa.getId());
	    	}
	    }
	    if (aa.getDate_start()!=null) {
	    	Logger.error("Date Got aa with id: " + Long.toString(aa.getId()) + " status: " + aa.getStatus());
	    } else {
	    	//All validation depends on getting hold of start (and end) date from activity application
	    	return true;
	    }
		
		if (aa.getDate_start() != null) {
			if (ald.getActivity_date().before(aa.getDate_start())) {
				context.disableDefaultConstraintViolation();
    			context.buildConstraintViolationWithTemplate("{error.activitybeforestart}")
			  	          .addConstraintViolation();
    			bHasErrors = true;
			}
		}
		if (aa.getDate_end() != null) {
			if (ald.getActivity_date().after(aa.getDate_end())) {
				context.disableDefaultConstraintViolation();
    			context.buildConstraintViolationWithTemplate("{error.activityafterend}")
			  	          .addConstraintViolation();
    			bHasErrors = true;
			}
		}
		Logger.debug("ActivityLocationDateValidator returning: " + Boolean.toString(!bHasErrors));
		return !bHasErrors;
	}
}
