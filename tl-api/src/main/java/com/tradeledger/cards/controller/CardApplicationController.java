package com.tradeledger.cards.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradeledger.cards.common.Applicant;
import com.tradeledger.cards.model.EligibilityResult;
import com.tradeledger.cards.service.EligibilityService;
import com.tradeledger.cards.utils.Reject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static com.tradeledger.cards.config.RequestMapping.APPLY_ENDPOINT;
import static com.tradeledger.cards.config.RequestMapping.BASE_APPLY_ENDPOINT;

/**
 * Created by daqwang on 5/8/21.
 */
@RestController
@RequestMapping(BASE_APPLY_ENDPOINT)
public class CardApplicationController {

    @Autowired
    public EligibilityService eligibilityService;


    @PostMapping(APPLY_ENDPOINT)
    public ResponseEntity<String> applyCard(@RequestBody Applicant applicant) throws JsonProcessingException {
        if (applicant == null) {
            throw new IllegalArgumentException("invalid applicant");
        }
        validate(applicant);

        Mono<EligibilityResult> eligibilityResultMono = eligibilityService.eligibilityCheck(applicant);
        EligibilityResult eligibilityResult = eligibilityResultMono.block();
        ObjectMapper objectMapper = new ObjectMapper();
        String result = objectMapper.writeValueAsString(eligibilityResult);
        if(result.contains("errorMessage")){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
        }
        return ResponseEntity.ok(objectMapper.writeValueAsString(eligibilityResult));
    }

    private void validate(Applicant applicant) {
        Reject.ifNullOrEmpty(applicant.getAddress());
        Reject.ifNullOrEmpty(applicant.getEmail());
        Reject.ifNullOrEmpty(applicant.getName());
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<String> handleException() {
        return ResponseEntity.badRequest().body("please provide valid applicant");
    }
}
