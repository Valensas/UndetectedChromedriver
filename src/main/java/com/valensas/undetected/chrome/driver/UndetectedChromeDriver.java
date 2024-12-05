package com.valensas.undetected.chrome.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class UndetectedChromeDriver extends ChromeDriver {

  private final boolean _headless;

  private final Process _browser;

  private final boolean _keepUserDataDir;

  private final String _userDataDir;

  private final ChromeOptions chromeOptions;

  public void get(String url) {
    if (_headless) {
      _headless();
    }
    _cdcProps();
    super.get(url);
  }

  public void quit() {
    super.quit();
    // kill process
    _browser.destroyForcibly();
    // delete temp user dir
    if (_keepUserDataDir) {
      for (int i = 0; i < 5; i++) {
        try {
          File file = new File(_userDataDir);
          if (!file.exists()) {
            break;
          }
          boolean f = file.delete();
          if (f) {
            break;
          }
        } catch (Exception e) {
          try {
            Thread.sleep(300);
          } catch (Exception ignored) {
          }
        }
      }
    }
  }

  public UndetectedChromeDriver(
      ChromeOptions chromeOptions,
      boolean headless,
      boolean keepUserDataDir,
      String userDataDir,
      Process browser) {

    super(chromeOptions);
    this.chromeOptions = chromeOptions;
    _browser = browser;
    _headless = headless;
    _keepUserDataDir = keepUserDataDir;
    _userDataDir = userDataDir;
  }

  /** configure headless */
  private void _headless() {
    // set navigator.webdriver
    Object f = this.executeScript("return navigator.webdriver");
    if (f == null) {
      return;
    }

    Map<String, Object> params1 = getStringObjectMap();

    this.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", params1);

    // set ua
    Map<String, Object> params2 = new HashMap<>();
    params2.put(
        "userAgent",
        ((String) Objects.requireNonNull(this.executeScript("return navigator.userAgent")))
            .replace("Headless", ""));
    this.executeCdpCommand("Network.setUserAgentOverride", params2);

    Map<String, Object> params3 = new HashMap<>();
    params3.put("source", "Object.defineProperty(navigator, 'maxTouchPoints', {get: () => 1});");
    this.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", params3);

    Map<String, Object> params4 = new HashMap<>();
    params4.put(
        "source",
        """
                Object.defineProperty(navigator.connection, 'rtt', {get: () => 100});
                // https://github.com/microlinkhq/browserless/blob/master/packages/goto/src/evasions/chrome-runtime.js
                window.chrome = {
                        app: {
                            isInstalled: false,
                            InstallState: {
                                DISABLED: 'disabled',
                                INSTALLED: 'installed',
                                NOT_INSTALLED: 'not_installed'
                            },
                            RunningState: {
                                CANNOT_RUN: 'cannot_run',
                                READY_TO_RUN: 'ready_to_run',
                                RUNNING: 'running'
                            }
                        },
                        runtime: {
                            OnInstalledReason: {
                                CHROME_UPDATE: 'chrome_update',
                                INSTALL: 'install',
                                SHARED_MODULE_UPDATE: 'shared_module_update',
                                UPDATE: 'update'
                            },
                            OnRestartRequiredReason: {
                                APP_UPDATE: 'app_update',
                                OS_UPDATE: 'os_update',
                                PERIODIC: 'periodic'
                            },
                            PlatformArch: {
                                ARM: 'arm',
                                ARM64: 'arm64',
                                MIPS: 'mips',
                                MIPS64: 'mips64',
                                X86_32: 'x86-32',
                                X86_64: 'x86-64'
                            },
                            PlatformNaclArch: {
                                ARM: 'arm',
                                MIPS: 'mips',
                                MIPS64: 'mips64',
                                X86_32: 'x86-32',
                                X86_64: 'x86-64'
                            },
                            PlatformOs: {
                                ANDROID: 'android',
                                CROS: 'cros',
                                LINUX: 'linux',
                                MAC: 'mac',
                                OPENBSD: 'openbsd',
                                WIN: 'win'
                            },
                            RequestUpdateCheckStatus: {
                                NO_UPDATE: 'no_update',
                                THROTTLED: 'throttled',
                                UPDATE_AVAILABLE: 'update_available'
                            }
                        }
                }

                // https://github.com/microlinkhq/browserless/blob/master/packages/goto/src/evasions/navigator-permissions.js
                if (!window.Notification) {
                        window.Notification = {
                            permission: 'denied'
                        }
                }

                const originalQuery = window.navigator.permissions.query
                window.navigator.permissions.__proto__.query = parameters =>
                        parameters.name === 'notifications'
                            ? Promise.resolve({ state: window.Notification.permission })
                            : originalQuery(parameters)
                       \s
                const oldCall = Function.prototype.call\s
                function call() {
                        return oldCall.apply(this, arguments)
                }
                Function.prototype.call = call

                const nativeToStringFunctionString = Error.toString().replace(/Error/g, 'toString')
                const oldToString = Function.prototype.toString

                function functionToString() {
                        if (this === window.navigator.permissions.query) {
                            return 'function query() { [native code] }'
                        }
                        if (this === functionToString) {
                            return nativeToStringFunctionString
                        }
                        return oldCall.call(oldToString, this)
                }
                // eslint-disable-next-line
                Function.prototype.toString = functionToString""");
    this.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", params4);
  }

  @NotNull
  private static Map<String, Object> getStringObjectMap() {
    Map<String, Object> params1 = new HashMap<>();
    params1.put(
        "source",
        """
                Object.defineProperty(window, 'navigator', {
                    value: new Proxy(navigator, {
                        has: (target, key) => (key === 'webdriver' ? false : key in target),
                        get: (target, key) =>
                            key === 'webdriver' ?
                            false :
                            typeof target[key] === 'function' ?
                            target[key].bind(target) :
                            target[key]
                        })
                });""");
    return params1;
  }

  /** remove cdc */
  private void _cdcProps() {
    //noinspection unchecked
    List<String> f =
        (List<String>)
            this.executeScript(
                """
                let objectToInspect = window,
                    result = [];
                while(objectToInspect !== null)
                { result = result.concat(Object.getOwnPropertyNames(objectToInspect));
                  objectToInspect = Object.getPrototypeOf(objectToInspect); }
                return result.filter(i => i.match(/.+_.+_(Array|Promise|Symbol)/ig))""");

    if (f != null && !f.isEmpty()) {
      Map<String, Object> param = getObjectMap();
      this.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", param);
    }
  }

  @NotNull
  private static Map<String, Object> getObjectMap() {
    Map<String, Object> param = new HashMap<>();
    param.put(
        "source",
        """
                let objectToInspect = window,
                    result = [];
                while(objectToInspect !== null)
                { result = result.concat(Object.getOwnPropertyNames(objectToInspect));
                  objectToInspect = Object.getPrototypeOf(objectToInspect); }
                result.forEach(p => p.match(/.+_.+_(Array|Promise|Symbol)/ig)
                                    &&delete window[p]&&console.log('removed',p))""");
    return param;
  }

  /** set stealth */
  UndetectedChromeDriver stealth() {
    StringBuilder stringBuffer = new StringBuilder();
    BufferedReader bufferedReader;
    try {
      InputStream in = this.getClass().getResourceAsStream("/static/js/stealth.min.js");
      assert in != null;
      bufferedReader = new BufferedReader(new InputStreamReader(in));
      String str;
      while ((str = bufferedReader.readLine()) != null) {
        stringBuffer.append(str);
        stringBuffer.append("\n");
      }
      in.close();
      bufferedReader.close();
    } catch (Exception ignored) {
    }
    Map<String, Object> params = new HashMap<>();
    params.put("source", stringBuffer.toString());
    this.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", params);
    return this;
  }

  @Override
  public void startSession(Capabilities capabilities) {
    if (capabilities == null) {
      capabilities = this.chromeOptions;
    }
    super.startSession(capabilities);
  }
}
