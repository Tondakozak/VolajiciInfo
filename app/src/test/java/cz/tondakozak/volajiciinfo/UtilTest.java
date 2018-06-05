package cz.tondakozak.volajiciinfo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UtilTest {
    @Test
    public void addHTTPS() {
        String url = "www.tondakozak.cz";
        String expected = "https://www.tondakozak.cz";
        String result = Util.addHTTPsProtocol(url);
        assertEquals(expected, result);
    }


    @Test
    public void addHTTPSnoAdd() {
        String url = "https://www.tondakozak.cz";
        String expected = "https://www.tondakozak.cz";
        String result = Util.addHTTPsProtocol(url);
        assertEquals(expected, result);
    }

    @Test
    public void addHTTPSnoAddhttpExists() {
        String url = "http://www.tondakozak.cz";
        String expected = "http://www.tondakozak.cz";
        String result = Util.addHTTPsProtocol(url);
        assertEquals(expected, result);
    }

}