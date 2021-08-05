package com.tradeledger.cards.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradeledger.cards.common.Applicant;
import com.tradeledger.cards.config.WebClientConfig;
import com.tradeledger.cards.model.EligibilityResult;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

import static com.tradeledger.cards.config.RequestMapping.ELIGIBILITY_CHECK_ENDPOINT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by daqwang on 5/8/21.
 */

public class EligibilityServiceTest {


    private HttpRequestService httpRequestService;

    public static MockWebServer mockWebServer;

    public WebClientConfig mockWebClientConfig;

    @BeforeEach
    public void setUp() throws IOException {


        mockWebClientConfig = mock(WebClientConfig.class);
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8090")
                .build();

        when(mockWebClientConfig.thirdPartyWebClient()).thenReturn(webClient);
        httpRequestService = new HttpRequestService(mockWebClientConfig);
        ReflectionTestUtils.setField(httpRequestService,"requestTimeoutInSecond",5);
        ReflectionTestUtils.setField(httpRequestService,"retryTimes",3);
        ReflectionTestUtils.setField(httpRequestService,"backOffInSecond",5);
        mockWebServer = new MockWebServer();

        mockWebServer.start(8090);
    }

    private String getApplicantJsonString() throws JsonProcessingException {
        Applicant applicant = new Applicant("test", "email@test.com", "address");
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(applicant);
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testEligibilityCheckRetrySuccessOnForthTime() throws Exception {
        Applicant applicant = new Applicant("test", "email@test.com", "address");

        EligibilityResult provideEligibilityResult = new EligibilityResult();
        ObjectMapper objectMapper = new ObjectMapper();
        String eligibilityResultJsonStr = objectMapper.writeValueAsString(provideEligibilityResult);
        EligibilityService eligibilityService = new EligibilityService(httpRequestService);
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
                .setHeader("Content-Type", MediaType.APPLICATION_JSON).setBody(eligibilityResultJsonStr));


        StepVerifier.create(eligibilityService.eligibilityCheck(applicant))
                .expectNextMatches(response -> {

                    ObjectMapper objectMapper1 = new ObjectMapper();
                    try {
                        return objectMapper1.writeValueAsString(response).equals(eligibilityResultJsonStr);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return false;
                })
                .verifyComplete();
        verifyNumberOfGetRequests(4);

    }

    private void verifyNumberOfGetRequests(int times) throws InterruptedException {
        for (int i = 0; i < times; i++) {
            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            Assertions.assertEquals("POST", recordedRequest.getMethod());
            Assertions.assertEquals(ELIGIBILITY_CHECK_ENDPOINT, recordedRequest.getPath());
        }
    }

}
