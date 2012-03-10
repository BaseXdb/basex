package org.basex.core;

import static org.basex.core.Prop.*;
import org.basex.io.IO;
import org.basex.util.Levenshtein;
import static org.basex.util.Token.token;

import org.basex.util.*;
import org.basex.util.list.StringList;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;

/**
 * This class assembles properties which are used all around the project. They
 * are initially read from and finally written to disk.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class AProp implements Iterable<String> {
  /** Properties. */
  protected final TreeMap<String, Object> props = new TreeMap<String, Object>();
  /** Property file. */
  private final String filename;

  /**
   * Constructor.
   * Reads the configuration file and initializes the project properties. The
   * file is located in the user's home directory.
   * If the {@code prop} argument is set to null, reading is omitted.
   * @param prop property file extension
   */
  protected AProp(final String prop) {
    filename = HOME + IO.BASEXSUFFIX + prop;

    try {
      for(final Field f : getClass().getFields()) {
        final Object obj = f.get(null);
        if(!(obj instanceof Object[])) continue;
        final Object[] arr = (Object[]) obj;
        props.put(arr[0].toString(), arr[1]);
      }
    } catch(final Exception ex) {
      Util.notexpected(ex);
    }
    if(prop == null) return;

    final StringList read = new StringList();
    final TokenBuilder err = new TokenBuilder();
    final File file = new File(filename);
    if(!file.exists()) {
      err.addExt("Saving properties in \"%\"..." + NL, filename);
    } else {
      BufferedReader br = null;
      try {
        br = new BufferedReader(new FileReader(file));
        for(String line; (line = br.readLine()) != null;) {
          line = line.trim();
          if(line.isEmpty() || line.charAt(0) == '#') continue;
          final int d = line.indexOf('=');
          if(d < 0) {
            err.addExt("%: \"%\" ignored. " + NL, filename, line);
            continue;
          }

          final String val = line.substring(d + 1).trim();
          String key = line.substring(0, d).trim().toUpperCase(Locale.ENGLISH);

          // extract numeric value in key
          int num = 0;
          final int ss = key.length();
          for(int s = 0; s < ss; ++s) {
            if(Character.isDigit(key.charAt(s))) {
              num = Integer.parseInt(key.substring(s));
              key = key.substring(0, s);
              break;
            }
          }
          read.add(key);

          final Object entry = props.get(key);
          if(entry == null) {
            err.addExt("%: \"%\" not found. " + NL, filename, key);
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
      } catch(final Exception ex) {
        err.addExt("% could not be parsed." + NL, filename);
        Util.debug(ex);
      } finally {
        if(br != null) try { br.close(); } catch(final IOException ex) { }
      }
    }

    // check if all mandatory files have been read
    try {
      if(err.isEmpty()) {
        boolean ok = true;
        for(final Field f : getClass().getFields()) {
          final Object obj = f.get(null);
          if(!(obj instanceof Object[])) continue;
          final String key = ((Object[]) obj)[0].toString();
          ok &= read.contains(key);
        }
        if(!ok) err.addExt("Saving properties in \"%\"..." + NL, filename);
      }
    } catch(final IllegalAccessException ex) {
      Util.notexpected(ex);
    }

    if(!err.isEmpty()) {
      Util.err(err.toString());
      write();
    }
  }

  /**
   * Parser a property string and sets the properties accordingly.
   * @param s property string
   * @throws IOException io exception
   */
  public final void properties(final String s) throws IOException {
    for(final String ser : s.trim().split(",")) {
      if(ser.isEmpty()) continue;
      final String[] sprop = ser.split("=", 2);
      final String key = sprop[0].trim().toLowerCase(Locale.ENGLISH);
      final Object obj = get(key);
      if(obj == null) {
        final String in = key.toUpperCase(Locale.ENGLISH);
        final String sim = similar(in);
        throw new BaseXException(
            sim != null ? org.basex.core.Text.UNKNOWN_OPT_SIMILAR_X :
              org.basex.core.Text.UNKNOWN_OPTION_X, in, sim);
      }
      if(obj instanceof Integer) {
        final int i = sprop.length < 2 ? 0 : Token.toInt(sprop[1]);
        if(i == Integer.MIN_VALUE)
          throw new BaseXException(org.basex.core.Text.INVALID_VALUE_X_X, key, sprop[1]);
        set(key, i);
      } else if(obj instanceof Boolean) {
        final String val = sprop.length < 2 ? org.basex.core.Text.TRUE : sprop[1];
        set(key, Util.yes(val));
      } else {
        set(key, sprop.length < 2 ? "" : sprop[1]);
      }
    }
  }

  /**
   * Writes the properties to disk.
   */
  public final synchronized void write() {
    final File file = new File(filename);

    final StringBuilder user = new StringBuilder();
    BufferedReader br = null;
    try {
      // caches options specified by the user
      if(file.exists()) {
        br = new BufferedReader(new FileReader(file));
        for(String line; (line = br.readLine()) != null;) {
          if(line.equals(PROPUSER)) break;
        }
        for(String line; (line = br.readLine()) != null;) {
          user.append(line).append(NL);
        }
      }
    } catch(final Exception ex) {
      Util.debug(ex);
    } finally {
      if(br != null) try { br.close(); } catch(final IOException e) { }
    }

    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(file));
      bw.write(PROPHEADER + NL);

      for(final Field f : getClass().getFields()) {
        final Object obj = f.get(null);
        if(!(obj instanceof Object[])) continue;
        final String key = ((Object[]) obj)[0].toString();

        final Object val = props.get(key);
        if(val instanceof String[]) {
          final String[] str = (String[]) val;
          bw.write(key + " = " + str.length + NL);
          final int is = str.length;
          for(int i = 0; i < is; ++i) {
            if(str[i] != null) bw.write(key + (i + 1) + " = " + str[i] + NL);
          }
        } else if(val instanceof int[]) {
          final int[] num = (int[]) val;
          final int ns = num.length;
          for(int i = 0; i < ns; ++i) {
            bw.write(key + i + " = " + num[i] + NL);
          }
        } else {
          bw.write(key + " = " + val + NL);
        }
      }
      bw.write(NL + PROPUSER + NL);
      bw.write(user.toString());
    } catch(final Exception ex) {
      Util.errln("% could not be written.", filename);
      Util.debug(ex);
    } finally {
      if(bw != null) try { bw.close(); } catch(final IOException e) { }
    }
  }

  /**
   * Returns the requested object, or {@code null}.
   * @param key key to be found
   * @return value
   */
  public final Object get(final String key) {
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
    return (Integer) get(key, Integer.class);
  }

  /**
   * Returns the requested boolean.
   * @param key key to be found
   * @return value
   */
  public final boolean is(final Object[] key) {
    return (Boolean) get(key, Boolean.class);
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
    setObject(key, val);
  }

  /**
   * Sets the specified integer for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final void set(final Object[] key, final int val) {
    setObject(key, val);
  }

  /**
   * Sets the specified boolean for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final void set(final Object[] key, final boolean val) {
    setObject(key, val);
  }

  /**
   * Sets the specified string array for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final void set(final Object[] key, final String[] val) {
    setObject(key, val);
  }

  /**
   * Sets the specified integer array for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final void set(final Object[] key, final int[] val) {
    setObject(key, val);
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
   * Checks if the specified property has changed.
   * @param key key
   * @param val new value
   * @return result of check
   */
  public final boolean sameAs(final Object[] key, final Object val) {
    return props.get(key[0].toString()).equals(val);
  }

  /**
   * Returns a key similar to the specified string, or {@code null}.
   * @param key key to be found
   * @return similar key
   */
  public final String similar(final String key) {
    final byte[] name = token(key);
    final Levenshtein ls = new Levenshtein();
    for(final String prop : props.keySet()) {
      if(ls.similar(name, token(prop), 0)) return prop;
    }
    return null;
  }

  /**
   * Retrieves the specified value. Throws an error if value cannot be read.
   * @param key key
   * @param c expected type
   * @return result
   */
  private Object get(final Object[] key, final Class<?> c) {
    final Object entry = props.get(key[0].toString());
    if(entry == null) Util.notexpected("Property " + key[0] + " not defined.");

    final Class<?> cc = entry.getClass();
    if(c != cc) Util.notexpected("Property '" + key[0] + "' is a " + Util.name(cc));
    return entry;
  }

  /**
   * Sets the specified value.
   *
   * @param key key
   * @param val value
   */
  private void setObject(final Object[] key, final Object val) {
    props.put(key[0].toString(), val);
    finish();
  }

  /**
   * Sets static properties.
   */
  void finish() {
    // nothing to do; if necessary, is overwritten.
  }

  @Override
  public String toString() {
    return Util.name(this) + props;
  }

  @Override
  public final Iterator<String> iterator() {
    return props.keySet().iterator();
  }
}
