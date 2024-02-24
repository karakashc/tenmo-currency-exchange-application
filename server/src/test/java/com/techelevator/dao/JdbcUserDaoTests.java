package com.techelevator.dao;

import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcUserDaoTests extends BaseDaoTests{

    private JdbcUserDao sut;

    @Before
    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcUserDao(jdbcTemplate);
    }

    @Test
    public void createNewUser() {
        boolean userCreated = sut.create("TEST_USER", "test_password");
        Assert.assertTrue(userCreated);
        User user = sut.findByUsername("TEST_USER");
        Assert.assertEquals("TEST_USER", user.getUsername());
    }

    @Test
    public void findIdByUsername() {
        sut.create("TEST_USER", "test_password");
        int id = sut.findIdByUsername("TEST_USER");
        Assert.assertTrue(id > 0);
    }

    @Test
    public void findAll_returns_all_users() {
        int initialSize = sut.findAll().size();
        sut.create("TEST_USER", "test_password");
        int finalSize = sut.findAll().size();
        Assert.assertEquals(initialSize + 1, finalSize);
    }
}
