package com.pulsepay.account;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;
import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public class AccountRepository {

    private final JdbcTemplate jdbcTemplate;
    public AccountRepository (JdbcTemplate jdbcTemplate){
        this.jdbcTemplate= jdbcTemplate;
    }

    public Account create(UUID ownerId,String currency ){
        String sql="""
                Insert into accounts(owner_id, currency, status) values(?,?,'ACTIVE')
                returning id,owner_id, currency, available_balance, status, created_at
                """;
        return jdbcTemplate.queryForObject(sql, (rs,rowNum)-> new Account(
            rs.getObject("id",UUID.class),
            rs.getObject("owner_id",UUID.class),
            rs.getString("currency").trim(),
            rs.getLong("available_balance"),
            rs.getString("status"),
            rs.getObject("created_at", OffsetDateTime.class).toInstant()),
            ownerId, 
            currency
        );
    }  

    public Optional<Account> findById(UUID accountId){
        String sql="""
                SELECT  id,owner_id, currency, available_balance, status, created_at 
                FROM accounts where id=?
                """;
        
        return jdbcTemplate.query(sql, (rs)->{
            if(!rs.next()){
                return Optional.empty();
            }

            return Optional.of(new Account(
                rs.getObject("id",UUID.class),
                rs.getObject("owner_id",UUID.class),
                rs.getString("currency").trim(),
                rs.getLong("available_balance"),
                rs.getString("status"),
                rs.getObject("created_at", OffsetDateTime.class).toInstant()
                 )
            );
            
        }, accountId);  
        
    }

}