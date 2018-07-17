package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import play.Logger;
import play.data.Form;
import play.data.validation.ValidationError;
import static play.libs.F.*;
import play.Logger;

public class FormLimitless<T> extends Form<T> {
    private String rootName = null;
    private Class<T> backedType = null;
    private Map<String,String> data = null;
    private Map<String,List<ValidationError>> errors = null;
    private Option<T> value = null;
    
    private T blankInstance() {
        try {
            return backedType.newInstance();
        } catch(Exception e) {
            throw new RuntimeException("Cannot instantiate " + backedType + ". It must have a default constructor", e);
        }
    }	
    public FormLimitless(Class<T> clazz) {
        super(null, clazz);
        this.backedType = clazz;
    }
    
    public FormLimitless(String rootName, Class<T> clazz, Map<String,String> data, Map<String,List<ValidationError>> errors, Option<T> value) {
    	super(rootName, clazz, data, errors, value);
        this.rootName = rootName;
        this.backedType = clazz;
        this.data = data;
        this.errors = errors;
        this.value = value;
    }
    
    public FormLimitless(String name, Class<T> clazz) {
        super(name, clazz, new HashMap<String,String>(), new HashMap<String,List<ValidationError>>(), new None<T>());
    }
    
    public FormLimitless<T> bindFromRequest(String... allowedFields) {
    	Map<String,String> data = requestData(); 
        return bind(data, allowedFields);
    }
    protected Map<String,String> requestData() {
        
        Map<String,String[]> urlFormEncoded = new HashMap<String,String[]>();
        if(play.mvc.Controller.request().body().asFormUrlEncoded() != null) {
            urlFormEncoded = play.mvc.Controller.request().body().asFormUrlEncoded();
        }
        
        Map<String,String[]> multipartFormData = new HashMap<String,String[]>();
        if(play.mvc.Controller.request().body().asMultipartFormData() != null) {
            multipartFormData = play.mvc.Controller.request().body().asMultipartFormData().asFormUrlEncoded();
        }
        
        Map<String,String> jsonData = new HashMap<String,String>();
        if(play.mvc.Controller.request().body().asJson() != null) {
            jsonData = play.libs.Scala.asJava(
                play.api.data.FormUtils.fromJson("", 
                    play.api.libs.json.Json.parse(
                        play.libs.Json.stringify(play.mvc.Controller.request().body().asJson())
                    )
                )
            );
        }
        
        Map<String,String[]> queryString = play.mvc.Controller.request().queryString();
        
        Map<String,String> data = new HashMap<String,String>();
        
        for(String key: urlFormEncoded.keySet()) {
            String[] value = urlFormEncoded.get(key);
            if(value.length > 0) {
                data.put(key, value[0]);
            }
        }
        
        for(String key: multipartFormData.keySet()) {
            String[] value = multipartFormData.get(key);
            if(value.length > 0) {
                data.put(key, value[0]);
            }
        }
        
        for(String key: jsonData.keySet()) {
            data.put(key, jsonData.get(key));
        }
        
        for(String key: queryString.keySet()) {
            String[] value = queryString.get(key);
            if(value.length > 0) {
                data.put(key, value[0]);
            }
        }
        
        return data;
    }    
    protected int getIndex(String sN)
    {
    	try
    	{
    		if (sN.indexOf("]") > sN.indexOf("["))
    		{
    			String sTry = sN.substring(sN.indexOf("[")+1, sN.indexOf("]"));
    			return Integer.parseInt(sTry);
    		}
    	}
    	catch (Exception e)
    	{
    		// let it return 0
    	}
    	return 0;
    }
    @SuppressWarnings("unchecked")
    public FormLimitless<T> bind(Map<String,String> data, String... allowedFields) {
        
        DataBinder dataBinder = null;
        Map<String, String> objectData = data;
        if(rootName == null) {
            dataBinder = new DataBinder(blankInstance());
        } else {
            dataBinder = new DataBinder(blankInstance(), rootName);
            objectData = new HashMap<String,String>();
            for(String key: data.keySet()) {
                if(key.startsWith(rootName + ".")) {
                    objectData.put(key.substring(rootName.length() + 1), data.get(key));
                }
            }
        }
        if(allowedFields.length > 0) {
            dataBinder.setAllowedFields(allowedFields);
        }
        dataBinder.setValidator(new SpringValidatorAdapter(play.data.validation.Validation.getValidator()));
        dataBinder.setConversionService(play.data.format.Formatters.conversion);
        dataBinder.setAutoGrowNestedPaths(true);

        int iMax = 1000;
        Iterator <String> itk = objectData.keySet().iterator();
        while (itk.hasNext())
        {
        	String sN = itk.next();
        	int iInd = getIndex(sN);
        	if (iInd > iMax)
        		iMax = iInd+1;
        }
        dataBinder.setAutoGrowCollectionLimit(iMax);
        dataBinder.bind(new MutablePropertyValues(objectData));
        dataBinder.validate();
        BindingResult result = dataBinder.getBindingResult();
        
        if(result.hasErrors()) {
            Map<String,List<ValidationError>> errors = new HashMap<String,List<ValidationError>>();
            for(FieldError error: result.getFieldErrors()) {
                String key = error.getObjectName() + "." + error.getField();
                if(key.startsWith("target.") && rootName == null) {
                    key = key.substring(7);
                }
                List<Object> arguments = new ArrayList<Object>();
                for(Object arg: error.getArguments()) {
                    if(!(arg instanceof org.springframework.context.support.DefaultMessageSourceResolvable)) {
                        arguments.add(arg);
                    }                    
                }
                if(!errors.containsKey(key)) {
                   errors.put(key, new ArrayList<ValidationError>()); 
                }
                errors.get(key).add(new ValidationError(key, error.isBindingFailure() ? "error.invalid" : error.getDefaultMessage(), arguments));                    
            }
            return new FormLimitless<T>(rootName, backedType, data, errors, new None<T>());
        } else {
            Map<String,List<ValidationError>> errors = new HashMap<String,List<ValidationError>>();
            List<ValidationError> gE = null;
            if(result.getTarget() != null) {
                try {
                    java.lang.reflect.Method v = result.getTarget().getClass().getMethod("validate");
                    gE = (List<ValidationError>)v.invoke(result.getTarget());
                } catch(NoSuchMethodException e) {
                	Logger.info("FormLimitless used in wrong context: "+e.toString());
                } catch(Throwable e) {
                    throw new RuntimeException(e);
                }
            }
            if(gE != null) {
                errors.put("", new ArrayList<ValidationError>());
                Iterator<ValidationError> it = gE.iterator();
                while (it.hasNext())
                {
                	ValidationError ve = it.next();
                	String key = ve.key();
                    if(key!=null && !key.equals("")) 
                    {
                    	if (!errors.containsKey(key)) 
                    		errors.put(key, new ArrayList<ValidationError>()); 
                        errors.get(key).add(new ValidationError(key, ve.message()));                    
                    }
                    else
                    	errors.get("").add(ve);
                }
            	return new FormLimitless<T>(rootName, backedType, data, errors, new None<T>());
            }
            HashMap<String,String> hm1 = new HashMap<String,String>(data);
            return new FormLimitless<T>(rootName, backedType, hm1, errors, Some((T)result.getTarget()));
        }
    }        
}
