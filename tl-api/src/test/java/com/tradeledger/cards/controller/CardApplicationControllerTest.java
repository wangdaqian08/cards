package com.tradeledger.cards.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradeledger.cards.common.Applicant;
import com.tradeledger.cards.model.EligibilityResult;
import com.tradeledger.cards.service.EligibilityService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Created by daqwang on 5/8/21.
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = CardApplicationController.class)
public class CardApplicationControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objMapper;

    @MockBean
    private EligibilityService eligibilityService;

    @Before
    public void setup() throws JsonProcessingException {
        EligibilityResult eligibilityResult = new EligibilityResult();
        String checkResponse = objMapper.writeValueAsString(eligibilityResult);
        Mono<EligibilityResult> eligibilityResultMono = Mono.just(eligibilityResult);
        when(eligibilityService.eligibilityCheck(any(Applicant.class))).thenReturn(eligibilityResultMono);

    }


    @Test
    public void testApplyCardWithValidApplicant() throws Exception {
        String applicantString = buildTestApplicantString("test", "email@test.com", "address");
        validateApplyRequest(applicantString, status().isOk());
    }

    @Test
    public void testApplyCardWithInvalidApplicant() throws Exception {
        //test empty applicant
        String applicantString = buildTestApplicantString("", "", "");
        validateApplyRequest(applicantString, status().isBadRequest());

        //test partially empty applicant
        applicantString = buildTestApplicantString("test", "email@test.com", "");
        validateApplyRequest(applicantString, status().isBadRequest());

        applicantString = buildTestApplicantString("test", "", "");
        validateApplyRequest(applicantString, status().isBadRequest());

        applicantString = buildTestApplicantString("", "email@test.com", "");
        validateApplyRequest(applicantString, status().isBadRequest());

        applicantString = buildTestApplicantString("", "", "address");
        validateApplyRequest(applicantString, status().isBadRequest());
    }



    private void validateApplyRequest(String applicantString, ResultMatcher badRequest) throws Exception {
        mvc.perform(post("/apply/card")
                .contentType("application/json")
                .content(applicantString)
        ).andExpect(badRequest);
    }

    private String buildTestApplicantString(String name, String email, String address) throws JsonProcessingException {
        Applicant applicant = new Applicant(name, email, address);
        return objMapper.writeValueAsString(applicant);
    }
}
