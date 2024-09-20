package com.wolfman.marathon;

import com.wolfman.marathon.knowledgeExtend.EnableHelloWorldRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

@EnableCaching
@EnableHelloWorldRegistrar
@EnableFeignClients(basePackages = {"com.wolfman.marathon"})
@SpringBootApplication
public class CompetitionManagementApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(CompetitionManagementApplication.class, args);
		System.out.println(configurableApplicationContext.getBean("hello"));
	}

}
