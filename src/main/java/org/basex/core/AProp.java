package org.basex.core;

import static org.basex.core.Prop.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;

import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

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
  private IOFile file;

  /**
   * Constructor, initializing the default options.
   */
  protected AProp() {
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
    system();
  }

  /**
   * Writes the properties to disk.
   */
  public final synchronized void write() {
    final StringBuilder user = new StringBuilder();
    BufferedReader br = null;
    try {
      // caches options specified by the user
      if(file.exists()) {
        br = new BufferedReader(new FileReader(file.file()));
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
      bw = new BufferedWriter(new FileWriter(file.file()));
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
      Util.errln("% could not be written.", file);
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
  public final synchronized Object get(final String key) {
    return props.get(key);
  }

  /**
   * Returns the requested string.
   * @param key key to be found
   * @return value
   */
  public final synchronized String get(final Object[] key) {
    return get(key, String.class).toString();
  }

  /**
   * Returns the requested integer.
   * @param key key to be found
   * @return value
   */
  public final synchronized int num(final Object[] key) {
    return (Integer) get(key, Integer.class);
  }

  /**
   * Returns the requested boolean.
   * @param key key to be found
   * @return value
   */
  public final synchronized boolean is(final Object[] key) {
    return (Boolean) get(key, Boolean.class);
  }

  /**
   * Returns the requested string array.
   * @param key key to be found
   * @return value
   */
  public final synchronized String[] strings(final Object[] key) {
    return (String[]) get(key, String[].class);
  }

  /**
   * Returns the requested integer array.
   * @param key key to be found
   * @return value
   */
  public final synchronized int[] nums(final Object[] key) {
    return (int[]) get(key, int[].class);
  }

  /**
   * Assigns the specified value for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final synchronized void set(final Object[] key, final String val) {
    setObject(key[0].toString(), val);
  }

  /**
   * Assigns the specified integer for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final synchronized void set(final Object[] key, final int val) {
    setObject(key[0].toString(), val);
  }

  /**
   * Assigns the specified boolean for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final synchronized void set(final Object[] key, final boolean val) {
    setObject(key[0].toString(), val);
  }

  /**
   * Assigns the specified string array for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final synchronized void set(final Object[] key, final String[] val) {
    setObject(key[0].toString(), val);
  }

  /**
   * Assigns the specified integer array for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final synchronized void set(final Object[] key, final int[] val) {
    setObject(key[0].toString(), val);
  }

  /**
   * Assigns the specified object for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final synchronized void setObject(final String key, final Object val) {
    props.put(key, val);
    finish();
  }

  /**
   * Sets the specified value after casting it to the correct type.
   * @param key key
   * @param val value
   * @return final value, or {@code null} if the key has not been found
   */
  public final synchronized String set(final String key, final String val) {
    final Object type = get(key);
    if(type == null) return null;

    String v = val;
    if(type instanceof Boolean) {
      final boolean b = val == null || val.isEmpty() ? !((Boolean) type) : Util.yes(val);
      setObject(key, b);
      v = Util.flag(b);
    } else if(type instanceof Integer) {
      setObject(key, Integer.parseInt(val));
      v = String.valueOf(get(key));
    } else if(type instanceof String) {
      setObject(key, val);
    } else {
      Util.notexpected("Unknown property type: " + type.getClass().getSimpleName());
    }
    return v;
  }

  /**
   * Returns an error string for an unknown key.
   * @param key key
   * @return error string
   */
  public final synchronized String unknown(final String key) {
    final String sim = similar(key);
    return Util.info(sim != null ? Text.UNKNOWN_OPT_SIMILAR_X_X :
      Text.UNKNOWN_OPTION_X, key, sim);
  }

  /**
   * Inverts a boolean property.
   * @param key key
   * @return new value
   */
  public final synchronized boolean invert(final Object[] key) {
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
  public final synchronized boolean sameAs(final Object[] key, final Object val) {
    return props.get(key[0].toString()).equals(val);
  }

  /**
   * Returns a key similar to the specified string, or {@code null}.
   * @param key key to be found
   * @return similar key
   */
  public final synchronized String similar(final String key) {
    final byte[] name = token(key);
    final Levenshtein ls = new Levenshtein();
    for(final String prop : props.keySet()) {
      if(ls.similar(name, token(prop), 0)) return prop;
    }
    return null;
  }

  @Override
  public final synchronized Iterator<String> iterator() {
    return props.keySet().iterator();
  }

  @Override
  public final synchronized String toString() {
    final TokenBuilder tb = new TokenBuilder();
    for(final Entry<String, Object> e : props.entrySet()) {
      if(!tb.isEmpty()) tb.add(',');
      tb.add(e.getKey()).add('=').addExt(e.getValue());
    }
    return tb.toString();
  }

  // PROTECTED METHODS ===================================================================

  /**
   * Reads the configuration file and initializes the project properties.
   * The file is located in the project home directory.
   * @param prop property file extension
   */
  protected synchronized void read(final String prop) {
    file = new IOFile(HOME + IO.BASEXSUFFIX + prop);

    final StringList read = new StringList();
    final TokenBuilder err = new TokenBuilder();
    if(!file.exists()) {
      err.addExt("Saving properties in \"%\"..." + NL, file);
    } else {
      BufferedReader br = null;
      try {
        br = new BufferedReader(new FileReader(file.file()));
        for(String line; (line = br.readLine()) != null;) {
          line = line.trim();
          if(line.isEmpty() || line.charAt(0) == '#') continue;
          final int d = line.indexOf('=');
          if(d < 0) {
            err.addExt("%: \"%\" ignored. " + NL, file, line);
            continue;
          }

          final String val = line.substring(d + 1).trim();
          String key = line.substring(0, d).trim();

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
            err.addExt("%: \"%\" not found. " + NL, file, key);
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
        err.addExt("% could not be parsed." + NL, file);
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
        if(!ok) err.addExt("Saving properties in \"%\"..." + NL, file);
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
   * Parses a property string and sets the properties accordingly.
   * @param s property string
   * @throws IOException io exception
   */
  protected final synchronized void parse(final String s) throws IOException {
    for(final String ser : s.trim().split(",")) {
      if(ser.isEmpty()) continue;
      final String[] sprop = ser.split("=", 2);

      final String key = sprop[0].trim();
      final String val = sprop.length < 2 ? "" : sprop[1];
      try {
        if(set(key, val) != null) continue;
      } catch(final Exception ex) {
        throw new BaseXException(Text.INVALID_VALUE_X_X, key, val);
      }
      throw new BaseXException(unknown(key));
    }
  }

  /**
   * Sets static properties.
   */
  protected void finish() {
    // nothing to do; if necessary, is overwritten.
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Scans the system properties and initializes the project properties.
   * All properties starting with {@Code org.basex.} will be assigned as properties
   * and removed from the global system properties.
   */
  private void system() {
    // collect parameters that start with "org.basex."
    final StringList sl = new StringList();
    final Properties pr = System.getProperties();
    for(final Object key : pr.keySet()) {
      String k = key.toString();
      if(k.startsWith(Prop.DBPREFIX)) sl.add(k);
    }
    // assign properties and remove existing keys
    for(final String key : sl) {
      if(set(key.substring(Prop.DBPREFIX.length()).toUpperCase(Locale.ENGLISH),
          System.getProperty(key)) != null) pr.remove(key);
    }
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
}
