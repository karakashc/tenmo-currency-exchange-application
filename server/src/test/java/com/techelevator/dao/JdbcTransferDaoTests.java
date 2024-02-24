package com.techelevator.dao;

import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

public class JdbcTransferDaoTests extends BaseDaoTests{
    private JdbcTransferDao transferDaoSut;
    private UserDao userDaoSut;
    private Principal mockPrincipal;
    @Before
    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        userDaoSut = new JdbcUserDao(jdbcTemplate);
        transferDaoSut = new JdbcTransferDao(jdbcTemplate, userDaoSut);

        //mock is used to create objects to test methods that have dependencies on other objects.
        // so in other words, if you have a method that uses an object that is not created in the method, you can mock that object.
        //https://www.baeldung.com/mockito-mock-methods
        //https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html#method-summary

        mockPrincipal = Mockito.mock(Principal.class);
    }

    @Test
    public void getUsersAvailable() {
        //Mockito is used to mock the Principal object and return a username.  This is used to test the getUsersAvailable method.
        // basically we are mocking the Principal object and returning a username. When x method is called on the mocked Principal object, return bob.
        Mockito.when(mockPrincipal.getName()).thenReturn("bob");

        List<TransferDTO> user = new ArrayList<>();
        user = transferDaoSut.getUsersAvailable(mockPrincipal);

        List<TransferDTO> expectedUsers = new ArrayList<>();
        expectedUsers.add(new TransferDTO("user"));

        Assert.assertEquals(expectedUsers.indexOf("username"), user.indexOf("username"));
    }

    @Test
    public void getUserById() {

        String user = transferDaoSut.getUserById(1001);
        Assert.assertEquals("bob", user);
    }

    @Test
    public void getTransferById() {
        Transfer transfer = transferDaoSut.getTransferById(3001, 1001);
        Assert.assertEquals(3001, transfer.getTransferId());

    }

    @Test
    public void getTransferByUserId() {
        List<Transfer> transfer = transferDaoSut.getTransferByUserId(1001);
        List<Transfer> expectedTransfer = new ArrayList<>(2);
        expectedTransfer.add(new Transfer(3001, new BigDecimal("1000.00"), "me", "you", true));
        expectedTransfer.add(new Transfer(3002, new BigDecimal("1.00"), "me", "you", true));

        Assert.assertEquals(expectedTransfer.size(), transfer.size());

    }

    @Test
    public void createTransfer() {
        Transfer transfer = transferDaoSut.createTransfer(new BigDecimal("101.00"), "user", "bob");
        Assert.assertEquals(3003, transfer.getTransferId());
    }
}