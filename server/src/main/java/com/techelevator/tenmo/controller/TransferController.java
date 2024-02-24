package com.techelevator.tenmo.controller;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDTO;
import com.techelevator.tenmo.model.TransferRequestDTO;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
public class TransferController {
 final private JdbcTransferDao transferDao;
 final private UserDao userDao;

    public TransferController(JdbcTransferDao transferDao, UserDao userDao) {
        this.transferDao = transferDao;
        this.userDao = userDao;
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(path = "/userlist", method = RequestMethod.GET)
    public List<TransferDTO> getUsers(Principal principal) {
        return transferDao.getUsersAvailable(principal);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(path = "/userlist/transfer", method = RequestMethod.GET)
    public List<Transfer> getTransfers(Principal principal) {
        try {
            int userId = userDao.findIdByUsername(principal.getName());
            return transferDao.getTransferByUserId(userId);
        } catch (IllegalArgumentException e) {
            String detailedMessage = "Illegal argument provided for [parameter name]. Expected [condition], received: " + e.getMessage();
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, detailedMessage, e);
        } catch (NullPointerException np) {
            String detailedMessage = "Null pointer encountered in [method or variable name]: " + np.getMessage();
            System.out.println(detailedMessage);
        } catch (DataIntegrityViolationException dive) {
            String detailedMessage = "Data integrity violation in [operation]: " + dive.getMessage();
            System.out.println(detailedMessage);
        } catch (DataAccessException dae) {
            String detailedMessage = "Data access exception during [specific operation]: " + dae.getMessage();
            System.out.println(detailedMessage);
        }
        return new ArrayList<>();
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(path = "/userlist/transfer/{transferId}", method = RequestMethod.GET)
    public Transfer getTransferById(@PathVariable int transferId, Principal principal) {
        try {
            int userId = userDao.findIdByUsername(principal.getName());
            return transferDao.getTransferById(transferId, userId);
        } catch (IllegalArgumentException e) {
            String detailedMessage = "Illegal argument provided for [parameter name]. Expected [condition], received: " + e.getMessage();
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, detailedMessage, e);
        } catch (NullPointerException np) {
            String detailedMessage = "Null pointer encountered in [method or variable name]: " + np.getMessage();
            System.out.println(detailedMessage);
        } catch (DataIntegrityViolationException dive) {
            String detailedMessage = "Data integrity violation in [operation]: " + dive.getMessage();
            System.out.println(detailedMessage);
        } catch (DataAccessException dae) {
            String detailedMessage = "Data access exception during [specific operation]: " + dae.getMessage();
            System.out.println(detailedMessage);
        }
        return new Transfer();
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(path = "/userlist/transfer", method = RequestMethod.POST)
    public ResponseEntity<Transfer> createTransfer(@Valid @RequestBody TransferRequestDTO transferRequestDTO, Principal principal) throws UnrecognizedPropertyException {
        try {
            Transfer createdTransfer = transferDao.createTransfer(transferRequestDTO.getAmount(), transferRequestDTO.getRecipientName(), principal.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body((createdTransfer));
        } catch (IllegalArgumentException e) {
            String detailedMessage = "Illegal argument provided for [parameter name]. Expected [condition], received: " + e.getMessage();
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, detailedMessage, e);
        } catch (NullPointerException np) {
            String detailedMessage = "Null pointer encountered in [method or variable name]: " + np.getMessage();
            System.out.println(detailedMessage);
        } catch (DataIntegrityViolationException dive) {
            String detailedMessage = "Data integrity violation in [operation]: " + dive.getMessage();
            System.out.println(detailedMessage);
        } catch (DataAccessException dae) {
            String detailedMessage = "Data access exception during [specific operation]: " + dae.getMessage();
            System.out.println(detailedMessage);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
