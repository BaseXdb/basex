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
 * This abstract class provides methods for accessing, reading and storing options.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class AOptions implements Iterable<String> {
  /** Comment in configuration file. */
  private static final String PROPHEADER = "# " + Prop.NAME + " Options File." + NL;
  /** Comment in configuration file. */
  private static final String PROPUSER = "# Local Options";

  /** Options. */
  protected final TreeMap<String, Object> options = new TreeMap<String, Object>();
  /** Options, cached from an input file. */
  private final StringBuilder user = new StringBuilder();
  /** Options file. */
  private IOFile file;

  /**
   * Constructor.
   */
  public AOptions() {
    this(null);
  }

  /**
   * Constructor, reading options from a configuration file.
   * @param suffix if {@code null}, file parsing will be skipped
   */
  public AOptions(final String suffix) {
    try {
      for(final Object[] arr : options(getClass())) {
        if(arr.length > 1) options.put(toString(arr), arr[1]);
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
   * Writes the options to disk.
   */
  public final synchronized void write() {
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(file.file()));
      bw.write(PROPHEADER);

      for(final Object[] arr : options(getClass())) {
        final String key = toString(arr);
        if(arr.length == 1) {
          bw.write(NL + "# " + key + NL);
          continue;
        }

        final Object val = options.get(key);
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
    return options.get(key);
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
    setObject(toString(key), val);
  }

  /**
   * Assigns the specified integer for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final synchronized void set(final Object[] key, final int val) {
    setObject(toString(key), val);
  }

  /**
   * Assigns the specified boolean for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final synchronized void set(final Object[] key, final boolean val) {
    setObject(toString(key), val);
  }

  /**
   * Assigns the specified string array for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final synchronized void set(final Object[] key, final String[] val) {
    setObject(toString(key), val);
  }

  /**
   * Assigns the specified integer array for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final synchronized void set(final Object[] key, final int[] val) {
    setObject(toString(key), val);
  }

  /**
   * Assigns the specified object for the specified key.
   * @param key key to be found
   * @param val value to be written
   */
  public final synchronized void setObject(final String key, final Object val) {
    options.put(key, val);
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
      Util.notexpected("Unknown option type: " + type.getClass().getSimpleName());
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
   * Inverts a boolean option.
   * @param key key
   * @return new value
   */
  public final synchronized boolean invert(final Object[] key) {
    final boolean val = !is(key);
    set(key, val);
    return val;
  }

  /**
   * Checks if the specified option has changed.
   * @param key key
   * @param val new value
   * @return result of check
   */
  public final synchronized boolean sameAs(final Object[] key, final Object val) {
    return options.get(toString(key)).equals(val);
  }

  /**
   * Returns a key similar to the specified string, or {@code null}.
   * @param key key to be found
   * @return similar key
   */
  public final synchronized String similar(final String key) {
    final byte[] name = token(key);
    final Levenshtein ls = new Levenshtein();
    for(final String opts : options.keySet()) {
      if(ls.similar(name, token(opts))) return opts;
    }
    return null;
  }

  /**
   * Scans the system properties and initializes the database options.
   * All properties starting with {@code org.basex.} will be assigned as options.
   */
  public final void setSystem() {
    // collect parameters that start with "org.basex."
    final StringList sl = new StringList();
    final Properties pr = System.getProperties();
    for(final Object key : pr.keySet()) {
      final String k = key.toString();
      if(k.startsWith(DBPREFIX)) sl.add(k);
    }
    // assign properties
    for(final String key : sl) {
      final String k = key.substring(DBPREFIX.length()).toUpperCase(Locale.ENGLISH);
      set(k, System.getProperty(key));
    }
  }

  @Override
  public final synchronized Iterator<String> iterator() {
    return options.keySet().iterator();
  }

  @Override
  public final synchronized String toString() {
    final TokenBuilder tb = new TokenBuilder();
    for(final Entry<String, Object> e : options.entrySet()) {
      if(!tb.isEmpty()) tb.add(',');
      tb.add(e.getKey()).add('=').add(e.getValue().toString().replace(",", ",,"));
    }
    return tb.toString();
  }

  // STATIC METHODS =====================================================================

  /**
   * Returns a system property.
   * @param key {@link Options} key
   * @return value, or empty string
   */
  public static String getSystem(final Object[] key) {
    return key.length > 0 ? getSystem(toString(key)) : "";
  }

  /**
   * Returns a system property. If necessary, the key will
   * be converted to lower-case and prefixed with {@link Prop#DBPREFIX}.
   * @param key {@link Options} key
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
   * @param key {@link Options} key
   * @param val value
   */
  public static void setSystem(final Object[] key, final Object val) {
    if(key.length > 0) setSystem(toString(key), val);
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
   * Returns all options from the specified class.
   * @param clz options class
   * @return option instances
   * @throws IllegalAccessException exception
   */
  public static final Object[][] options(final Class<? extends AOptions> clz)
      throws IllegalAccessException {

    final ArrayList<Object[]> opts = new ArrayList<Object[]>();
    for(final Field f : clz.getFields()) {
      if(!Modifier.isStatic(f.getModifiers())) continue;
      final Object obj = f.get(null);
      if(obj instanceof Object[]) opts.add((Object[]) obj);
    }
    return opts.toArray(new Object[opts.size()][]);
  };

  // PROTECTED METHODS ==================================================================

  /**
   * Parses an option string and sets the options accordingly.
   * @param string options string
   * @throws IOException io exception
   */
  protected final synchronized void parse(final String string) throws IOException {
    final int sl = string.length();
    int i = 0;
    while(i < sl) {
      int k = string.indexOf('=', i);
      if(k == -1) break;
      final String key = string.substring(i, k);
      final StringBuilder val = new StringBuilder();
      i = k;
      while(++i < sl) {
        final char ch = string.charAt(i);
        if(ch == ',' && (++i == sl || string.charAt(i) != ',')) break;
        val.append(ch);
      }
      try {
        if(set(key, val.toString()) != null) continue;
      } catch(final Exception ex) {
        throw new BaseXException(Text.INVALID_VALUE_X_X, key, val);
      }
      throw new BaseXException(unknown(key));
    }
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Reads the configuration file and initializes the options.
   * The file is located in the project home directory.
   * @param suffix optional suffix of options file
   */
  private synchronized void read(final String suffix) {
    file = new IOFile(HOME + IO.BASEXSUFFIX + suffix);

    final StringList read = new StringList();
    final TokenBuilder err = new TokenBuilder();
    boolean local = false;
    if(!file.exists()) {
      err.addExt("Saving options in \"%\"..." + NL, file);
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

          final Object entry = options.get(key);
          if(entry == null) {
            err.addExt("%: \"%\" not found. " + NL, file, key);
          } else if(entry instanceof String) {
            options.put(key, val);
          } else if(entry instanceof Integer) {
            options.put(key, Integer.parseInt(val));
          } else if(entry instanceof Boolean) {
            options.put(key, Boolean.parseBoolean(val));
          } else if(entry instanceof String[]) {
            if(num == 0) {
              options.put(key, new String[Integer.parseInt(val)]);
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
        for(final Object[] arr : options(getClass())) {
          if(arr.length > 1) ok &= read.contains(toString(arr));
        }
        if(!ok) err.addExt("Saving options in \"%\"..." + NL, file);
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
    final String k = toString(key);
    final Object entry = options.get(k);
    if(entry == null) Util.notexpected("Option " + k + " not defined.");

    final Class<?> cc = entry.getClass();
    if(c != cc) Util.notexpected("Option '" + k + "' is a " + Util.name(cc));
    return entry;
  }

  /**
   * Returns a string representation of the specified key.
   * @param key option
   * @return string representation
   */
  public static String toString(final Object[] key) {
    return key[0].toString();
  }
}
