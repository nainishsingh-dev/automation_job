package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.crypto.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

@SpringBootApplication
@EnableScheduling
public class WorkflowApplication {

	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        SpringApplication.run(WorkflowApplication.class, args);
    }

}
