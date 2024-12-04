package com.valensas.undetected.chrome.driver;

import com.valensas.undetected.chrome.driver.util.DriverUtil;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ChromeDriverTests {
  @Test
  public void chromeDriverTests() {
    ChromeOptions chrome_options;
    chrome_options =
        new ChromeOptions().addArguments("--no-sandbox").addArguments("--headless=new");
    ChromeDriver chromeDriver1 = DriverUtil.undetectedChromeDriver("--headless=new");
    ChromeDriver chromeDriver2 = new ChromeDriverBuilder().build(chrome_options, null);
    chromeDriver1.get("https://www.gstatic.com/generate_204");
    chromeDriver2.get("https://www.gstatic.com/generate_204");
    chromeDriver1.quit();
    chromeDriver2.quit();
  }
}
