package com.herocraftonline.townships.storage;

import com.herocraftonline.herostorage.api.SQLSerializer;
import com.herocraftonline.herostorage.api.SQLTable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author gabizou
 */

public class SQLInstance extends SQLSerializer {

    final List<SQLTable<?>> tables = new ArrayList(Arrays.asList());


    @Override
    public boolean init() {
        super.init();
        try {
            Connection connection = getConnection(); /// Our database connection
            for (SQLTable<?> table: tables){
                table.init(this, connection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }

}
