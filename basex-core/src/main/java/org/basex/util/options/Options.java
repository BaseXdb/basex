package org.basex.util.options;

import static java.lang.Integer.*;
import static org.basex.util.Prop.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class provides methods for accessing, reading and storing options.
 * Options (name/value pairs) may either be instances of the {@link Option} class.
 * If an instance of this class contains no pre-defined options, assigned options will
 * be added as free options.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class Options implements Iterable<Option<?>> {
  /** Yes/No enumeration. */
  public enum YesNo {
    /** Yes. */ YES,
    /** No.  */ NO;

    @Override
    public String toString() {
      return super.toString().toLowerCase(Locale.ENGLISH);
    }
  }

  /** Yes/No/Omit enumeration. */
  public enum YesNoOmit {
    /** Yes.  */ YES,
    /** No.   */ NO,
    /** Omit. */ OMIT;

    @Override
    public String toString() {
      return super.toString().toLowerCase(Locale.ENGLISH);
    }
  }

  /** Comment in configuration file. */
  private static final String PROPUSER = "# Local Options";

  /** Map with option names and definition. */
  protected final TreeMap<String, Option<?>> options = new TreeMap<>();
  /** Map with option names and values. */
  private final TreeMap<String, Object> values = new TreeMap<>();
  /** Free option definitions. */
  private final HashMap<String, String> free = new HashMap<>();

  /** Options, cached from an input file. */
  private final StringBuilder user = new StringBuilder();
  /** Options file. */
  private IOFile file;

  /**
   * Default constructor.
   */
  public Options() {
    this((IOFile) null);
  }

  /**
   * Constructor with options file.
   * @param opts options file
   */
  protected Options(final IOFile opts) {
    init();
    if(opts != null) read(opts);
  }

  /**
   * Constructor with options file.
   * @param opts options file
   */
  protected Options(final Options opts) {
    for(final Entry<String, Option<?>> e : opts.options.entrySet())
      options.put(e.getKey(), e.getValue());
    for(final Entry<String, Object> e : opts.values.entrySet())
      values.put(e.getKey(), e.getValue());
    for(final Entry<String, String> e : opts.free.entrySet())
      free.put(e.getKey(), e.getValue());
    user.append(opts.user);
    file = opts.file;
  }

  /**
   * Initializes all options.
   */
  private void init() {
    try {
      for(final Option<?> opt : options(getClass())) {
        if(opt instanceof Comment) continue;
        final String name = opt.name();
        values.put(name, opt.value());
        options.put(name, opt);
      }
    } catch(final Exception ex) {
      throw Util.notExpected(ex);
    }
  }

  /**
   * Writes the options to disk.
   */
  public final synchronized void write() {
    final TokenBuilder tmp = new TokenBuilder();
    try {
      boolean first = true;
      for(final Option<?> opt : options(getClass())) {
        final String name = opt.name();
        if(opt instanceof Comment) {
          if(!first) tmp.add(NL);
          tmp.add("# " + name).add(NL);
        } else if(opt instanceof NumbersOption) {
          final int[] ints = get((NumbersOption) opt);
          final int is = ints == null ? 0 : ints.length;
          for(int i = 0; i < is; ++i) tmp.add(name + i + " = " + ints[i]).add(NL);
        } else if(opt instanceof StringsOption) {
          final String[] strings = get((StringsOption) opt);
          final int ss = strings == null ? 0 : strings.length;
          tmp.add(name + " = " + ss).add(NL);
          for(int i = 0; i < ss; ++i) tmp.add(name + (i + 1) + " = " + strings[i]).add(NL);
        } else {
          tmp.add(name + " = " + get(opt)).add(NL);
        }
        first = false;
      }
      tmp.add(NL).add(PROPUSER).add(NL);
      tmp.add(user.toString());
      final byte[] content = tmp.finish();

      // only write file if contents have changed
      if(!file.exists() || !eq(content, file.read())) file.write(content);

    } catch(final Exception ex) {
      Util.errln("% could not be written.", file);
      Util.debug(ex);
    }
  }

  /**
   * Returns the option with the specified name.
   * @param name name of the option
   * @return value (may be {@code null})
   */
  public final synchronized Option<?> option(final String name) {
    return options.get(name);
  }

  /**
   * Returns the value of the specified option.
   * @param option option
   * @return value (may be {@code null})
   */
  public final synchronized Object get(final Option<?> option) {
    return values.get(option.name());
  }

  /**
   * Sets an option to a value without checking its type.
   * @param option option
   * @param value value to be assigned
   */
  public final synchronized void put(final Option<?> option, final Object value) {
    values.put(option.name(), value);
  }

  /**
   * Checks if a value was set for the specified option.
   * @param option option
   * @return result of check
   */
  public final synchronized boolean contains(final Option<?> option) {
    return get(option) != null;
  }

  /**
   * Returns the requested string.
   * @param option option to be found
   * @return value
   */
  public final synchronized String get(final StringOption option) {
    return (String) get((Option<?>) option);
  }

  /**
   * Returns the requested number.
   * @param option option to be found
   * @return value
   */
  public final synchronized Integer get(final NumberOption option) {
    return (Integer) get((Option<?>) option);
  }

  /**
   * Returns the requested boolean.
   * @param option option to be found
   * @return value
   */
  public final synchronized Boolean get(final BooleanOption option) {
    return (Boolean) get((Option<?>) option);
  }

  /**
   * Returns the original instance of the requested string array.
   * @param option option to be found
   * @return value
   */
  public final synchronized String[] get(final StringsOption option) {
    return (String[]) get((Option<?>) option);
  }

  /**
   * Returns the original instance of the requested integer array.
   * @param option option to be found
   * @return value
   */
  public final synchronized int[] get(final NumbersOption option) {
    return (int[]) get((Option<?>) option);
  }

  /**
   * Returns the original instance of the requested options.
   * @param option option to be found
   * @param <O> options
   * @return value
   */
  @SuppressWarnings("unchecked")
  public final synchronized <O extends Options> O get(final OptionsOption<O> option) {
    return (O) get((Option<?>) option);
  }

  /**
   * Returns the requested enum value.
   * @param option option to be found
   * @param <V> enumeration value
   * @return value
   */
  @SuppressWarnings("unchecked")
  public final synchronized <V extends Enum<V>> V get(final EnumOption<V> option) {
    return (V) get((Option<?>) option);
  }

  /**
   * Sets the string value of an option.
   * @param option option to be set
   * @param value value to be written
   */
  public final synchronized void set(final StringOption option, final String value) {
    put(option, value);
  }

  /**
   * Sets the integer value of an option.
   * @param option option to be set
   * @param value value to be written
   */
  public final synchronized void set(final NumberOption option, final int value) {
    put(option, value);
  }

  /**
   * Sets the boolean value of an option.
   * @param option option to be set
   * @param value value to be written
   */
  public final synchronized void set(final BooleanOption option, final boolean value) {
    put(option, value);
  }

  /**
   * Sets the string array value of an option.
   * @param option option to be set
   * @param value value to be written
   */
  public final synchronized void set(final StringsOption option, final String[] value) {
    put(option, value);
  }

  /**
   * Sets the integer array value of an option.
   * @param option option to be set
   * @param value value to be written
   */
  public final synchronized void set(final NumbersOption option, final int[] value) {
    put(option, value);
  }

  /**
   * Sets the options of an option.
   * @param option option to be set
   * @param value value to be set
   * @param <O> options
   */
  public final synchronized <O extends Options> void set(final OptionsOption<O> option,
      final O value) {
    put(option, value);
  }

  /**
   * Sets the enumeration of an option.
   * @param option option to be set
   * @param value value to be set
   * @param <V> enumeration value
   */
  public final synchronized <V extends Enum<V>> void set(final EnumOption<V> option,
      final Enum<V> value) {
    put(option, value);
  }

  /**
   * Sets the enumeration of an option.
   * @param option option to be set
   * @param value string value, which will be converted to an enum value or {@code null}
   * @param <V> enumeration value
   */
  public final synchronized <V extends Enum<V>> void set(final EnumOption<V> option,
      final String value) {
    put(option, option.get(value));
  }

  /**
   * Assigns a value after casting it to the correct type. If the option is unknown,
   * it will be added as free option.
   * @param name name of option
   * @param val value
   * @throws BaseXException database exception
   */
  public synchronized void assign(final String name, final String val) throws BaseXException {
    if(options.isEmpty()) {
      free.put(name, val);
    } else {
      assign(name, val, -1, true);
    }
  }

  /**
   * Returns all name/value pairs without pre-defined option.
   * @return options
   */
  public final synchronized HashMap<String, String> free() {
    return free;
  }

  /**
   * Returns an error string for an unknown option.
   * @param name name of option
   * @return error string
   */
  public final synchronized String error(final String name) {
    final String sim = similar(name);
    return Util.info(sim != null ? Text.UNKNOWN_OPT_SIMILAR_X_X : Text.UNKNOWN_OPTION_X, name, sim);
  }

  /**
   * Inverts the boolean value of an option.
   * @param option option
   * @return new value
   */
  public final synchronized boolean invert(final BooleanOption option) {
    final boolean val = !get(option);
    set(option, val);
    return val;
  }

  /**
   * Overwrites the options with system properties.
   * All properties starting with {@code org.basex.} will be assigned as options.
   */
  public final void setSystem() {
    // collect parameters that start with "org.basex."
    final StringList sl = new StringList();
    for(final Object key : System.getProperties().keySet()) {
      final String k = key.toString();
      if(k.startsWith(DBPREFIX)) sl.add(k);
    }
    // assign properties
    for(final String key : sl) {
      final String v = System.getProperty(key);
      try {
        final String k = key.substring(DBPREFIX.length()).toUpperCase(Locale.ENGLISH);
        if(assign(k, v, -1, false)) Util.debug(k + Text.COLS + v);
      } catch(final BaseXException ex) {
        Util.errln(ex);
      }
    }
  }

  @Override
  public final synchronized Iterator<Option<?>> iterator() {
    return options.values().iterator();
  }

  @Override
  public final synchronized String toString() {
    // only those options are listed the value of which differs from default value
    final StringBuilder sb = new StringBuilder();
    for(final Entry<String, Object> e : values.entrySet()) {
      final String name = e.getKey();
      final Object value = e.getValue();
      if(value == null) continue;

      final StringList sl = new StringList();
      final Object value2 = options.get(name).value();
      if(value instanceof String[]) {
        for(final String s : (String[]) value) sl.add(s);
      } else if(value instanceof int[]) {
        for(final int s : (int[]) value) sl.add(Integer.toString(s));
      } else if(value instanceof Options) {
        final String s = value.toString();
        if(value2 == null || !s.equals(value2.toString())) sl.add(s);
      } else if(!value.equals(value2)) {
        sl.add(value.toString());
      }
      for(final String s : sl) {
        if(sb.length() != 0) sb.append(',');
        sb.append(name).append('=').append(s.replace(",", ",,"));
      }
    }
    return sb.toString();
  }

  // STATIC METHODS =====================================================================

  /**
   * Returns a system property. If necessary, the key will be converted to lower-case
   * and prefixed with the {@link Prop#DBPREFIX} string.
   * @param option option
   * @return value, or empty string
   */
  public static String getSystem(final Option<?> option) {
    String name = option.name().toLowerCase(Locale.ENGLISH);
    if(!name.startsWith(DBPREFIX)) name = DBPREFIX + name;
    final String v = System.getProperty(name);
    return v == null ? "" : v;
  }

  /**
   * Sets a system property if it has not been set before.
   * @param option option
   * @param val value
   */
  public static void setSystem(final Option<?> option, final Object val) {
    setSystem(option.name(), val);
  }

  /**
   * Sets a system property if it has not been set before. If necessary, the key will
   * be converted to lower-case and prefixed with the {@link Prop#DBPREFIX} string.
   * @param key key
   * @param val value
   */
  public static void setSystem(final String key, final Object val) {
    final String name = key.indexOf('.') == -1 ? DBPREFIX + key.toLowerCase(Locale.ENGLISH) : key;
    final String value = val.toString();
    if(System.getProperty(name) == null) {
      if(value.isEmpty()) System.clearProperty(name);
      else System.setProperty(name, val.toString());
    }
  }

  /**
   * Returns all options from the specified class.
   * @param clz options class
   * @return option instances
   * @throws IllegalAccessException exception
   */
  public static Option<?>[] options(final Class<? extends Options> clz)
      throws IllegalAccessException {

    final ArrayList<Option<?>> opts = new ArrayList<>();
    for(final Field f : clz.getFields()) {
      if(!Modifier.isStatic(f.getModifiers())) continue;
      final Object obj = f.get(null);
      if(obj instanceof Option) opts.add((Option<?>) obj);
    }
    return opts.toArray(new Option[opts.size()]);
  }

  /**
   * Returns a list of allowed keys.
   * @param option option
   * @param all allowed values
   * @return exception
   */
  public static String allowed(final Option<?> option, final Object... all) {
    final TokenBuilder vals = new TokenBuilder();
    for(final Object a : all) vals.add(vals.isEmpty() ? "" : ",").add(a.toString());
    return Util.info(Text.OPT_ONEOF, option.name(), vals);
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Parses an option string and sets the options accordingly.
   * @param string options string
   * @throws BaseXException database exception
   */
  public synchronized void parse(final String string) throws BaseXException {
    final int sl = string.length();
    int i = 0;
    while(i < sl) {
      int k = string.indexOf('=', i);
      if(k == -1) k = sl;
      final String key = string.substring(i, k).trim();
      final StringBuilder val = new StringBuilder();
      i = k;
      while(++i < sl) {
        final char ch = string.charAt(i);
        if(ch == ',' && (++i == sl || string.charAt(i) != ',')) break;
        val.append(ch);
      }
      assign(key, val.toString());
    }
  }

  /**
   * Reads the configuration file and initializes the options.
   * The file is located in the project home directory.
   * @param opts options file
   */
  private synchronized void read(final IOFile opts) {
    file = opts;
    final StringList read = new StringList();
    final StringList errs = new StringList();
    final boolean exists = file.exists();
    if(exists) {
      try(final NewlineInput nli = new NewlineInput(opts)) {
        boolean local = false;
        for(String line; (line = nli.readLine()) != null;) {
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
            errs.add("line \"" + line + "\" ignored.");
            continue;
          }

          final String val = line.substring(d + 1).trim();
          String name = line.substring(0, d).trim();

          // extract numeric value in key
          int num = 0;
          final int ss = name.length();
          for(int s = 0; s < ss; ++s) {
            if(Character.isDigit(name.charAt(s))) {
              num = Strings.toInt(name.substring(s));
              name = name.substring(0, s);
              break;
            }
          }

          if(local) {
            // cache local options as system properties
            setSystem(name, val);
          } else {
            try {
              assign(name, val, num, true);
              read.add(name);
            } catch(final BaseXException ex) {
              errs.add(ex.getMessage());
            }
          }
        }
      } catch(final IOException ex) {
        errs.add("file could not be parsed.");
        Util.errln(ex);
      }
    }

    // check if all mandatory files have been read
    boolean ok = true;
    if(errs.isEmpty()) {
      try {
        for(final Option<?> opt : options(getClass())) {
          if(ok && !(opt instanceof Comment)) ok = read.contains(opt.name());
        }
      } catch(final IllegalAccessException ex) {
        throw Util.notExpected(ex);
      }
    }

    if(!ok || !exists || !errs.isEmpty()) {
      write();
      errs.add("writing new configuration file.");
      for(final String s : errs) Util.errln(file + ": " + s);
    }
  }

  /**
   * Assigns the specified name and value.
   * @param name name of option
   * @param val value of option
   * @param num number (optional)
   * @param error raise error if option is unknown
   * @return success flag
   * @throws BaseXException database exception
   */
  private synchronized boolean assign(final String name, final String val, final int num,
      final boolean error) throws BaseXException {

    final Option<?> option = options.get(name);
    if(option == null) {
      if(error) throw new BaseXException(error(name));
      return false;
    }

    if(option instanceof BooleanOption) {
      final boolean v;
      if(val == null || val.isEmpty()) {
        final Boolean b = get((BooleanOption) option);
        if(b == null) throw new BaseXException(Text.OPT_BOOLEAN, option.name());
        v = !b;
      } else {
        v = Strings.yes(val);
        if(!v && !Strings.no(val)) throw new BaseXException(Text.OPT_BOOLEAN, option.name());
      }
      put(option, v);
    } else if(option instanceof NumberOption) {
      final int v = Strings.toInt(val);
      if(v == MIN_VALUE) throw new BaseXException(Text.OPT_NUMBER, option.name());
      put(option, v);
    } else if(option instanceof StringOption) {
      put(option, val);
    } else if(option instanceof EnumOption) {
      final EnumOption<?> eo = (EnumOption<?>) option;
      final Object v = eo.get(val);
      if(v == null) throw new BaseXException(allowed(option, (Object[]) eo.values()));
      put(option, v);
    } else if(option instanceof OptionsOption) {
      final Options o = ((OptionsOption<?>) option).newInstance();
      o.parse(val);
      put(option, o);
    } else if(option instanceof NumbersOption) {
      final int v = Strings.toInt(val);
      if(v == MIN_VALUE) throw new BaseXException(Text.OPT_NUMBER, option.name());
      int[] ii = (int[]) get(option);
      if(num == -1) {
        if(ii == null) ii = new int[0];
        final IntList il = new IntList(ii.length + 1);
        for(final int i : ii) il.add(i);
        put(option, il.add(v).finish());
      } else {
        if(num < 0 || num >= ii.length) throw new BaseXException(Text.OPT_OFFSET, option.name());
        ii[num] = v;
      }
    } else if(option instanceof StringsOption) {
      String[] ss = (String[]) get(option);
      if(num == -1) {
        if(ss == null) ss = new String[0];
        final StringList sl = new StringList(ss.length + 1);
        for(final String s : ss) sl.add(s);
        put(option, sl.add(val).finish());
      } else if(num == 0) {
        final int v = Strings.toInt(val);
        if(v == MIN_VALUE) throw new BaseXException(Text.OPT_NUMBER, option.name());
        values.put(name, new String[v]);
      } else {
        if(num <= 0 || num > ss.length) throw new BaseXException(Text.OPT_OFFSET, option.name());
        ss[num - 1] = val;
      }
    }
    return true;
  }

  /**
   * Returns an option name similar to the specified string or {@code null}.
   * @param name name to be found
   * @return similar name
   */
  private String similar(final String name) {
    final byte[] nm = token(name);
    final Levenshtein ls = new Levenshtein();
    for(final String opts : options.keySet()) {
      if(ls.similar(nm, token(opts))) return opts;
    }
    return null;
  }
}
