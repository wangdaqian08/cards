package com.tradeledger.cards.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.List;

/**
 * Created by daqwang on 5/8/21.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EligibilityResult {

    private List<String> eligibleCards;

    public EligibilityResult(){}
    public EligibilityResult(String errorMessage){
        this.errorMessage = errorMessage;
    }

    public String errorMessage;

    public List<String> getEligibleCards() {
        return eligibleCards;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof EligibilityResult)) return false;

        EligibilityResult that = (EligibilityResult) o;

        return new EqualsBuilder().append(eligibleCards, that.eligibleCards).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(eligibleCards).toHashCode();
    }
}
