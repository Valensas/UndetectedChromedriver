package com.valensas.undetected.chrome.driver.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatcherUtil {
  private final Logger logger = LoggerFactory.getLogger(PatcherUtil.class);
  private final String _driverExecutablePath;

  public PatcherUtil(String _driverExecutablePath) {
    this._driverExecutablePath = _driverExecutablePath;
  }

  public void Auto() {
    if (!isBinaryPatched()) {
      patchExe();
    }
  }

  private boolean isBinaryPatched() {
    if (_driverExecutablePath == null) {
      throw new RuntimeException("driverExecutablePath is required.");
    }
    File file = new File(_driverExecutablePath);

    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(file, StandardCharsets.ISO_8859_1));

      String line;

      while ((line = br.readLine()) != null) {
        if (line.contains("undetected chromedriver")) {
          return true;
        }
      }

    } catch (Exception e) {
      logger.error("PatcherUtil exception: ", e);
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (Exception e) {
          logger.error("PatcherUtil exception: ", e);
        }
      }
    }
    return false;
  }

  private void patchExe() {
    RandomAccessFile file = null;
    try {
      file = new RandomAccessFile(_driverExecutablePath, "rw");

      byte[] buffer = new byte[1024];
      StringBuilder stringBuilder = new StringBuilder();
      long read;
      while (true) {
        read = file.read(buffer, 0, buffer.length);
        if (read == 0 || read == -1) {
          break;
        }
        stringBuilder.append(new String(buffer, 0, (int) read, StandardCharsets.ISO_8859_1));
      }
      String content = stringBuilder.toString();
      Pattern pattern = Pattern.compile("\\{window\\.cdc.*?;}");
      Matcher matcher = pattern.matcher(content);
      if (matcher.find()) {
        String group = matcher.group();
        StringBuilder newTarget =
            new StringBuilder("{console.log(\"undetected chromedriver 1337!\"}");
        int k = group.length() - newTarget.length();
        newTarget.append(" ".repeat(Math.max(0, k)));
        String newContent = content.replace(group, newTarget.toString());
        file.seek(0);
        file.write(newContent.getBytes(StandardCharsets.ISO_8859_1));
      }

    } catch (Exception e) {
      logger.error("PatcherUtil exception: ", e);
    } finally {
      if (file != null) {
        try {
          file.close();
        } catch (Exception e) {
          logger.error("PatcherUtil exception: ", e);
        }
      }
    }
  }
}
