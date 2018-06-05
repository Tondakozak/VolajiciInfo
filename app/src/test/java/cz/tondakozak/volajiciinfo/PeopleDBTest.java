package cz.tondakozak.volajiciinfo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class PeopleDBTest {


    @Test
    public void cleanNumber() {
        String number = "+42045454545454";
        String expected = "+42045454545454";
        String result = PeopleDB.cleanNumberFormat(number);

        assertEquals(expected, result);
    }


    @Test
    public void cleanNumber2() {
        String number = "42045454545454";
        String expected = "42045454545454";
        String result = PeopleDB.cleanNumberFormat(number);

        assertEquals(expected, result);
    }


    @Test
    public void cleanNumber3() {
        String number = "+42045       454545      45                     4";
        String expected = "+42045454545454";
        String result = PeopleDB.cleanNumberFormat(number);

        assertEquals(expected, result);
    }



    @Test
    public void cleanNumber4() {
        String number = "+4204 54545 454 54";
        String expected = "+42045454545454";
        String result = PeopleDB.cleanNumberFormat(number);

        assertEquals(expected, result);
    }





}