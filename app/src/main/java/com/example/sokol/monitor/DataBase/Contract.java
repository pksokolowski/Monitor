package com.example.sokol.monitor.DataBase;

import android.provider.BaseColumns;

/** Database contract, helper class
 * It's best practice to have this sort of a class to omit repetitively hard-coding all the column
 * names in code. Instead use Contract.[tableName].[columsWithinThatTable]
 *
 * Created by Sokol on 24.03.2018.
 */

final class Contract {
    // preventing accidental instantiation.
    private Contract() {
    }

    static class logs implements BaseColumns {
        static final String TABLE_NAME = "logs";
        static final String COLUMN_NAME_START_TIME= "start_time";
        static final String COLUMN_NAME_END_TIME = "end_time";
        static final String COLUMN_NAME_CAT = "category";
    }

    static class categories implements BaseColumns {
        static final String TABLE_NAME = "categories";
        static final String COLUMN_NAME_TITLE = "title";
        static final String COLUMN_NAME_INITIAL = "initial";
        static final String COLUMN_NAME_ORDER_NUMBER = "order_number";
        static final String COLUMN_NAME_STATUS = "status";
    }
}