package com.valensas.undetected.chrome.driver.util;

import com.valensas.undetected.chrome.driver.ChromeDriverBuilder;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DriverUtil {
  static Logger logger = LoggerFactory.getLogger(WebDriverManager.class);

  public static ChromeDriver undetectedChromeDriver(String... extraArguments) {
    WebDriverManager wdm = WebDriverManager.chromedriver().timeout(600).ttl(600).ttlBrowsers(600);

    OSArchitecture.setWdmArchitecture(wdm, logger).setup();

    ChromeOptions options =
        new ChromeOptions()
            .addArguments("--window-size=1920,1080")
            .addArguments("--disable-gpu")
            .addArguments("--incognito")
            .addArguments("--no-sandbox")
            .addArguments("--disable-dev-shm-usage")
            .addArguments("--disable-gpu")
            .addArguments("--disable-software-compositing")
            .addArguments("--disable-accelerated-2d-canvas")
            .addArguments("--disable-accelerated-jpeg-decoding")
            .addArguments("--disable-accelerated-video-decode")
            .addArguments("--disable-accelerated-mjpeg-decode")
            .addArguments("--disable-blink-features=AutomationControlled");
    for (String arg : extraArguments) {
      options = options.addArguments(arg);
    }
    String driverPath = wdm.getDownloadedDriverPath();

    return new ChromeDriverBuilder().build(options, driverPath);
  }
}
