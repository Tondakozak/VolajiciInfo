package cz.tondakozak.volajiciinfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * This class manage DB of callers
 * The class uses SQLite
 */
public class PeopleDB {

    protected static final String DATABASE_NAME = "volajiciInfoDB2";
    protected static final int DATABASE_VERSION = 2;

    protected static final String TABLE_NAME = "people";


    private SQLiteOpenHelper openHelper;

    static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper (Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
         * Create the DB table
         * @param db
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            String tableCreationSQL =
                    "  CREATE TABLE `people` (" +
                            "`_id` INTEGER PRIMARY KEY," +
                            "  `firstname` text NOT NULL," +
                            "  `surname` text NOT NULL," +
                            "  `tel` text NOT NULL," +
                            "  `orderInfo` text NOT NULL," +
                            "  `info` text NOT NULL" +
                            ");";
            db.execSQL(tableCreationSQL);
        }

        /**
         * Upgrade the DB scheme
         * @param db
         * @param oldVersion
         * @param newVersion
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS people");
            onCreate(db);
        }
    }

    public PeopleDB (Context context) {
        openHelper = new DatabaseHelper(context);
    }

    /**
     * Return cursor with information about caller with given phone number
     * @param callerNumber
     * @return
     */
    public Cursor getMan(String callerNumber) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String callerNumberWithoutCode = callerNumber;
        if (callerNumber.length() > 4 && callerNumber.substring(0, 4).equals("+420")) {
            callerNumberWithoutCode = callerNumber.substring(4);
        }
        String[] selectionArgs = { callerNumber, callerNumberWithoutCode};
        String[] columns = {"tel", "firstname", "surname", "orderInfo", "info"};
        return db.query(TABLE_NAME, columns, "tel = ? OR tel=?", selectionArgs,
                null, null, null, "1");
    }

    /**
     * Return true if the given number is in the db
     * @param callerNumber
     * @return
     */
    private boolean manExists(String callerNumber) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String callerNumberWithoutCode = callerNumber;
        if (callerNumber.length() > 4 && callerNumber.substring(0, 4).equals("+420")) {
            callerNumberWithoutCode = callerNumber.substring(4);
        }
        String[] selectionArgs = { callerNumber, callerNumberWithoutCode};
        String[] columns = {"tel", "firstname", "surname", "orderInfo", "info"};
        Cursor man = db.query(TABLE_NAME, columns, "tel = ? OR tel = ?", selectionArgs,
                null, null, null, "1");
        return man.getCount() > 0;
    }

    /**
     * Insert new value into db. If the number already exists in it, update the existing one
     * @param tel
     * @param firstname
     * @param surname
     * @param orderInfo
     * @param info
     */
    public void insertOrUpdateMan(String tel, String firstname, String surname, String orderInfo, String info) {
        SQLiteDatabase db = openHelper.getWritableDatabase();

        ContentValues initialValues = new ContentValues();
        initialValues.put("tel", tel);
        initialValues.put("firstname", firstname);
        initialValues.put("surname", surname);
        initialValues.put("`orderInfo`", orderInfo);
        initialValues.put("info", info);

        String callerNumberWithoutCode = tel;
        if (tel.length() > 4 && tel.substring(0, 4).equals("+420")) {
            callerNumberWithoutCode = tel.substring(4);
        }

        if (manExists(tel)) { // if the number exists, update the row
            db.update(TABLE_NAME, initialValues, "tel=? OR tel=?", new String[] {tel, callerNumberWithoutCode});
        } else {
            // Insert new row
            db.insertWithOnConflict(TABLE_NAME, null, initialValues, SQLiteDatabase.CONFLICT_IGNORE);
        }


        // close db connection
        db.close();
    }


    /**
     * Delete all data from db
     */
    public void deleteData() {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        db.execSQL("DELETE FROM "+PeopleDB.TABLE_NAME);
        db.close();
    }

    /**
     * Close all db connections
     */
    public void close() {
        openHelper.close();
    }

    /**
     * Insert new rows or update existing one according to the given json
     * @param json
     */
    public void updateDBValues(JSONArray json) {
        for (int manId = 0; manId < json.length(); manId++) {

            try {
                JSONObject manJson = json.getJSONObject(manId);
                String tel = cleanNumberFormat(manJson.getString("tel"));
                insertOrUpdateMan(tel, manJson.getString("firstname").trim(), manJson.getString("surname").trim(), manJson.getString("order").trim(), "");
            } catch (JSONException ex) {
                Log.e("People DB", "Parse json error");
                Log.e("PeopleDB", ex.getMessage());
            }
        }
    }

    /**
     * Returns number of items in the db
     * @return
     */
    public int getNumberOfPeople() {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] selectionArgs = { };
        String[] columns = {"tel"};
        Cursor result =  db.query(TABLE_NAME, columns, "1 = 1", selectionArgs,
                null, null, null, null);

        return result.getCount();
    }

    /**
     * Remove no digit characters except for leading +
     * @param number
     * @return
     */
    public static String cleanNumberFormat(String number) {
        boolean firstPlus = (number.length() > 0 && number.charAt(0) == '+');
        number = number.replaceAll("[^0-9]", "");
        if (firstPlus) {
            number = "+"+number;
        }
        return number;
    }
}
