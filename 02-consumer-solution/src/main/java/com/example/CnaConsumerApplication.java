package com.example;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@EnableFeignClients
@EnableCircuitBreaker
@EnableDiscoveryClient
@SpringBootApplication
public class CnaConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CnaConsumerApplication.class, args);
	}
}

@Data
class Contact{
	Long id;
	String name;
	String address;
}

@FeignClient(name = "cna-provider", fallback = RemoteServiceFallback.class)
interface RemoteService{
	@RequestMapping("/contacts")
	Resources<Contact> getAllContacts();
}

@Component
class RemoteServiceFallback implements RemoteService{
	@Override
	public Resources<Contact> getAllContacts() {
		Contact defaultContact = new Contact();
		defaultContact.setAddress("default");
		defaultContact.setName("default");
		defaultContact.setId(0L);
		return new Resources<>(Arrays.asList(defaultContact));
	}
}

@RefreshScope
@RestController
class SimpleController{

	@Autowired RemoteService remoteService;
	@Value("${greeting.message}")
	private String message;

	@GetMapping("/greeting")
	public String greeting(){
		return message;
	}

	@GetMapping("/names")
	public List<String> getAllNames(){
		return remoteService.getAllContacts()
				.getContent()
				.stream()
				.map(c -> c.getName())
				.collect(Collectors.toList());
	}

}
