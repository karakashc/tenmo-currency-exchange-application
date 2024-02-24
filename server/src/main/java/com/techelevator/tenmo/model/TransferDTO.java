package com.techelevator.tenmo.model;

import javax.validation.constraints.NotEmpty;

public class TransferDTO {
    @NotEmpty
    String username;

    public TransferDTO(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
