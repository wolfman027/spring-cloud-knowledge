package com.wolfman.marathon.qualifier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfiguration {


    @Bean("testClass1")
    @Qualifier
    TestClass testClass(){
        return new TestClass("TestClass1");
    }

    @Bean("testClass2")
    @Qualifier
    TestClass testClass2(){
        return new TestClass("TestClass2");
    }

}
