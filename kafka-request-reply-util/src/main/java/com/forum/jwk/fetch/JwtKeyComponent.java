package com.forum.jwk.fetch;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Component
@Getter
public class JwtKeyComponent  {

    private final String oauth2ServerJwk;
    @Autowired
    private RestTemplate restTemplate;
    private Thread thread;
    private FutureTask<String> future = new FutureTask<>(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    System.out.println("Oper o="+oauth2ServerJwk+">>>>");
                    String jwkOpenKey = restTemplate.getForObject(
                            oauth2ServerJwk, String.class);
                    System.out.println("received jwkOpenKey key="+jwkOpenKey);
                    return jwkOpenKey;
                }
            });

    public void fetch(){
        future  = new FutureTask<>(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        System.out.println("Oper o="+oauth2ServerJwk+">>>>");
                        String jwkOpenKey = restTemplate.getForObject(
                                oauth2ServerJwk, String.class);
                        return jwkOpenKey;
                    }
                });
        thread = new Thread(future);
        thread.start();
    }

    public JwtKeyComponent (RestTemplate restTemplate, @Value("${auth-server.get-jwk-link}")String oauth2ServerJwk){
        this.restTemplate=restTemplate;
        this.oauth2ServerJwk=oauth2ServerJwk;

        thread = new Thread(future);
        thread.start();
    }
    public String get() throws InterruptedException, ExecutionException {
        return future.get();

    }
}
