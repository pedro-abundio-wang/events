package com.events.common.jdbc.executor;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface EventsRowMapper<T> {
    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
