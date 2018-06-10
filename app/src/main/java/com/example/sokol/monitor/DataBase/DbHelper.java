package com.example.sokol.monitor.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.example.sokol.monitor.CatData;
import com.example.sokol.monitor.LogsData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * db helper Singleton
 * Created by Sokol on 24.03.2018.
 */

public class DbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dataBase.db";

    private static DbHelper sInstance;
    private static SQLiteDatabase sDataBase;

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private void loadWritableDatabaseIfNotLoadedAlready() {
        if (sDataBase == null) {
            sDataBase = sInstance.getWritableDatabase();
        }
    }

    public void shutdown() {
        if (sDataBase != null) if (sDataBase.isOpen()) sDataBase.close();
        if (sInstance != null) sInstance.close();

        sInstance = null;
        sDataBase = null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CATEGORIES);
        db.execSQL(SQL_CREATE_LOGS);
    }

    public static synchronized DbHelper getInstance(Context appContext) {
        Context app = appContext.getApplicationContext();

        if (sInstance == null)
            sInstance = new DbHelper(app);

        return sInstance;
    }

    private static final String SQL_CREATE_CATEGORIES =
            "CREATE TABLE " + Contract.categories.TABLE_NAME + " (" +
                    Contract.categories._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.categories.COLUMN_NAME_TITLE + " TEXT, " +
                    Contract.categories.COLUMN_NAME_INITIAL + " TEXT, " +
                    Contract.categories.COLUMN_NAME_ORDER_NUMBER + " INTEGER, " +
                    Contract.categories.COLUMN_NAME_STATUS + " INTEGER);";

    private static final String SQL_CREATE_LOGS =
            "CREATE TABLE " + Contract.logs.TABLE_NAME + " (" +
                    Contract.logs._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.logs.COLUMN_NAME_START_TIME + " INTEGER, " +
                    Contract.logs.COLUMN_NAME_END_TIME + " INTEGER, " +
                    Contract.logs.COLUMN_NAME_CAT + " INTEGER," +
                    " FOREIGN KEY (" + Contract.logs.COLUMN_NAME_CAT + ") REFERENCES " + Contract.categories.TABLE_NAME + "(" + Contract.categories._ID + "));";

    private static final String SQL_DELETE_EVERYTHING =
            "DROP TABLE IF EXISTS " + Contract.logs.TABLE_NAME + "; " +
                    "DROP TABLE IF EXISTS " + Contract.categories.TABLE_NAME;

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        sqLiteDatabase.execSQL(SQL_DELETE_EVERYTHING);
        onCreate(sqLiteDatabase);
    }

    /**
     * pobiera wszystkie istniejące kategorie z bazy danych, ale tylko od min_status wzwyż.
     */
    public List<CatData> getCategories(int min_status) {
        loadWritableDatabaseIfNotLoadedAlready();

        String[] projection = {
                Contract.categories._ID,
                Contract.categories.COLUMN_NAME_TITLE,
                Contract.categories.COLUMN_NAME_INITIAL,
                Contract.categories.COLUMN_NAME_ORDER_NUMBER,
                Contract.categories.COLUMN_NAME_STATUS
        };

        String whereClause = Contract.categories.COLUMN_NAME_STATUS + " >=? ";

        String[] whereArgs = new String[]{
                String.valueOf(min_status)
        };

        Cursor cursor = sDataBase.query(
                Contract.categories.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                whereClause,                                // The columns for the WHERE clause
                whereArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                Contract.categories.COLUMN_NAME_ORDER_NUMBER + " ASC"     // The sort order
        );

        // establish column indexes:
        final int column_ID = cursor.getColumnIndex(Contract.categories._ID);
        final int column_title = cursor.getColumnIndex(Contract.categories.COLUMN_NAME_TITLE);
        final int column_initial = cursor.getColumnIndex(Contract.categories.COLUMN_NAME_INITIAL);
        final int column_status = cursor.getColumnIndex(Contract.categories.COLUMN_NAME_STATUS);

        List<CatData> cats = new ArrayList<>();
        while (cursor.moveToNext()) {

            cats.add(new CatData(cursor.getLong(column_ID),
                    cursor.getString(column_title),
                    cursor.getString(column_initial),
                    cursor.getInt(column_status)));
        }

        // got to close the cursor when no longer used!
        cursor.close();

        return cats;
    }

    /**
     * saves/updates cats in db.
     *
     * @param cats list of cats from the cats editor
     */
    public void pushCategories(List<CatData> cats) {
        List<CatData> dbCats = getCategories(CatData.CATEGORY_STATUS_DELETED);

        // put cats as they are in db into hashmap
        HashMap<String, CatData> h_dbCats = new HashMap<>();
        // also store indexes for quick comparisons
        HashMap<String, Integer> h_db_ordernum = new HashMap<>();
        for (int i = 0; i < dbCats.size(); i++) {
            CatData cat = dbCats.get(i);
            if (h_dbCats.containsKey(cat.getTitle())) continue;
            h_dbCats.put(cat.getTitle(), cat);
            h_db_ordernum.put(cat.getTitle(), i);
        }
        // put new state of cats into a hashmap
        HashMap<String, CatData> h_cats = new HashMap<>();
        for (CatData cat : cats) {
            if (h_cats.containsKey(cat.getTitle())) continue;
            h_cats.put(cat.getTitle(), cat);
        }

        for (int i = 0; i < cats.size(); i++) {
            CatData cat = cats.get(i);
            CatData dbCat = h_dbCats.get(cat.getTitle());
            if (dbCat == null) {
                // nowy kot, dodaj do db
                addCategory(cat, i);
            } else if (!cat.equals(dbCat) || h_db_ordernum.get(cat.getTitle()) != i) {
                // zmieniony kot, zaktualizuj kota w db:
                updateCategory(cat, i);

            }
        }

        // look for deleted ones: those who are in dbCats but no longer returned in cats.
        // update such to have -1, or "deleted", status
        for (int i = 0; i < dbCats.size(); i++) {
            CatData cat = dbCats.get(i);
            if (cat.getStatus() > CatData.CATEGORY_STATUS_DELETED && !h_cats.containsKey(cat.getTitle())) {
                // create a "deleted" copy of the cat
                CatData deleted_cat = new CatData(cat.getID(), cat.getTitle(), cat.getInitial(), CatData.CATEGORY_STATUS_DELETED);
                // update the cat to deleted status in db
                updateCategory(deleted_cat, i);
            }
        }
    }


    /**
     * Saves a new category to db.
     *
     * @param cat       the category to save
     * @param order_num the number of it on the list, ordering number.
     */
    private void addCategory(CatData cat, int order_num) {
        loadWritableDatabaseIfNotLoadedAlready();
        ContentValues cv = new ContentValues();
        cv.put(Contract.categories.COLUMN_NAME_TITLE, cat.getTitle());
        cv.put(Contract.categories.COLUMN_NAME_INITIAL, cat.getInitial());
        cv.put(Contract.categories.COLUMN_NAME_ORDER_NUMBER, order_num);
        cv.put(Contract.categories.COLUMN_NAME_STATUS, cat.getStatus());
        long newRowId = sDataBase.insert(Contract.categories.TABLE_NAME, null, cv);
    }

    /**
     * updates cat based on cat.getTitle(), because it is assumed that no two cats can exist with same name
     * and ID is not transfered back and forth out of the db to prevent odd errors in the future messing up the data.
     * ... and to keep the code clean
     *
     * @param cat
     * @param order_num
     */
    private void updateCategory(CatData cat, int order_num) {
        loadWritableDatabaseIfNotLoadedAlready();
        ContentValues cv = new ContentValues();
        cv.put(Contract.categories.COLUMN_NAME_INITIAL, cat.getInitial());
        cv.put(Contract.categories.COLUMN_NAME_ORDER_NUMBER, order_num);
        cv.put(Contract.categories.COLUMN_NAME_STATUS, cat.getStatus());
        String whereClause = Contract.categories.COLUMN_NAME_TITLE + " =? ";
        String[] whereArgs = {
                cat.getTitle()
        };
        long numOFRowsAffected = sDataBase.update(Contract.categories.TABLE_NAME, cv, whereClause, whereArgs);
    }

    public void pushLog(long catID, long startTime, long endTime) {
        loadWritableDatabaseIfNotLoadedAlready();
        ContentValues cv = new ContentValues();
        cv.put(Contract.logs.COLUMN_NAME_START_TIME, startTime);
        cv.put(Contract.logs.COLUMN_NAME_END_TIME, endTime);
        cv.put(Contract.logs.COLUMN_NAME_CAT, catID);
        long newRowId = sDataBase.insert(Contract.logs.TABLE_NAME, null, cv);
    }

    public void changeLog(long logID, long catID, long startTime, long endTime){
        loadWritableDatabaseIfNotLoadedAlready();
        ContentValues cv = new ContentValues();
        cv.put(Contract.logs.COLUMN_NAME_START_TIME, startTime);
        cv.put(Contract.logs.COLUMN_NAME_END_TIME, endTime);
        cv.put(Contract.logs.COLUMN_NAME_CAT, catID);
        String whereClause = Contract.logs._ID + " =? ";
        String[] whereArgs = {
                String.valueOf(logID)
        };
        long numOFRowsAffected = sDataBase.update(Contract.logs.TABLE_NAME, cv, whereClause, whereArgs);
    }

    public void deleteLog(long logID){
        loadWritableDatabaseIfNotLoadedAlready();
        String whereClause = Contract.logs._ID + " =? ";
        String[] whereArgs = {
                String.valueOf(logID)
        };
        sDataBase.delete(Contract.logs.TABLE_NAME, whereClause, whereArgs);
    }

    public LogsData getLogs(long since, long till, Long[] categories) {
        // return empty set for empty categories requested list...
        if (categories.length == 0) {
            return new LogsData.LogsDataBuilder().spitItOut();
        }

        loadWritableDatabaseIfNotLoadedAlready();

        String[] projection = {
                Contract.logs.COLUMN_NAME_START_TIME,
                Contract.logs.COLUMN_NAME_END_TIME,
                Contract.logs.COLUMN_NAME_CAT,
                Contract.logs._ID
        };

        String whereClause = "("+Contract.logs.COLUMN_NAME_START_TIME + " >=? OR " +Contract.logs.COLUMN_NAME_END_TIME + " >= ? ) AND " + Contract.logs.COLUMN_NAME_START_TIME + " <? AND " + Contract.logs.COLUMN_NAME_CAT + " IN " + makePlaceholders(categories.length);

        List<String> whereArgsList = new ArrayList<>(1+categories.length);
        whereArgsList.add(String.valueOf(since));
        whereArgsList.add(String.valueOf(since));
        whereArgsList.add(String.valueOf(till));
        for(int i =0; i< categories.length; i++){
            whereArgsList.add(String.valueOf(categories[i]));
        }
        String[] whereArgs = whereArgsList.toArray(new String[whereArgsList.size()]);

        Cursor cursor = sDataBase.query(
                Contract.logs.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                whereClause,                                // The columns for the WHERE clause
                whereArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null
        );

        final int column_start = cursor.getColumnIndex(Contract.logs.COLUMN_NAME_START_TIME);
        final int column_end = cursor.getColumnIndex(Contract.logs.COLUMN_NAME_END_TIME);
        final int column_catId = cursor.getColumnIndex(Contract.logs.COLUMN_NAME_CAT);
        final int column_Id = cursor.getColumnIndex(Contract.logs._ID);

        LogsData.LogsDataBuilder lbuilder = new LogsData.LogsDataBuilder();
        while (cursor.moveToNext()) {

            lbuilder.addDataPoint(
                    Math.max(cursor.getLong(column_start), since),
                    Math.min(cursor.getLong(column_end), till),
                    cursor.getLong(column_Id),
                    cursor.getLong(column_catId)
            );
        }

        // got to close the cursor when no longer used!
        cursor.close();

        return lbuilder.spitItOut();
    }

    String makePlaceholders(int len) {
        return "(" + TextUtils.join(",", Collections.nCopies(len, "?")) + ")";
    }
}
