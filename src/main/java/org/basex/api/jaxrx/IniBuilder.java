package org.basex.api.jaxrx;

import java.util.HashMap;
import java.util.Map;

/**
 * This class builds the Shiro INI file responsible for authentication
 * configuration.
 * 
 * @author Lukas Lewandowski, University of Konstanz.
 * 
 */
public class IniBuilder {

  /** Ini {@link String} representation. */
  private String mIniString;
  /** Ini custom builder. */
  private Map<String, Map<String, String>> mIniMap;
  /** Main section {@link Map}. */
  private Map<String, String> mMainMap;
  /** URLs section {@link Map}. */
  private Map<String, String> mURLMap;
  /** Main section constant. */
  private static final String MAIN = "[main]";
  /** URLs section constant. */
  private static final String URLS = "[urls]";
  /** authc constant. */
  private static final String AUTHC = "authc";
  /** realm property identifier. */
  private static final String REALM = "myRealm";

  /**
   * Default constructor;
   */
  public IniBuilder() {
    // no default ini
    mIniMap = new HashMap<String, Map<String, String>>();
    mMainMap = new HashMap<String, String>();
    mURLMap = new HashMap<String, String>();
    mIniMap.put(MAIN, mMainMap);
    mIniMap.put(URLS, mURLMap);
  }

  /**
   * This constructor reads an ini from the exported resource folder.
   * 
   * @param iniPath The ini file name.
   */
  public IniBuilder(final String iniPath) {
    mIniString = org.basex.api.jaxrx.Util.importShiroConfig(iniPath);
  }

  /**
   * This method sets the authentication method. Examples are e.g., Basic
   * Authentication or Digest Authentication.
   * 
   * @param method authentication method as {@link String}.
   */
  public void setAuthenticationMethod(final String method) {
    mMainMap.put(AUTHC, method);
  }

  /**
   * This method sets the custom realm class. This class will be initialized by
   * Apache Shiro to perform authentication.
   * @param realm The complete class name of the realm.
   */
  public void setCustomRealm(final String realm) {
    mMainMap.put(REALM, realm);
  }

  /**
   * This method sets the URLs which will be under control of the authentication
   * method.
   * @param url The URL under control.
   */
  public void setURL(final String url) {
    mURLMap.put(url, AUTHC);
  }

  /**
   * Returns the {@link String} representation of the Shiro ini file.
   * @return Shiro ini as {@link String}.
   */
  public String buildIni() {
    if(mIniString == null) {
      // ini not from ini file.
      // ini will be generated.
      StringBuilder ini = new StringBuilder();
      String nl = "\n";
      for(final Map.Entry<String, Map<String, String>> section : mIniMap.entrySet()) {
        ini.append(section.getKey() + nl);
        for(final Map.Entry<String, String> property : section.getValue().entrySet()) {
          ini.append(property.getKey() + "=" + property.getValue() + nl);
        }
      }
      return ini.toString();
    }
    return mIniString;
  }

}
