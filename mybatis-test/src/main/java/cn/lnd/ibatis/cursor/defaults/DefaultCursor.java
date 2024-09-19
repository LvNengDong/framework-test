package cn.lnd.ibatis.cursor.defaults;

import cn.lnd.ibatis.cursor.Cursor;
import cn.lnd.ibatis.executor.resultset.DefaultResultSetHandler;
import cn.lnd.ibatis.executor.resultset.ResultSetWrapper;
import cn.lnd.ibatis.mapping.ResultMap;
import cn.lnd.ibatis.session.ResultContext;
import cn.lnd.ibatis.session.ResultHandler;
import cn.lnd.ibatis.session.RowBounds;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 21:15
 */
public class DefaultCursor<T> implements Cursor<T> {

    // ResultSetHandler stuff
    private final DefaultResultSetHandler resultSetHandler;
    private final ResultMap resultMap;
    private final ResultSetWrapper rsw;
    private final RowBounds rowBounds;
    private final cn.lnd.ibatis.cursor.defaults.DefaultCursor.ObjectWrapperResultHandler<T> objectWrapperResultHandler = new cn.lnd.ibatis.cursor.defaults.DefaultCursor.ObjectWrapperResultHandler<T>();

    private final cn.lnd.ibatis.cursor.defaults.DefaultCursor.CursorIterator cursorIterator = new cn.lnd.ibatis.cursor.defaults.DefaultCursor.CursorIterator();
    private boolean iteratorRetrieved;

    private cn.lnd.ibatis.cursor.defaults.DefaultCursor.CursorStatus status = cn.lnd.ibatis.cursor.defaults.DefaultCursor.CursorStatus.CREATED;
    private int indexWithRowBound = -1;

    private enum CursorStatus {

        /**
         * A freshly created cursor, database ResultSet consuming has not started
         */
        CREATED,
        /**
         * A cursor currently in use, database ResultSet consuming has started
         */
        OPEN,
        /**
         * A closed cursor, not fully consumed
         */
        CLOSED,
        /**
         * A fully consumed cursor, a consumed cursor is always closed
         */
        CONSUMED
    }

    public DefaultCursor(DefaultResultSetHandler resultSetHandler, ResultMap resultMap, ResultSetWrapper rsw, RowBounds rowBounds) {
        this.resultSetHandler = resultSetHandler;
        this.resultMap = resultMap;
        this.rsw = rsw;
        this.rowBounds = rowBounds;
    }

    @Override
    public boolean isOpen() {
        return status == cn.lnd.ibatis.cursor.defaults.DefaultCursor.CursorStatus.OPEN;
    }

    @Override
    public boolean isConsumed() {
        return status == cn.lnd.ibatis.cursor.defaults.DefaultCursor.CursorStatus.CONSUMED;
    }

    @Override
    public int getCurrentIndex() {
        return rowBounds.getOffset() + cursorIterator.iteratorIndex;
    }

    @Override
    public Iterator<T> iterator() {
        if (iteratorRetrieved) {
            throw new IllegalStateException("Cannot open more than one iterator on a Cursor");
        }
        iteratorRetrieved = true;
        return cursorIterator;
    }

    @Override
    public void close() {
        if (isClosed()) {
            return;
        }

        ResultSet rs = rsw.getResultSet();
        try {
            if (rs != null) {
                Statement statement = rs.getStatement();

                rs.close();
                if (statement != null) {
                    statement.close();
                }
            }
            status = cn.lnd.ibatis.cursor.defaults.DefaultCursor.CursorStatus.CLOSED;
        } catch (SQLException e) {
            // ignore
        }
    }

    protected T fetchNextUsingRowBound() {
        T result = fetchNextObjectFromDatabase();
        while (result != null && indexWithRowBound < rowBounds.getOffset()) {
            result = fetchNextObjectFromDatabase();
        }
        return result;
    }

    protected T fetchNextObjectFromDatabase() {
        if (isClosed()) {
            return null;
        }

        try {
            status = cn.lnd.ibatis.cursor.defaults.DefaultCursor.CursorStatus.OPEN;
            resultSetHandler.handleRowValues(rsw, resultMap, objectWrapperResultHandler, RowBounds.DEFAULT, null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        T next = objectWrapperResultHandler.result;
        if (next != null) {
            indexWithRowBound++;
        }
        // No more object or limit reached
        if (next == null || (getReadItemsCount() == rowBounds.getOffset() + rowBounds.getLimit())) {
            close();
            status = cn.lnd.ibatis.cursor.defaults.DefaultCursor.CursorStatus.CONSUMED;
        }
        objectWrapperResultHandler.result = null;

        return next;
    }

    private boolean isClosed() {
        return status == cn.lnd.ibatis.cursor.defaults.DefaultCursor.CursorStatus.CLOSED || status == cn.lnd.ibatis.cursor.defaults.DefaultCursor.CursorStatus.CONSUMED;
    }

    private int getReadItemsCount() {
        return indexWithRowBound + 1;
    }

    private static class ObjectWrapperResultHandler<T> implements ResultHandler<T> {

        private T result;

        @Override
        public void handleResult(ResultContext<? extends T> context) {
            this.result = context.getResultObject();
            context.stop();
        }
    }

    private class CursorIterator implements Iterator<T> {

        /**
         * Holder for the next object to be returned
         */
        T object;

        /**
         * Index of objects returned using next(), and as such, visible to users.
         */
        int iteratorIndex = -1;

        @Override
        public boolean hasNext() {
            if (object == null) {
                object = fetchNextUsingRowBound();
            }
            return object != null;
        }

        @Override
        public T next() {
            // Fill next with object fetched from hasNext()
            T next = object;

            if (next == null) {
                next = fetchNextUsingRowBound();
            }

            if (next != null) {
                object = null;
                iteratorIndex++;
                return next;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Cannot remove element from Cursor");
        }
    }
}
