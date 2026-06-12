package com.guilhermesemog.unimove;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class UnimoveApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnimoveApplication.class, args);
    }

}
