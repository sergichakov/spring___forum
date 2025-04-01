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
//    @Value("${auth-server.get-jwk-link}")
    private final String oauth2ServerJwk;
    @Autowired
    private RestTemplate restTemplate;
    private Thread thread;// = new Thread(future);
    private FutureTask<String> future  /**/=
            new FutureTask<>(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    System.out.println("Oper o="+oauth2ServerJwk+">>>>");
                    String jwkOpenKey = restTemplate.getForObject(
                            oauth2ServerJwk, String.class);
                    System.out.println("received jwkOpenKey key="+jwkOpenKey);
                    return jwkOpenKey;
                }
            });
//    public String getOAuth2ServerJwk(){
//        return oauth2ServerJwk;
//    }
    public void fetch(){
        future  /**/=
                new FutureTask<>(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        System.out.println("Oper o="+oauth2ServerJwk+">>>>");
                        String jwkOpenKey = restTemplate.getForObject(
                                oauth2ServerJwk, String.class);
                        System.out.println("received jwkOpenKey key="+jwkOpenKey);
                        return jwkOpenKey;
                    }
                });
        thread = new Thread(future);
        System.out.println("thread"+thread);
        thread.start();
    }

    public JwtKeyComponent (RestTemplate restTemplate, @Value("${auth-server.get-jwk-link}")String oauth2ServerJwk){
        this.restTemplate=restTemplate;
        this.oauth2ServerJwk=oauth2ServerJwk;
//        future =
//                new FutureTask<>(new Callable<String>() {
//                    @Override
//                    public String call() throws Exception {
////                        System.out.println("Oper o="+oauth2ServerJwk+">>>>");
////                        if (null==oauth2ServerJwk){
////                            System.out.println("o= is null");
////                        }else {
////                            System.out.println(" getBytes() length=" + oauth2ServerJwk.getBytes().length);
////                        }
////                        String serverLinkJwk=JwtKeyComponent.this.getOAuth2ServerJwk();
////                        System.out.println("JwtKeyComponent o=-"+serverLinkJwk);
//                        String jwkOpenKey = restTemplate.getForObject(//"http://localhost:9000/jwks"
//                                serverLinkJwk /*oauth2ServerJwk*/, String.class);
//                        System.out.println("received jwkOpenKey key="+jwkOpenKey);
//                        return jwkOpenKey;
//                    }
//                });
        thread = new Thread(future);
        thread.start();
        //System.out.println("Oper oauth2ServerJwk="+oauth2ServerJwk+">>");
    }
    public String get() throws InterruptedException, ExecutionException {
        System.out.println("Oper oauth2ServerJwk="+oauth2ServerJwk+">>");
        //String getKey=future.get();
        //String keyRS256=Base64.getDecoder().decode(getKey);
        return future.get();

    }
}
