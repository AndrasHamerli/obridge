/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Ferenc Karsany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.obridge.dao;

import org.obridge.model.data.TypeAttribute;
import org.obridge.util.jdbc.JdbcTemplate;
import org.obridge.util.jdbc.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * User: fkarsany
 * Date: 2013.11.14.
 */
public class TypeDao {

    private static final String GET_TYPE_ATTRIBUTES = "SELECT attr_name, attr_type_name, attr_no, nvl(scale,-1) data_scale, case when attr_type_owner is not null then 1 else 0 end multi_type, bb.typecode, " +
            "(select elem_type_name from user_coll_types t where t.TYPE_NAME = aa.attr_type_name) collection_base_type " +
            "FROM user_type_attrs aa, user_types bb " +
            "WHERE UPPER(aa.type_name) = ? and aa.attr_type_name = bb.type_name(+) ORDER BY attr_no ASC";

    private JdbcTemplate jdbcTemplate;

    public TypeDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<String> getTypeList() {
        return jdbcTemplate.queryForList("SELECT type_name FROM user_types WHERE typecode = 'OBJECT'");
    }

    public List<TypeAttribute> getTypeAttributes(String typeName) {

        return jdbcTemplate.query(
                GET_TYPE_ATTRIBUTES,
                new Object[]{typeName.toUpperCase()}, new RowMapper<TypeAttribute>() {
                    @Override
                    public TypeAttribute mapRow(ResultSet resultSet, int i) throws SQLException {
                        return new TypeAttribute(
                                resultSet.getString("attr_name"),
                                resultSet.getString("attr_type_name"),
                                resultSet.getInt("attr_no"),
                                resultSet.getInt("data_scale"),
                                resultSet.getInt("multi_type"),
                                resultSet.getString("typecode"),
                                resultSet.getString("collection_base_type")
                        );
                    }
                }
        );
    }
}
