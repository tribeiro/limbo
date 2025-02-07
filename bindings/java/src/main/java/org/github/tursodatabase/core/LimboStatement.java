package org.github.tursodatabase.core;

import java.sql.SQLException;
import org.github.tursodatabase.annotations.NativeInvocation;
import org.github.tursodatabase.annotations.Nullable;
import org.github.tursodatabase.utils.LimboExceptionUtils;
import org.github.tursodatabase.utils.Logger;
import org.github.tursodatabase.utils.LoggerFactory;

/**
 * By default, only one <code>resultSet</code> object per <code>LimboStatement</code> can be open at
 * the same time. Therefore, if the reading of one <code>resultSet</code> object is interleaved with
 * the reading of another, each must have been generated by different <code>LimboStatement</code>
 * objects. All execution method in the <code>LimboStatement</code> implicitly close the current
 * <code>resultSet</code> object of the statement if an open one exists.
 */
public class LimboStatement {
  private static final Logger log = LoggerFactory.getLogger(LimboStatement.class);

  private final String sql;
  private final long statementPointer;
  private final LimboResultSet resultSet;

  private boolean closed;

  // TODO: what if the statement we ran was DDL, update queries and etc. Should we still create a
  // resultSet?
  public LimboStatement(String sql, long statementPointer) {
    this.sql = sql;
    this.statementPointer = statementPointer;
    this.resultSet = LimboResultSet.of(this);
    log.debug("Creating statement with sql: {}", this.sql);
  }

  public LimboResultSet getResultSet() {
    return resultSet;
  }

  /**
   * Expects a clean statement created right after prepare method is called.
   *
   * @return true if the ResultSet has at least one row; false otherwise.
   */
  public boolean execute() throws SQLException {
    resultSet.next();
    return resultSet.hasLastStepReturnedRow();
  }

  LimboStepResult step() throws SQLException {
    final LimboStepResult result = step(this.statementPointer);
    if (result == null) {
      throw new SQLException("step() returned null, which is only returned when an error occurs");
    }

    return result;
  }

  /**
   * Because Limbo supports async I/O, it is possible to return a {@link LimboStepResult} with
   * {@link LimboStepResult#STEP_RESULT_ID_ROW}. However, this is handled by the native side, so you
   * can expect that this method will not return a {@link LimboStepResult#STEP_RESULT_ID_ROW}.
   */
  @Nullable
  private native LimboStepResult step(long stmtPointer) throws SQLException;

  /**
   * Throws formatted SQLException with error code and message.
   *
   * @param errorCode Error code.
   * @param errorMessageBytes Error message.
   */
  @NativeInvocation(invokedFrom = "limbo_statement.rs")
  private void throwLimboException(int errorCode, byte[] errorMessageBytes) throws SQLException {
    LimboExceptionUtils.throwLimboException(errorCode, errorMessageBytes);
  }

  /**
   * Closes the current statement and releases any resources associated with it. This method calls
   * the native `_close` method to perform the actual closing operation.
   */
  public void close() throws SQLException {
    if (closed) {
      return;
    }
    this.resultSet.close();
    _close(statementPointer);
    closed = true;
  }

  private native void _close(long statementPointer);

  /**
   * Checks if the statement is closed.
   *
   * @return true if the statement is closed, false otherwise.
   */
  public boolean isClosed() {
    return closed;
  }

  @Override
  public String toString() {
    return "LimboStatement{"
        + "statementPointer="
        + statementPointer
        + ", sql='"
        + sql
        + '\''
        + '}';
  }
}
