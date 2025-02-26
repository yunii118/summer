package org.choongang.business;

import org.choongang.commons.Utils;
import org.choongang.member.service.BusinessVerifyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class BusinessNoApiTest {

    @Autowired
    private BusinessVerifyService verifyService ;

    @Test
    @DisplayName("사업자등록 상태 확인 API 테스트")
    void statusApiTest() throws URISyntaxException {

        String url = "https://api.odcloud.kr/api/nts-businessman/v1/status?serviceKey=PZRd8sqm44M6dwhJU0o73ZkBN14pihSQrggFbquqY1%2BlR4efdEMsl7rj%2FdsckR2ekzub2nSalS3CEI60IirgEA%3D%3D";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("b_no", "2208657343");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(new URI(url), request, String.class);
        String body = response.getBody();
        System.out.println(body);
    }

    @Test
    @DisplayName("사업자 등록증 상태 체크 함수 테스트")
    void checkBusinessPermit() {
        boolean result = verifyService.checkBusinessPermit("2208657343");

        assertTrue(result);

    }
}
