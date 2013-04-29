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
  /** Cached user properties. */
  private final StringBuilder user = new StringBuilder();
  /** Properties. */
  protected final TreeMap<String, Object> props = new TreeMap<String, Object>();
  /** Property file. */
  private IOFile file;

  /**
   * Constructor.
   */
  public AProp() {
    this(null);
  }

  /**
   * Constructor, reading options from a configuration file.
   * @param suffix if {@code null}, file parsing will be skipped
   */
  public AProp(final String suffix) {
    try {
      for(final Object[] arr : props(getClass())) {
        if(arr.length > 1) props.put(arr[0].toString(), arr[1]);
      }
    } catch(final Exception ex) {
      ex.printStackTrace();
      Util.notexpected(ex);
    }
    if(suffix != null) read(suffix);
    // sets options stored in system properties
    setSystem();
  }

  /**
   * Writes the properties to disk.
   */
  public final synchronized void write() {
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(file.file()));
      bw.write(PROPHEADER);

      for(final Object[] arr : props(getClass())) {
        final String key = arr[0].toString();
        if(arr.length == 1) {
          bw.write(NL + "# " + key + NL);
          continue;
        }

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
      if(bw != null) try { bw.close(); } catch(final IOException ignored) { }
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

  /**
   * Scans the system properties and initializes the project properties.
   * All properties starting with {@code org.basex.} will be assigned as properties
   * and removed from the global system properties.
   */
  public final void setSystem() {
    // collect parameters that start with "org.basex."
    final StringList sl = new StringList();
    final Properties pr = System.getProperties();
    for(final Object key : pr.keySet()) {
      final String k = key.toString();
      if(k.startsWith(Prop.DBPREFIX)) sl.add(k);
    }
    // assign properties
    for(final String key : sl) {
      set(key.substring(Prop.DBPREFIX.length()).toUpperCase(Locale.ENGLISH),
          System.getProperty(key));
    }
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

  /**
   * Returns a system property.
   * @param key {@link Prop} key
   * @return value, or empty string
   */
  public static String getSystem(final Object[] key) {
    return key.length > 0 ? getSystem(key[0].toString()) : "";
  }

  // STATIC METHODS =====================================================================

  /**
   * Returns a system property. If necessary, the key will
   * be converted to lower-case and prefixed with {@link Prop#DBPREFIX}.
   * @param key {@link Prop} key
   * @return value, or empty string
   */
  public static String getSystem(final String key) {
    final String k = (key.startsWith(DBPREFIX) ? key : DBPREFIX + key).
        toLowerCase(Locale.ENGLISH);
    final String v = System.getProperty(k);
    return v == null ? "" : v;
  }

  /**
   * Sets a system property if it has not been set before.
   * @param key {@link Prop} key
   * @param val value
   */
  public static void setSystem(final Object[] key, final Object val) {
    if(key.length > 0) setSystem(key[0].toString(), val);
  }

  /**
   * Sets a system property if it has not been set before. If necessary, the key will
   * be converted to lower-case and prefixed with {@link Prop#DBPREFIX}.
   * @param key key
   * @param val value
   */
  public static void setSystem(final String key, final Object val) {
    final String k = key.indexOf('.') != -1 ? key :
      DBPREFIX + key.toLowerCase(Locale.ENGLISH);
    if(System.getProperty(k) == null) System.setProperty(k, val.toString());
  }

  /**
   * Returns all property objects from the specified property class.
   * @param clz property class
   * @return property objects
   * @throws IllegalAccessException exception
   */
  public static final Object[][] props(final Class<? extends AProp> clz)
      throws IllegalAccessException {

    final ArrayList<Object[]> props = new ArrayList<Object[]>();
    for(final Field f : clz.getFields()) {
      if(!Modifier.isStatic(f.getModifiers())) continue;
      final Object obj = f.get(null);
      if(!(obj instanceof Object[])) continue;
      props.add((Object[]) obj);
    }
    return props.toArray(new Object[props.size()][]);
  };

  // PROTECTED METHODS ==================================================================

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

  // PRIVATE METHODS ====================================================================

  /**
   * Reads the configuration file and initializes the project properties.
   * The file is located in the project home directory.
   * @param prop property file extension
   */
  private synchronized void read(final String prop) {
    file = new IOFile(HOME + IO.BASEXSUFFIX + prop);

    final StringList read = new StringList();
    final TokenBuilder err = new TokenBuilder();
    boolean local = false;
    if(!file.exists()) {
      err.addExt("Saving properties in \"%\"..." + NL, file);
    } else {
      BufferedReader br = null;
      try {
        br = new BufferedReader(new FileReader(file.file()));
        for(String line; (line = br.readLine()) != null;) {
          line = line.trim();

          // start of local options
          if(line.equals(PROPUSER)) {
            local = true;
            continue;
          }
          if(local) user.append(line).append(NL);

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
          // cache local options as system properties
          if(local) {
            setSystem(key, val);
            continue;
          }

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
          // add key for final check
          read.add(key);
        }
      } catch(final Exception ex) {
        err.addExt("% could not be parsed." + NL, file);
        Util.debug(ex);
      } finally {
        if(br != null) try { br.close(); } catch(final IOException ignored) { }
      }
    }

    // check if all mandatory files have been read
    try {
      if(err.isEmpty()) {
        boolean ok = true;
        for(final Object[] arr : props(getClass())) {
          if(arr.length > 1) ok &= read.contains(arr[0].toString());
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
