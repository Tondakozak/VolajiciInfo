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
        peopleDB.insertOrUpdateMan("+420774423107", "tonda", "Kozák", "orderInfo", "infoInfo");
    }

    @Test
    public void getManTest() {
        peopleDB.insertOrUpdateMan("+420774423107", "tonda", "Kozák", "orderInfo", "infoInfo");
        Cursor man = peopleDB.getMan("+420774423107");

        assertEquals(1, man.getCount());
    }

    @Test
    public void getMan2InsertsTest() {
        peopleDB.insertOrUpdateMan("+4207744231078", "tonda", "Kozák", "orderInfo", "infoInfo");
        peopleDB.insertOrUpdateMan("+4207744231078", "tonda", "KozákTonda", "orderInfo", "infoInfo");
        Cursor man = peopleDB.getMan("+4207744231078");

        assertEquals(1, man.getCount());

        man.moveToFirst();
        assertEquals("KozákTonda", man.getString(man.getColumnIndex("surname")));
        assertEquals(1, peopleDB.getNumberOfPeople());
    }

    @Test
    public void getNoManTest() {
        //peopleDB.insertOrUpdateMan("+420774423107", "tonda", "Kozák", "orderInfo", "infoInfo");
        Cursor man = peopleDB.getMan("+420774423107-t");

        assertEquals(0, man.getCount());
    }


    public void updateDBValuesTest() {
        String jsonString = "[\n" +
                "  {\"tel\": \"sdssdfasdfdsd\", \"firstname\":\"Lukáš\", \"surname\":\"1Kozák\", \"order\":\"malawi\"},\n" +
                "  \n" +
                "  {\"tel\": \"sdsd22sd\", \"firstname\":\"gdftonda\", \"surname\":\"Kozák\", \"order\":\"malawi\"},\n" +
                "  \n" +
                "  {\"tel\": \"6589sdsdsd\", \"firstname\":\"ddtonda\", \"surname\":\"2Kozák\", \"order\":\"2malawi\"}]";
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            peopleDB.updateDBValues(jsonArray);

            Cursor man = peopleDB.getMan("sdssdfasdfdsd");

            assertEquals(1, man.getCount());

            Cursor man2 = peopleDB.getMan("sdsd22sd");

            assertEquals(1, man2.getCount());

            Cursor man3 = peopleDB.getMan("6589sdsdsd");

            assertEquals(1, man3.getCount());

        } catch (JSONException e) {
            fail();
        }
    }
}
