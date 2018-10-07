package cz.tondakozak.volajiciinfo;

import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class PeopleDBTestInstrumented {
    static Context appContext;
    static PeopleDB peopleDB;


    @After
    public void after() {
        peopleDB.deleteData();
        peopleDB.close();
    }

    @Before
    public  void before() {
        appContext = InstrumentationRegistry.getTargetContext();
        peopleDB = new PeopleDB(appContext);
    }


    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("cz.tondakozak.volajiciinfo", appContext.getPackageName());
    }


    @Test
    public void constructorTest() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        PeopleDB peopleDB = new PeopleDB(appContext);
    }

    @Test
    public void insertTest() {
        peopleDB.insertOrUpdateMan("+420774423107", "[\"tonda\", \"Kozák\", \"orderInfo\", \"infoInfo]\"");
    }

    @Test
    public void getManTest() {
        peopleDB.insertOrUpdateMan("+420774423107", "[\"tonda\", \"Kozák\", \"orderInfo\", \"infoInfo]\"");
        Cursor man = peopleDB.getMan("+420774423107");

        assertEquals(1, man.getCount());
    }


    @Test
    public void getManTestWithoutCode() {
        peopleDB.insertOrUpdateMan("774423107", "[\"tonda\", \"Kozák\", \"orderInfo\", \"infoInfo]\"");
        Cursor man = peopleDB.getMan("+420774423107");

        assertEquals(1, man.getCount());
    }

    @Test
    public void getMan2InsertsTest() {
        peopleDB.deleteData();
        peopleDB.insertOrUpdateMan("+420774423107", "[\"Jan\", \"Milíč\", \"orderInfo\", \"infoInfo]\"");
        peopleDB.insertOrUpdateMan("+420774423155", "[\"tonda\", \"Kozák\", \"orderInfo\", \"infoInfo]\"");
        Cursor man = peopleDB.getMan("+420774423107");

        assertEquals(1, man.getCount());

        man.moveToFirst();
        assertEquals("[\"Jan\", \"Milíč\", \"orderInfo\", \"infoInfo]\"", man.getString(man.getColumnIndex("info")));
        assertEquals(2, peopleDB.getNumberOfPeople());
    }

    @Test
    public void getNoManTest() {
        peopleDB.insertOrUpdateMan("+420774423107", "[\"tonda\", \"Kozák\", \"orderInfo\", \"infoInfo]\"");
        Cursor man = peopleDB.getMan("+420774423107-t");

        assertEquals(0, man.getCount());
    }


    public void updateDBValuesTest() {
        String jsonString = "[" +
                "      {\"tel\": \"+420787545121\", \"lines\":[\"tonda\", \"Kozák\", \"<b>objednávka</b>\"]}," +
                "      {\"tel\": \"+4207875451251\", \"lines\": [\"<b>Lukáš</b>\", \"surname\",\"Novák\", \"order\",\"Informace o objednávce\"]}," +
                "      {\"tel\": \"+420658231428\", \"lines\": [\"Jiří\", \"Kličko\", \"order\",\"Informace o objednávce bla bla\"]}" +
                "]";

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            peopleDB.updateDBValues(jsonArray);

            Cursor man = peopleDB.getMan("+420787545121");

            assertEquals(1, man.getCount());

            Cursor man2 = peopleDB.getMan("+4207875451251");

            assertEquals(1, man2.getCount());

            Cursor man3 = peopleDB.getMan("+420658231428");

            assertEquals(1, man3.getCount());

        } catch (JSONException e) {
            fail();
        }
    }


    public void updateDBValuesFormatTest() {
        String jsonString = "[" +
                "      {\"tel\": \"+420787545121\", \"lines\":[\"tonda\", \"Kozák\", \"<b>objednávka</b>\"]}," +
                "      {\"tel\": \"+420787545121\", \"lines\": [\"<b>Lukáš</b>\", \"surname\",\"Novák\", \"order\",\"Informace o objednávce\"]}," +
                "      {\"tel\": \"+420658231428\", \"lines\": [\"Jiří\", \"Kličko\", \"order\",\"Informace o objednávce bla bla\"]}," +
                "      {\"tel\": \"+420774423107\", \"lines\": [\"<u>Antonín</u>\",\"<h1 style'color: red;'>Kozák</h1>\", \"order\",\"Lorem Ipsum je demonstrativní výplňový text používaný v <b>tiskařském a knihařském </b>průmyslu. \"]}," +
                "      {\"tel\": \"+420658231425\", \"lines\": [\"<u>Antonín</u>\",\"<h1 style'color: red;'>Kozák</h1>\", \"order\",\"Lorem Ipsum je demonstrativní výplňový text používaný v <b>tiskařském a knihařském </b>průmyslu. \", \"Lorem Ipsum je demonstrativní výplňový text používaný v <b>tiskařském a knihařském </b>průmyslu. \",\"Lorem Ipsum je demonstrativní výplňový text používaný v <b>tiskařském a knihařském </b>průmyslu. \",\"Lorem Ipsum je demonstrativní výplňový text používaný v <b>tiskařském a knihařském </b>průmyslu. \",\"Lorem Ipsum je demonstrativní výplňový text používaný v <b>tiskařském a knihařském </b>průmyslu. \",\"Lorem Ipsum je demonstrativní výplňový text používaný v <b>tiskařském a knihařském </b>průmyslu. \",\"Lorem Ipsum je demonstrativní výplňový text používaný v <b>tiskařském a knihařském </b>průmyslu. \",\"Lorem Ipsum je demonstrativní výplňový text používaný v <b>tiskařském a knihařském </b>průmyslu. \",\"Lorem Ipsum je demonstrativní výplňový text používaný v <b>tiskařském a knihařském </b>průmyslu. \",\"Lorem Ipsum je demonstrativní výplňový text používaný v <b>tiskařském a knihařském </b>průmyslu. \"]}]";

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            peopleDB.updateDBValues(jsonArray);

            Cursor man = peopleDB.getMan("+420787545121");

            assertEquals(1, man.getCount());

            Cursor man2 = peopleDB.getMan("+420658231425");

            assertEquals(1, man2.getCount());

            Cursor man3 = peopleDB.getMan("+420774423107");

            assertEquals(1, man3.getCount());

        } catch (JSONException e) {
            fail();
        }
    }


    public void getFullDialingCodeTest() {
        String tNumber = "+420774423107";
        String expectedDialingCode = "+420";
        String fullDialingCode = peopleDB.getFullDialingCode(tNumber);

        assertEquals(expectedDialingCode, fullDialingCode);
    }


    public void getFullDialingCodeTest1() {
        String tNumber = "01144774423107";
        String expectedDialingCode = "01144";
        String fullDialingCode = peopleDB.getFullDialingCode(tNumber);

        assertEquals(expectedDialingCode, fullDialingCode);
    }


    public void getFullDialingCodeTest2() {
        String tNumber = "00421774423107";
        String expectedDialingCode = "00421";
        String fullDialingCode = peopleDB.getFullDialingCode(tNumber);

        assertEquals(expectedDialingCode, fullDialingCode);
    }


    public void getFullDialingCodeTest3() {
        String tNumber = "466423107";
        String expectedDialingCode = "";
        String fullDialingCode = peopleDB.getFullDialingCode(tNumber);

        assertEquals(expectedDialingCode, fullDialingCode);
    }


    public void getNumberWithoutCodeTest() {
        String tNumber = "+420466423107";
        String expectedNumber = "466423107";
        String number = peopleDB.removeDialingCode(tNumber);

        assertEquals(expectedNumber, number);
    }

    public void getNumberWithoutCodeTest1() {
        String tNumber = "00420466423107";
        String expectedNumber = "466423107";
        String number = peopleDB.removeDialingCode(tNumber);

        assertEquals(expectedNumber, number);
    }

    public void getNumberWithoutCodeTest2() {
        String tNumber = "466423107";
        String expectedNumber = "466423107";
        String number = peopleDB.removeDialingCode(tNumber);

        assertEquals(expectedNumber, number);
    }

    public void getManWithoutCode() {
        String jsonString = "[" +
                "      {\"tel\": \"+420787545121\", \"lines\":[\"tonda\", \"Kozák\", \"<b>objednávka</b>\"]}," +
                "      {\"tel\": \"+420787545111\", \"lines\": [\"<b>Lukáš</b>\", \"surname\",\"Novák\", \"order\",\"Informace o objednávce\"]}," +
                "      {\"tel\": \"+420658231428\", \"lines\": [\"Jiří\", \"Kličko\", \"order\",\"Informace o objednávce bla bla\"]}," +
                "      {\"tel\": \"+421658231428\", \"lines\": [\"<u>Antonín</u>\",\"<h1 style'color: red;'>Kozák</h1>\", \"order\",\"Lorem Ipsum je demonstrativní výplňový text používaný v <b>tiskařském a knihařském </b>průmyslu. \"]}," +
                "      {\"tel\": \"+420658231425\", \"lines\": [\"<u>Antonín</u>\",\"<h1 style'color: red;'>Kozák</h1>\", \"order\",\"Lorem Ipsum je demonstrativní výplňový text používaný v <b>tiskařském a knihařském </b>průmyslu. \"]}]";

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            peopleDB.updateDBValues(jsonArray);

            Cursor man = peopleDB.getManWithoutCode("787545121");

            assertEquals(1, man.getCount());

            Cursor man2 = peopleDB.getManWithoutCode("+420658231428");

            assertEquals(2, man2.getCount());

            Cursor man3 = peopleDB.getManWithoutCode("658231428");

            assertEquals(2, man3.getCount());

        } catch (JSONException e) {
            fail();
        }
    }
}
