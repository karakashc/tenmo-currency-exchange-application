package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDTO;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
@Component
public class JdbcTransferDao implements TransferDao{

    private final JdbcTemplate jdbcTemplate;
    final private UserDao userDao;
    public JdbcTransferDao(JdbcTemplate jdbcTemplate, UserDao userDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDao = userDao;
    }

    @Override
    public List<TransferDTO> getUsersAvailable(Principal principal) {
        List<TransferDTO> users = new ArrayList<>();
        String sql = "SELECT username FROM tenmo_user;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);

        while (results.next()) {
            if (results.getString("username").equals(principal.getName())) {
                continue;
            }
            TransferDTO transferDTO = new TransferDTO(results.getString("username"));
            users.add(transferDTO);
        }
        return users;
        // TODO: Make sure the user is available, and activated.
    }

    @Override
    public String getUserById(int userId) {
        String sql = "SELECT username FROM tenmo_user " +
                "WHERE user_id =?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
        String username = "";
        if (results.next()) {
            username = results.getString("username");
        }
        return username;
    }

    @Override
    public Transfer getTransferById(int transferId, int userId) {
        Transfer transfer = new Transfer();
        String sql = "SELECT * FROM transfer " +
                "JOIN tenmo_user ON transfer.from_user_id = tenmo_user.user_id OR transfer.to_user_id = tenmo_user.user_id " +
                "WHERE transfer_id = ? " +
                "AND tenmo_user.user_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferId, userId);
        if (results.next()) {
            transfer = mapRowToTransfer(results);
        }
        return transfer;
    }

    @Override
    public List<Transfer> getTransferByUserId(int userId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT * FROM transfer " +
                "WHERE from_user_id = ? " +
                "OR to_user_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId, userId);
        while (results.next()) {
            Transfer transfer = mapRowToTransfer(results);
            transfers.add(transfer);
        }
        return transfers;
    }

    @Override
    public Transfer createTransfer(BigDecimal tAmount, String userToSendTo, String userToSendFrom) {
        Transfer transfer = new Transfer();

        // Below sql is used twice as result1 and result2,
        // Result1 is assigned to the Sender, and Result2 is assigned to the Receiver:
        String sql = "SELECT account.user_id, account.balance " +
                "FROM account " +
                "JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                "WHERE tenmo_user.username = ?;";

        SqlRowSet result1 = jdbcTemplate.queryForRowSet(sql, userToSendFrom);
        SqlRowSet result2 = jdbcTemplate.queryForRowSet(sql, userToSendTo);

        // Below if statement is getting the balance from the database and assign those values to Sender and Receiver balances:
        if (result1.next() && result2.next()) {
            BigDecimal fromBalance = result1.getBigDecimal("balance");
            BigDecimal toBalance = result2.getBigDecimal("balance");

            int userIdFrom = userDao.findIdByUsername(userToSendFrom);
            int userIdTo = userDao.findIdByUsername(userToSendTo);

            if (userIdTo == userIdFrom) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot send money to yourself");
            } else if (tAmount == null || fromBalance.compareTo(tAmount) < 0) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient funds");
            } else if (tAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot send negative or zero money");
            }
            // Below if statement will prevent the user to send money to same user as logged in:

            transfer.setApproved(true);
            transfer.setTransferAmount(tAmount);
            transfer.setTo(userToSendTo);
            transfer.setFrom(userToSendFrom);

            // Below sql will create a transfer in the database:
            String sqlInsert = "INSERT INTO transfer (from_user_id,to_user_id,transfer_amount,status) VALUES(?,?,?,?) RETURNING transfer_id";
            try {
                int transferId = jdbcTemplate.queryForObject(sqlInsert, new Object[]{userIdFrom, userIdTo, tAmount, transfer.isApproved()}, Integer.class);
                transfer.setTransferId(transferId);
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


            // Below sql will update the sender balance in the database:
            String sqlUpdateSenderBalance = "UPDATE account " +
                    "SET balance = ? " +
                    "WHERE user_id = ?;";
            try {
                jdbcTemplate.update(sqlUpdateSenderBalance, fromBalance.subtract(tAmount), userIdFrom);
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

            // Below sql will update the receiver balance in the database:
            String sqlUpdateReceiverBalance = "UPDATE account " +
                    "SET balance = ? " +
                    "WHERE user_id = ?;";
            try {
                jdbcTemplate.update(sqlUpdateReceiverBalance, toBalance.add(tAmount), userIdTo);
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
        }

        return transfer;
    }

    public void subtractFromBalance(BigDecimal tAmount, BigDecimal toBalance, int userIdFrom) {
        // Below sql will update the receiver balance in the database:
        String sqlUpdateReceiverBalance = "UPDATE account " +
                "SET balance = ? " +
                "WHERE user_id = ?;";
        try {
            jdbcTemplate.update(sqlUpdateReceiverBalance, toBalance.add(tAmount), userIdFrom);
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
    }

    public void addToBalance(BigDecimal tAmount, BigDecimal fromBalance, int userIdTo) {
        // Below sql will update the sender balance in the database:
        String sqlUpdateSenderBalance = "UPDATE account " +
                "SET balance = ? " +
                "WHERE user_id = ?;";
        try {
            jdbcTemplate.update(sqlUpdateSenderBalance, fromBalance.subtract(tAmount), userIdTo);
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
    }
    // TODO: Add challenge use cases here:

    private Transfer mapRowToTransfer(SqlRowSet results) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(results.getInt("transfer_id"));
        transfer.setTransferAmount(results.getBigDecimal("transfer_amount"));
        transfer.setFrom(getUserById(results.getInt("from_user_id")));
        transfer.setTo(getUserById(results.getInt("to_user_id")));
        transfer.setApproved(results.getBoolean("status"));
        return transfer;
    }

}
