package com.mohsen.swift.dao.erp.repository;

import com.mohsen.swift.model.SwiftMessageVO;
import com.mohsen.swift.dao.erp.entity.SwiftPaymentsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

@Repository
public class PaymentsLogRepository {

  @Autowired
  @Qualifier("erpJdbcTemplate")
  private JdbcTemplate jdbcTemplate;

  private static final String SQL_FIND_ALL = "SELECT * FROM APPS.XXMohsen_SWIFT_PAYMENT_LOG_V";

  private static final String SQL_FIND_DELIVERED = "SELECT * FROM APPS.XXMohsen_SWIFT_PAYMENT_LOG_V WHERE " +
      "SWIFT_STATUS = 'DELIVERED'";

  private static final String SQL_FIND_LATEST_DATE = "SELECT t.SEQ, t.PAYMENT_ID, t.ACTION_DATE, t.SWIFT_STATUS " +
      "FROM APPS.XXMohsen_SWIFT_PAYMENT_LOG_V t " +
      "INNER JOIN ( " +
      "SELECT PAYMENT_ID, MAX(ACTION_DATE) as MAX_DATE " +
      "FROM APPS.XXMohsen_SWIFT_PAYMENT_LOG_V " +
      "GROUP BY PAYMENT_ID " +
      ") tm ON t.PAYMENT_ID = tm.PAYMENT_ID AND t.ACTION_DATE = tm.MAX_DATE ORDER BY t.SEQ";

  public List<SwiftPaymentsLog> findAll() {
    return jdbcTemplate.query(SQL_FIND_ALL, (rs, rowNum) -> getSwiftPaymentsLog(rs));
  }

  public List<SwiftPaymentsLog> findByLatestDate() {
    return jdbcTemplate.query(SQL_FIND_LATEST_DATE, (rs, rowNum) -> getSwiftPaymentsLog(rs));
  }

  public void updatePaymentStatus(SwiftMessageVO messageVO) {
    List<SqlParameter> parameters = Arrays.asList(new SqlParameter(Types.BIGINT), new SqlParameter(Types.VARCHAR));
    jdbcTemplate.call(con -> {
      CallableStatement cs = con.prepareCall("{call APPS.XXMohsen_SWIFT_PAYMENT_PKG.update_status(?,?)}");
      cs.setLong(1, Long.parseLong(messageVO.getPaymentId()));
      cs.setString(2, messageVO.getStatus());
      return cs;
    }, parameters);
  }

  public void updateMT940FileDetails(String fileName, String filePath) {
    List<SqlParameter> parameters = Arrays.asList(new SqlParameter(Types.BIGINT), new SqlParameter(Types.VARCHAR));
    jdbcTemplate.call(con -> {
      CallableStatement cs = con.prepareCall("{call APPS.XXMohsen_SWIFT_PAYMENT_PKG.insert_file_name_mt940(?,?)}");
      cs.setString(1, fileName);
      cs.setString(2, filePath);
      return cs;
    }, parameters);
  }

  private SwiftPaymentsLog getSwiftPaymentsLog(ResultSet rs) throws SQLException {
    SwiftPaymentsLog payments = new SwiftPaymentsLog();
    payments.setSeq(rs.getLong("SEQ"));
    payments.setPaymentId(rs.getLong("PAYMENT_ID"));
    payments.setStatus(rs.getString("SWIFT_STATUS"));
    payments.setActionDate(rs.getDate("ACTION_DATE"));
    return payments;
  }
}
