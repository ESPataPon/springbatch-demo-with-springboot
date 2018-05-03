package batch.config;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao;
import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.serializer.Serializer;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * @version :1.0.0
 * @author: term
 * @time: 2018-04-24 22:09
 * @description :
 */
public class MyJdbcExecutionContextDao  extends AbstractJdbcBatchMetadataDao implements ExecutionContextDao {

    private static final String FIND_JOB_EXECUTION_CONTEXT = "SELECT SHORT_CONTEXT, SERIALIZED_CONTEXT "
            + "FROM %PREFIX%JOB_EXECUTION_CONTEXT WHERE JOB_EXECUTION_ID = ?";

    private static final String INSERT_JOB_EXECUTION_CONTEXT = "INSERT INTO %PREFIX%JOB_EXECUTION_CONTEXT "
            + "(SHORT_CONTEXT, SERIALIZED_CONTEXT, JOB_EXECUTION_ID) " + "VALUES(?, ?, ?)";

//    private static final String INSERT_JOB_EXECUTION_CONTEXT = "INSERT INTO %PREFIX%JOB_EXECUTION_CONTEXT "
//            + "(SHORT_CONTEXT, SERIALIZED_CONTEXT, JOB_EXECUTION_ID,CREATEAT) " + "VALUES(?, ?, ?, ?)";


    private static final String UPDATE_JOB_EXECUTION_CONTEXT = "UPDATE %PREFIX%JOB_EXECUTION_CONTEXT "
            + "SET SHORT_CONTEXT = ?, SERIALIZED_CONTEXT = ? " + "WHERE JOB_EXECUTION_ID = ?";

    private static final String FIND_STEP_EXECUTION_CONTEXT = "SELECT SHORT_CONTEXT, SERIALIZED_CONTEXT "
            + "FROM %PREFIX%STEP_EXECUTION_CONTEXT WHERE STEP_EXECUTION_ID = ?";

    private static final String INSERT_STEP_EXECUTION_CONTEXT = "INSERT INTO %PREFIX%STEP_EXECUTION_CONTEXT "
            + "(SHORT_CONTEXT, SERIALIZED_CONTEXT, STEP_EXECUTION_ID) " + "VALUES(?, ?, ?)";

    private static final String UPDATE_STEP_EXECUTION_CONTEXT = "UPDATE %PREFIX%STEP_EXECUTION_CONTEXT "
            + "SET SHORT_CONTEXT = ?, SERIALIZED_CONTEXT = ? " + "WHERE STEP_EXECUTION_ID = ?";

    private static final int DEFAULT_MAX_VARCHAR_LENGTH = 2500;

    private int shortContextLength = DEFAULT_MAX_VARCHAR_LENGTH;

    private LobHandler lobHandler = new DefaultLobHandler();

    private ExecutionContextSerializer serializer;

    /**
     * Setter for {@link Serializer} implementation
     *
     * @param serializer
     */
    public void setSerializer(ExecutionContextSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * The maximum size that an execution context can have and still be stored
     * completely in short form in the column <code>SHORT_CONTEXT</code>.
     * Anything longer than this will overflow into large-object storage, and
     * the first part only will be retained in the short form for readability.
     * Default value is 2500. Clients using multi-bytes charsets on the database
     * server may need to reduce this value to as little as half the value of
     * the column size.
     * @param shortContextLength
     */
    public void setShortContextLength(int shortContextLength) {
        this.shortContextLength = shortContextLength;
    }

    @Override
    public ExecutionContext getExecutionContext(JobExecution jobExecution) {
        Long executionId = jobExecution.getId();
        Assert.notNull(executionId, "ExecutionId must not be null.");

        List<ExecutionContext> results = getJdbcTemplate().query(getQuery(FIND_JOB_EXECUTION_CONTEXT),
                new MyJdbcExecutionContextDao.ExecutionContextRowMapper(), executionId);
        if (results.size() > 0) {
            return results.get(0);
        }
        else {
            return new ExecutionContext();
        }
    }

    @Override
    public ExecutionContext getExecutionContext(StepExecution stepExecution) {
        Long executionId = stepExecution.getId();
        Assert.notNull(executionId, "ExecutionId must not be null.");

        List<ExecutionContext> results = getJdbcTemplate().query(getQuery(FIND_STEP_EXECUTION_CONTEXT),
                new MyJdbcExecutionContextDao.ExecutionContextRowMapper(), executionId);
        if (results.size() > 0) {
            return results.get(0);
        }
        else {
            return new ExecutionContext();
        }
    }

    @Override
    public void updateExecutionContext(final JobExecution jobExecution) {
        Long executionId = jobExecution.getId();
        ExecutionContext executionContext = jobExecution.getExecutionContext();
        Assert.notNull(executionId, "ExecutionId must not be null.");
        Assert.notNull(executionContext, "The ExecutionContext must not be null.");

        String serializedContext = serializeContext(executionContext);

        persistSerializedContext(executionId, serializedContext, UPDATE_JOB_EXECUTION_CONTEXT);
    }

    @Override
    public void updateExecutionContext(final StepExecution stepExecution) {
        // Attempt to prevent concurrent modification errors by blocking here if
        // someone is already trying to do it.
        synchronized (stepExecution) {
            Long executionId = stepExecution.getId();
            ExecutionContext executionContext = stepExecution.getExecutionContext();
            Assert.notNull(executionId, "ExecutionId must not be null.");
            Assert.notNull(executionContext, "The ExecutionContext must not be null.");

            String serializedContext = serializeContext(executionContext);

            persistSerializedContext(executionId, serializedContext, UPDATE_STEP_EXECUTION_CONTEXT);
        }
    }

    @Override
    public void saveExecutionContext(JobExecution jobExecution) {

        Long executionId = jobExecution.getId();
        ExecutionContext executionContext = jobExecution.getExecutionContext();
        Assert.notNull(executionId, "ExecutionId must not be null.");
        Assert.notNull(executionContext, "The ExecutionContext must not be null.");

        String serializedContext = serializeContext(executionContext);

        persistSerializedContext(executionId, serializedContext, INSERT_JOB_EXECUTION_CONTEXT);
    }

    @Override
    public void saveExecutionContext(StepExecution stepExecution) {
        Long executionId = stepExecution.getId();
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        Assert.notNull(executionId, "ExecutionId must not be null.");
        Assert.notNull(executionContext, "The ExecutionContext must not be null.");

        String serializedContext = serializeContext(executionContext);

        persistSerializedContext(executionId, serializedContext, INSERT_STEP_EXECUTION_CONTEXT);
    }

    @Override
    public void saveExecutionContexts(Collection<StepExecution> stepExecutions) {
        Assert.notNull(stepExecutions, "Attempt to save an null collection of step executions");
        Map<Long, String> serializedContexts = new HashMap<Long, String>(stepExecutions.size());
        for (StepExecution stepExecution : stepExecutions) {
            Long executionId = stepExecution.getId();
            ExecutionContext executionContext = stepExecution.getExecutionContext();
            Assert.notNull(executionId, "ExecutionId must not be null.");
            Assert.notNull(executionContext, "The ExecutionContext must not be null.");
            serializedContexts.put(executionId, serializeContext(executionContext));
        }
        persistSerializedContexts(serializedContexts, INSERT_STEP_EXECUTION_CONTEXT);
    }

    public void setLobHandler(LobHandler lobHandler) {
        this.lobHandler = lobHandler;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
    }

    /**
     * @param executionId
     * @param serializedContext
     * @param sql with parameters (shortContext, longContext, executionId)
     */
    private void persistSerializedContext(final Long executionId, String serializedContext, String sql) {

        final String shortContext;
        final String longContext;
        if (serializedContext.length() > shortContextLength) {
            // Overestimate length of ellipsis to be on the safe side with
            // 2-byte chars
            shortContext = serializedContext.substring(0, shortContextLength - 8) + " ...";
            longContext = serializedContext;
        }
        else {
            shortContext = serializedContext;
            longContext = null;
        }

        getJdbcTemplate().update(getQuery(sql), new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, shortContext);
                if (longContext != null) {
                    lobHandler.getLobCreator().setClobAsString(ps, 2, longContext);
                }
                else {
                    ps.setNull(2, getClobTypeToUse());
                }
                ps.setLong(3, executionId);
//                ps.setTimestamp(4,new Timestamp(System.currentTimeMillis()));
            }
        });
    }

    /**
     * @param serializedContexts
     * @param sql with parameters (shortContext, longContext, executionId)
     */
    private void persistSerializedContexts(final Map<Long, String> serializedContexts, String sql) {
        if (!serializedContexts.isEmpty()) {
            final Iterator<Long> executionIdIterator = serializedContexts.keySet().iterator();

            getJdbcTemplate().batchUpdate(getQuery(sql), new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Long executionId = executionIdIterator.next();
                    String serializedContext = serializedContexts.get(executionId);
                    String shortContext;
                    String longContext;
                    if (serializedContext.length() > shortContextLength) {
                        // Overestimate length of ellipsis to be on the safe side with
                        // 2-byte chars
                        shortContext = serializedContext.substring(0, shortContextLength - 8) + " ...";
                        longContext = serializedContext;
                    } else {
                        shortContext = serializedContext;
                        longContext = null;
                    }
                    ps.setString(1, shortContext);
                    if (longContext != null) {
                        lobHandler.getLobCreator().setClobAsString(ps, 2, longContext);
                    } else {
                        ps.setNull(2, getClobTypeToUse());
                    }
                    ps.setLong(3, executionId);
                }

                @Override
                public int getBatchSize() {
                    return serializedContexts.size();
                }
            });
        }
    }

    private String serializeContext(ExecutionContext ctx) {
        Map<String, Object> m = new HashMap<String, Object>();
        for (Map.Entry<String, Object> me : ctx.entrySet()) {
            m.put(me.getKey(), me.getValue());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String results = "";

        try {
            serializer.serialize(m, out);
            results = new String(out.toByteArray(), "ISO-8859-1");
        }
        catch (IOException ioe) {
            throw new IllegalArgumentException("Could not serialize the execution context", ioe);
        }

        return results;
    }

    private class ExecutionContextRowMapper implements RowMapper<ExecutionContext> {

        @Override
        public ExecutionContext mapRow(ResultSet rs, int i) throws SQLException {
            ExecutionContext executionContext = new ExecutionContext();
            String serializedContext = rs.getString("SERIALIZED_CONTEXT");
            if (serializedContext == null) {
                serializedContext = rs.getString("SHORT_CONTEXT");
            }

            Map<String, Object> map;
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(serializedContext.getBytes("ISO-8859-1"));
                map = serializer.deserialize(in);
            }
            catch (IOException ioe) {
                throw new IllegalArgumentException("Unable to deserialize the execution context", ioe);
            }
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                executionContext.put(entry.getKey(), entry.getValue());
            }
            return executionContext;
        }
    }

}
