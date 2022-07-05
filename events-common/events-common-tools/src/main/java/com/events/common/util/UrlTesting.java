package com.events.common.util;

import org.junit.Assert;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlTesting {
  public static void assertUrlStatusIsOk(String host, int port, String path) throws IOException {
    HttpURLConnection connection =
        (HttpURLConnection)
            new URL(String.format("http://%s:%s/%s", host, port, path)).openConnection();
    Assert.assertEquals(200, connection.getResponseCode());
  }
}
