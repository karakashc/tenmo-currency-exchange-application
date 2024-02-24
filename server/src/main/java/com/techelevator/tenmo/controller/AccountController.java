package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.AccountDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;


@RestController
public class AccountController {

    final private UserDao userDao;
    final private AccountDao accountDao;

    public AccountController(UserDao userDao,AccountDao accountDao) {
        this.userDao = userDao;
        this.accountDao= accountDao;
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/balance", method = RequestMethod.GET)
    public AccountDTO getBalance(Principal principal) {
        String userName = principal.getName();
        int userId = userDao.findIdByUsername(userName);
        return accountDao.getBalance(userId);
    }

}
