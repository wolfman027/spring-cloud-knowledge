package com.wolfman.marathon.qualifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
public class TestController {

    @Qualifier
    @Autowired
    List<TestClass> testClassList = Collections.emptyList();


    @GetMapping("testClassList")
    public Object test(){
        return testClassList;
    }



}
