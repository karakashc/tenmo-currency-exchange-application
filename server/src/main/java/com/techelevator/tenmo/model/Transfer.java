package com.techelevator.tenmo.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

public class Transfer {
    @NotBlank
    @NotNull
    private int transferId;
    @Positive
    @NotBlank
    @NotNull
    private BigDecimal transferAmount;
    @NotBlank
    @NotNull
    private String from;
    @NotBlank
    @NotNull
    private String to;

    private boolean isApproved;

    public Transfer() {
    }

    public Transfer(int transferId, BigDecimal transferAmount, String from, String to, boolean isApproved) {
        this.transferId = transferId;
        this.transferAmount = transferAmount;
        this.from = from;
        this.to = to;
        this.isApproved = isApproved;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public BigDecimal getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(BigDecimal transferAmount) {
        this.transferAmount = transferAmount;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

}
