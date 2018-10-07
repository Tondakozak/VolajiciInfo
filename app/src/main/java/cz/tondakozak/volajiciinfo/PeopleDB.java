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
    protected static final int DATABASE_VERSION = 3;

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
                            "  `tel` text NOT NULL," +
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

        setIdd();
        setPrefix();
    }

    public String[] idd;
    public String[] prefix;

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
        String[] columns = {"tel", "info"};
        return db.query(TABLE_NAME, columns, "tel = ? OR tel=?", selectionArgs,
                null, null, null, "10");
    }

    public Cursor getManWithoutCode(String callerNumber) {
        SQLiteDatabase db = openHelper.getReadableDatabase();

        String[] selectionArgs = {"%"+callerNumber};
        String[] columns = {"tel", "info"};
        return db.query(TABLE_NAME, columns, "tel LIKE ", selectionArgs,
                null, null, null, "10");
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
        String[] columns = {"tel", "info"};
        Cursor man = db.query(TABLE_NAME, columns, "tel = ? OR tel = ?", selectionArgs,
                null, null, null, "1");
        return man.getCount() > 0;
    }

    /**
     * Insert new value into db. If the number already exists in it, update the existing one
     * @param tel
     * @param info
     */
    public void insertOrUpdateMan(String tel, String info) {
        SQLiteDatabase db = openHelper.getWritableDatabase();

        ContentValues initialValues = new ContentValues();
        initialValues.put("tel", tel);
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
     * Insert new value into db.
     * @param tel
     * @param info
     */
    public void insertMan(String tel, String info) {
        SQLiteDatabase db = openHelper.getWritableDatabase();

        ContentValues initialValues = new ContentValues();
        initialValues.put("tel", tel);
        initialValues.put("info", info);


        // Insert new row
        db.insertWithOnConflict(TABLE_NAME, null, initialValues, SQLiteDatabase.CONFLICT_IGNORE);



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

        // delete old data
        deleteData();

        // insert new data
        for (int manId = 0; manId < json.length(); manId++) {

            try {
                JSONObject manJson = json.getJSONObject(manId);
                String tel = cleanNumberFormat(manJson.getString("tel"));
                insertMan(tel, manJson.getJSONArray("lines").toString());
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


    private void setIdd() {
        String[] idd = {
                "+",
                "00",
                "011",
                "0011",
                "810",
                "0010",
                "0014",
                "15",
                "005",
                "119",
                "009",
                "001",
                "010",
                "000",
                "020",
                "99",
                "05",
                "002",
                "09",
                "19"};
        this.idd = idd;

    }

    private void setPrefix() {
        String[] prefix = {
                "421",
                "420",
                "93",
            "355",
            "213",
            "684",
            "376",
            "244",
            "809",
            "268",
            "54",
            "374",
            "297",
            "247",
            "61",
            "672",
            "43",
            "994",
            "242",
            "246",
            "973",
            "880",
            "375",
            "32",
            "501",
            "229",
            "975",
            "284",
            "591",
            "387",
            "267",
            "55",
            "673",
            "359",
            "226",
            "257",
            "855",
            "237",
            "1",
            "238",
            "345",
            "236",
            "235",
            "56",
            "86",
            "886",
            "57",
            "269",
            "682",
            "506",
            "385",
            "53",
            "357",
            "45",
            "767",
            "253",
            "593",
            "20",
            "503",
            "240",
            "291",
            "372",
            "251",
            "500",
            "298",
            "679",
            "358",
            "33",
            "596",
            "594",
            "241",
            "220",
            "995",
            "49",
            "233",
            "350",
            "30",
            "299",
            "473",
            "671",
            "502",
            "224",
            "245",
            "592",
            "509",
            "504",
            "852",
            "36",
            "354",
            "91",
            "62",
            "98",
            "964",
            "353",
            "972",
            "39",
            "225",
            "876",
            "81",
            "962",
            "7",
            "254",
            "686",
            "82",
            "850",
            "965",
            "996",
            "371",
            "856",
            "961",
            "266",
            "231",
            "370",
            "218",
            "423",
            "352",
            "853",
            "389",
            "261",
            "265",
            "60",
            "960",
            "223",
            "356",
            "692",
            "222",
            "230",
            "52",
            "691",
            "373",
            "976",
            "212",
            "258",
            "95",
            "264",
            "674",
            "977",
            "31",
            "599",
            "869",
            "687",
            "64",
            "505",
            "227",
            "234",
            "683",
            "1670",
            "47",
            "968",
            "92",
            "680",
            "507",
            "675",
            "595",
            "51",
            "63",
            "48",
            "351",
            "1787",
            "974",
            "262",
            "40",
            "250",
            "670",
            "378",
            "239",
            "966",
            "221",
            "381",
            "248",
            "232",
            "65",
            "386",
            "677",
            "252",
            "27",
            "34",
            "94",
            "290",
            "508",
            "249",
            "597",
            "46",
            "41",
            "963",
            "689",
            "255",
            "66",
            "228",
            "690",
            "676",
            "1868",
            "216",
            "90",
            "993",
            "688",
            "256",
            "380",
            "971",
            "44",
            "598",
            "678",
            "58",
            "84",
            "1340",
            "681",
            "685",
            "967",
            "243",
            "260",
            "263"
        };

        this.prefix = prefix;
    }

    /**
     * Return full dialing code found in given telNumber. If no code found, return ""
     * @param telNumber
     * @return
     */
    public String getFullDialingCode(String telNumber) {
        String fullDialingCode = "";

        for (int iddId = 0; iddId < this.idd.length; iddId++) {
            if (telNumber.startsWith(idd[iddId])) {

                String foundIdd = idd[iddId];
                for (int prefixId = 0; prefixId < prefix.length; prefixId++) {
                    if (telNumber.startsWith(foundIdd+prefix[prefixId])) {
                        fullDialingCode = foundIdd+prefix[prefixId];
                        return fullDialingCode;
                    }
                }

            }
        }

        return "";
    }

    public String removeDialingCode(String telNumber) {
        String fullDialingCode = getFullDialingCode(telNumber);

        return telNumber.substring(fullDialingCode.length());
    }
}
