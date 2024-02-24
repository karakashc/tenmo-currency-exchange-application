package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.AccountDTO;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Component
public class JdbcAccountDao implements AccountDao {
    private final JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AccountDTO getBalance(int userId) {

        String sql = "SELECT tenmo_user.username, account.balance " +
                "FROM tenmo_user " +
                "JOIN account on tenmo_user.user_id = account.user_id " +
                "WHERE tenmo_user.user_id = ?;";

        BigDecimal balance;
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
            if (results.next()) {
                balance = results.getBigDecimal("balance");
                String userName = results.getString("username");
                return new AccountDTO(userName, balance);
            }

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
        return null;
    }

}
