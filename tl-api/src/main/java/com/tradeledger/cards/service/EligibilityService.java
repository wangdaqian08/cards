package com.tradeledger.cards.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.tradeledger.cards.common.Applicant;
import com.tradeledger.cards.model.EligibilityResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import static com.tradeledger.cards.config.RequestMapping.ELIGIBILITY_CHECK_ENDPOINT;

/**
 * Created by daqwang on 5/8/21.
 */
@Service
public class EligibilityService {

    public static final Logger LOG = LoggerFactory.getLogger(EligibilityService.class);

    private final HttpRequestService httpRequestService;

    @Autowired
    public EligibilityService(final HttpRequestService httpRequestService) {
        this.httpRequestService = httpRequestService;
    }


    @HystrixCommand(commandKey = "eligibilityCheck",
            fallbackMethod = "defaultServiceNotAvailable")
    public Mono<EligibilityResult> eligibilityCheck(Applicant applicant) {

        return httpRequestService.buildPostRequest(ELIGIBILITY_CHECK_ENDPOINT, BodyInserters.fromValue(applicant), EligibilityResult.class);
    }

    public Mono<EligibilityResult> defaultServiceNotAvailable(Applicant applicant) {
        LOG.warn("third party api service is not available");
        String message = "service is not available, please try again later";
        EligibilityResult eligibilityResult = new EligibilityResult(message);

        return Mono.just(eligibilityResult);
    }




}
