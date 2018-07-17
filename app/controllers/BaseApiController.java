package controllers;

import java.io.StringWriter;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.wordnik.swagger.core.util.JsonUtil;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

public class BaseApiController extends Controller {

    protected static ObjectMapper mapper = JsonUtil.mapper();

    /**
     * Returns a Json response using the Play Framework helpers
     * @param obj the object to be converted to Json
     * @return Play Framework version
     */
    public static Result JsonResponse(Object obj) {
        return JsonResponse(obj, 200);
    }

    /**
     * Returns a Json object with a status code
     * @param obj the object to be converted to Json
     * @param code the status code to be returned
     * @return The Play Framework result
     */
    public static Result JsonResponse(Object obj, int code) {
        StringWriter w = new StringWriter();
        try {
            mapper.writeValue(w, obj);
        } catch (Exception e) {
            // TODO: handle proper return code
            e.printStackTrace();
            Logger.error("JSON response error: "+e.toString());
        }

        response().setContentType("application/json");
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        response().setHeader("Access-Control-Allow-Headers", "Content-Type, api_key, Authorization");

        return status(code, w.toString());
    }
}
