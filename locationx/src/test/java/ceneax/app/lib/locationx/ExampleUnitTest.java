package ceneax.app.lib.locationx;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Arrays;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        System.out.println(Arrays.toString(LocationUtil.wgs84ToGcj02(1.2894, 103.8499)));
    }
}