package com.wolfman.marathon;

import com.wolfman.marathon.knowledgeExtend.EnableHelloWorldRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableHelloWorldRegistrar
@EnableFeignClients(basePackages = {"com.wolfman.marathon"})
@SpringBootApplication
public class CompetitionManagementApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(CompetitionManagementApplication.class, args);
		System.out.println(configurableApplicationContext.getBean("hello"));
	}

	@LoadBalanced
	@Bean
	RestTemplate loadBalanced() {
		return new RestTemplate();
	}

}
