package com.turno.los.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class AgentDecisionRequest {
    @NotBlank
    @Pattern(regexp = "APPROVE|REJECT", message = "decision must be APPROVE or REJECT")
    public String decision;
}
