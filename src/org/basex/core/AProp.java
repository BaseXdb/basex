package org.basex.core;

import static org.basex.core.Prop.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import org.basex.io.IO;
import org.basex.util.TokenBuilder;

/**
 * This class assembles properties which are used all around the project. They
 * are initially read from and finally written to disk.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class AProp {
  /** Properties. */
  private final HashMap<String, Object> props = new HashMap<String, Object>();
  /** Property file. */
  private final String filename;

  /**
   * Constructor.
   * Reads the configuration file and initializes the project properties. The
   * file is located in the user's home directory.
   * If the <code>prop</code> argument is set to null, reading is omitted.
   * @param prop property file extension
   */
  public AProp(final String prop) {
    filename = HOME + IO.BASEXSUFFIX + prop;

    try {
      for(final Field f : getClass().getFields()) {
        final Object obj = f.get(null);
        if(!(obj instanceof Object[])) continue;
        final Object[] arr = (Object[]) obj;
        props.put(arr[0].toString(), arr[1]);
      }
    } catch(final Exception ex) {
      Main.notexpected(ex);
    }
    if(prop == null) return;

    final File file = new File(filename);
    if(!file.exists()) return;

    final TokenBuilder err = new TokenBuilder();
    try {
      final BufferedReader br = new BufferedReader(new FileReader(file));
      String line = null;

      while((line = br.readLine()) != null) {
        line = line.trim();
        if(line.isEmpty() || line.charAt(0) == '#') continue;
        final int d = line.indexOf('=');
        if(d < 0) {
          err.add("%: \"%\" ignored. " + NL, filename, line);
          continue;
        }

        final String val = line.substring(d + 1).trim();
        String key = line.substring(0, d).trim().toUpperCase();
        // extract numeric value in key
        int num = 0;
        for(int s = 0; s < key.length(); s++) {
          if(Character.isDigit(key.charAt(s))) {
            num = Integer.parseInt(key.substring(s));
            key = key.substring(0, s);
            break;
          }
        }

        final Object entry = props.get(key);
        if(entry == null) {
          err.add("%: \"%\" not found. " + NL, filename, key);
        } else if(entry instanceof String) {
          props.put(key, val);
        } else if(entry instanceof Integer) {
          props.put(key, Integer.parseInt(val));
        } else if(entry instanceof Boolean) {
          props.put(key, Boolean.parseBoolean(val));
        } else if(entry instanceof String[]) {
          if(num == 0) {
            props.put(key, new String[Integer.parseInt(val)]);
          } else {
            ((String[]) entry)[num - 1] = val;
          }
        } else if(entry instanceof int[]) {
          ((int[]) entry)[num] = Integer.parseInt(val);
        }
      }
      br.close();
    } catch(final Exception ex) {
      err.add("% could not be parsed." + NL, filename);
      Main.debug(ex);
    }
    if(err.size() != 0) {
      Main.err(err.toString());
      write();
    }
  }

  /**
   * Writes the properties to disk.
   */
  public final synchronized void write() {
    final File file = new File(filename);

    try {
      // caches options specified by the user
      final StringBuilder user = new StringBuilder();
      if(file.exists()) {
        final BufferedReader br = new BufferedReader(new FileReader(file));
        String line = null;
        while((line = br.readLine()) != null)
          if(line.equals(PROPUSER)) break;
        while((line = br.readLine()) != null) {
          user.append(line);
          user.append(NL);
        }
        br.close();
      }

      final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
      bw.write(PROPHEADER + NL);

      for(final Field f : getClass().getFields()) {
        final Object obj = f.get(null);
        if(!(obj instanceof Object[])) continue;
        final String key = ((Object[]) obj)[0].toString();
        if(key.equals(Prop.SKIP[0])) break;

        final Object val = props.get(key);
        if(val instanceof String[]) {
          final String[] str = (String[]) val;
          bw.write(key + " = " + str.length + NL);
          for(int i = 0; i < str.length; i++) {
            if(str[i] != null) bw.write(key + (i + 1) + " = " + str[i] + NL);
          }
        } else if(val instanceof int[]) {
          final int[] num = (int[]) val;
          for(int i = 0; i < num.length; i++) {
            bw.write(key + i + " = " + num[i] + NL);
          }
        } else {
          bw.write(key + " = " + val + NL);
        }
      }
      bw.write(NL + PROPUSER + NL);
      bw.write(user.toString());
      bw.close();
    } catch(final Exception ex) {
      Main.errln("% could not be written.", filename);
      Main.debug(ex);
    }
  }

  /**
   * Returns the requested object.
   * @param key key to be found
   * @return value
   */
  public final Object object(final String key) {
    return props.get(key);
  }

  /**
   * Returns the requested string.
   * @param key key to be found
   * @return value
   */
  public final String get(final Object[] key) {
    return get(key, String.class).toString();
  }

  /**
   * Returns the requested integer.
   * @param key key to be found
   * @return value
   */
  public final int num(final Object[] key) {
    return ((Integer) get(key, Integer.class)).intValue();
  }

  /**
   * Returns the requested boolean.
   * @param key key to be found
   * @return value
   */
  public final boolean is(final Object[] key) {
    return ((Boolean) get(key, Boolean.class)).booleanValue();
  }

  /**
   * Returns the requested string array.
   * @param key key to be found
   * @return value
   */
  public final String[] strings(final Object[] key) {
    return (String[]) get(key, String[].class);
  }

  /**
   * Returns the requested integer array.
   * @param key key to be found
   * @return value
   */
  public final int[] nums(final Object[] key) {
    return (int[]) get(key, int[].class);
  }

  /**
   * Sets the specified value for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final void set(final Object[] key, final String val) {
    set(key, val, String.class);
  }

  /**
   * Sets the specified integer for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final void set(final Object[] key, final int val) {
    set(key, val, Integer.class);
  }

  /**
   * Sets the specified boolean for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final void set(final Object[] key, final boolean val) {
    set(key, val, Boolean.class);
  }

  /**
   * Sets the specified string array for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final void set(final Object[] key, final String[] val) {
    set(key, val, String[].class);
  }

  /**
   * Sets the specified integer array for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final void set(final Object[] key, final int[] val) {
    set(key, val, int[].class);
  }

  /**
   * Sets the specified value for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final void set(final String key, final Object val) {
    props.put(key, val);
    finish();
  }

  /**
   * Inverts a boolean property.
   * @param key key
   * @return new value
   */
  public final boolean invert(final Object[] key) {
    final boolean val = !is(key);
    set(key, val);
    return val;
  }

  /**
   * Retrieves the specified value. Throws an error if value cannot be read.
   * @param key key
   * @param c expected type
   * @return result
   */
  private Object get(final Object[] key, final Class<?> c) {
    final Object entry = props.get(key[0].toString());
    if(entry == null) Main.notexpected("Property " + key[0] + " not defined.");
    final Class<?> cc = entry.getClass();
    if(c != cc) Main.notexpected("Property '" + key[0] + "' is a " +
        cc.getSimpleName());
    return entry;
  }

  /**
   * Sets the specified value.
   * @param key key
   * @param c expected type
   * @param val value
   */
  private void set(final Object[] key, final Object val, final Class<?> c) {
    get(key, c);
    props.put(key[0].toString(), val);
    finish();
  }

  /**
   * Sets static properties.
   */
  protected void finish() { }
}
