package com.hnf.honeycomb.controller;

import com.hnf.honeycomb.util.JsonResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author 佚名
 */
@CrossOrigin(methods = {RequestMethod.GET, RequestMethod.POST}, origins = "*")
public abstract class AbstractController {

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public JsonResult exceptionHandle(Exception e) {
        e.printStackTrace();
        return new JsonResult(e);
    }
}
