package com.techelevator.dao;


import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.AccountDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class JdbcAccountDaoTests extends BaseDaoTests {

    private JdbcAccountDao sut1;
    private JdbcUserDao sut2;
    private UserDao userDao;
    @Before
    public void setup(){
        JdbcTemplate jdbcTemplate =new JdbcTemplate(dataSource);
        sut1 = new JdbcAccountDao(jdbcTemplate);
        sut2 = new JdbcUserDao(jdbcTemplate);
    }
    @Test
    public void getBalance(){

        int userId= sut2.findIdByUsername("bob");

        AccountDTO accountDTO = sut1.getBalance(userId);
        BigDecimal bigDecimal = new BigDecimal("1000.00");

        Assert.assertNotNull(accountDTO);
        Assert.assertEquals(bigDecimal,accountDTO.getBalance());


    }


}