package com.assembliestore.api.module.product.category;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
public class CategoryToggleVisibleTest {
    private final String TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiJjOTFmMGIxZS0zNmY1LTRmOWItYTE0YS0yZTVmZGUzZjNlZjUiLCJ1c2VyTmFtZSI6ImFsaWNlMDEiLCJzdWIiOiJhbGljZTAxQGV4YW1wbGUuY29tIiwiaWF0IjoxNzUzMDcwMjg5LCJleHAiOjE3NTMxNTY2ODl9.MS2J1ltrP0d6-VAdalDXxTi8TzArBPuBJR5LNytGou-b2weSb3vWYCzstRG65QLjhb8clPWKYCJSbUTpYXjeyw";
    private final String BASE_URL = "http://localhost:8080/categorie/toggle-visible/1";

    @Test
    public void testToggleVisibleCategory() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(BASE_URL, HttpMethod.PATCH, entity, String.class);
        assert(response.getStatusCode() == HttpStatus.OK);
    }
}
