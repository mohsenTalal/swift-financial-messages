package com.mohsen.swift.dao.integration.repository;

import com.mohsen.swift.dao.integration.entity.SwiftStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SwiftStatusRepository {

  @Autowired
  @Qualifier("intJdbcTemplate")
  private JdbcTemplate jdbcTemplate;

  private static final String SQL_INSERT = "INSERT INTO SWIFT_STATUS (SWIFT_REF, PAYMENT_ID, SWIFT_STATUS) VALUES (?," +
      "?,?)";

  private static final String SQL_UPDATE_PMT = "UPDATE SWIFT_STATUS SET SWIFT_STATUS = ?, UPDATED_ON = " +
      "CURRENT_TIMESTAMP, IS_ENABLED = 0 WHERE PAYMENT_ID = ?";

  private static final String SQL_UPDATE_REF = "UPDATE SWIFT_STATUS SET SWIFT_STATUS = ?, UPDATED_ON = " +
      "CURRENT_TIMESTAMP, IS_ENABLED = 0 WHERE SWIFT_REF = ?";

  private static final String SQL_FIND_ALL = "SELECT * FROM SWIFT_STATUS WHERE IS_ENABLED = 0";

  private static final String SQL_FIND_BY_REF = "SELECT * FROM SWIFT_STATUS WHERE SWIFT_REF = ?";

  private static final String SQL_FIND_BY_PMT_ID = "SELECT * FROM SWIFT_STATUS WHERE PAYMENT_ID = ?";

  private static final String SQL_FIND_BY_STATUS = "SELECT * FROM SWIFT_STATUS WHERE SWIFT_STATUS = ?";

  public void insertStatus(String reference, String paymentId, String status) {
    jdbcTemplate.update(con -> {
      PreparedStatement ps = con.prepareStatement(SQL_INSERT);
      ps.setString(1, reference);
      ps.setString(2, paymentId);
      ps.setString(3, status);
      return ps;
    });
  }

  public void updateStatusByPaymentId(String paymentId, String status) {
    jdbcTemplate.update(con -> {
      PreparedStatement ps = con.prepareStatement(SQL_UPDATE_PMT);
      ps.setString(1, status);
      ps.setString(2, paymentId);
      return ps;
    });
  }

  public void updateStatusByReference(String reference, String status) {
    jdbcTemplate.update(con -> {
      PreparedStatement ps = con.prepareStatement(SQL_UPDATE_REF);
      ps.setString(1, status);
      ps.setString(2, reference);
      return ps;
    });
  }

  public List<SwiftStatus> findAll() {
    return jdbcTemplate.query(SQL_FIND_ALL, (rs, rowNum) -> getSwiftStatus(rs));
  }

  public List<SwiftStatus> findByReference(String reference) {
    return jdbcTemplate.query(con -> {
      PreparedStatement ps = con.prepareStatement(SQL_FIND_BY_REF);
      ps.setString(1, reference);
      return ps;
    }, (rs, rowNum) -> getSwiftStatus(rs));
  }

  public List<SwiftStatus> findByPaymentId(String paymentId) {
    return jdbcTemplate.query(con -> {
      PreparedStatement ps = con.prepareStatement(SQL_FIND_BY_PMT_ID);
      ps.setString(1, paymentId);
      return ps;
    }, (rs, rowNum) -> getSwiftStatus(rs));
  }

  public List<SwiftStatus> findAllByStatus(String status) {
    return jdbcTemplate.query(con -> {
      PreparedStatement ps = con.prepareStatement(SQL_FIND_BY_STATUS);
      ps.setString(1, status);
      return ps;
    }, (rs, rowNum) -> getSwiftStatus(rs));
  }

  private SwiftStatus getSwiftStatus(ResultSet rs) throws SQLException {
    SwiftStatus status = new SwiftStatus();
    status.setId(rs.getInt("ID"));
    status.setReference(rs.getString("SWIFT_REF"));
    status.setStatus(rs.getString("SWIFT_STATUS"));
    status.setPaymentId(rs.getString("PAYMENT_ID"));
    status.setIsEnabled(rs.getInt("IS_ENABLED"));
    status.setCreatedOn(rs.getDate("CREATED_ON"));
    status.setUpdatedOn(rs.getDate("UPDATED_ON"));
    return status;
  }
}
