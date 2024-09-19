package cn.lnd.ibatis.executor;

import java.sql.BatchUpdateException;
import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 21:04
 */
public class BatchExecutorException extends ExecutorException {

    private static final long serialVersionUID = 154049229650533990L;
    private final List<cn.lnd.ibatis.executor.BatchResult> successfulBatchResults;
    private final BatchUpdateException batchUpdateException;
    private final cn.lnd.ibatis.executor.BatchResult batchResult;

    public BatchExecutorException(String message,
                                  BatchUpdateException cause,
                                  List<cn.lnd.ibatis.executor.BatchResult> successfulBatchResults,
                                  cn.lnd.ibatis.executor.BatchResult batchResult) {
        super(message + " Cause: " + cause, cause);
        this.batchUpdateException = cause;
        this.successfulBatchResults = successfulBatchResults;
        this.batchResult = batchResult;
    }

    /*
     * Returns the BatchUpdateException that caused the nested executor
     * to fail.  That exception contains an array of row counts
     * that can be used to determine exactly which statemtn of the
     * executor caused the failure (or failures).
     *
     * @return the root BatchUpdateException
     */
    public BatchUpdateException getBatchUpdateException() {
        return batchUpdateException;
    }

    /*
     * Returns a list of BatchResult objects.  There will be one entry
     * in the list for each successful sub-executor executed before the failing
     * executor.
     *
     * @return the previously successful executor results (may be an empty list
     *         if no executor has executed successfully)
     */
    public List<BatchResult> getSuccessfulBatchResults() {
        return successfulBatchResults;
    }

    /*
     * Returns the SQL statement that caused the failure
     * (not the parameterArray)
     *
     * @return the failing SQL string
     */
    public String getFailingSqlStatement() {
        return batchResult.getSql();
    }

    /*
     * Returns the statement id of the statement that caused the failure
     *
     * @return the statement id
     */
    public String getFailingStatementId() {
        return batchResult.getMappedStatement().getId();
    }
}
