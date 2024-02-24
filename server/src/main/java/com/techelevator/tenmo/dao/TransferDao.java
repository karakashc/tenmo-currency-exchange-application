package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDTO;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

public interface TransferDao {

    // get user information
    List<TransferDTO> getUsersAvailable(Principal principal);

    String getUserById(int userId);

    // get transfers
    Transfer getTransferById(int transferId, int userId);

    List<Transfer> getTransferByUserId(int userId);

    // create transfer
    Transfer createTransfer(BigDecimal bigDecimal, String userToSendTo, String userToSendFrom);

    // TODO: Add challenge use cases here:

}
