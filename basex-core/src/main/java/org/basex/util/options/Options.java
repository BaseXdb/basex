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
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * This class provides methods for accessing, reading and storing options.
 * Options (name/value pairs) may either be instances of the {@link Option} class.
 * If an instance of this class contains no pre-defined options, assigned options will
 * be added as free options.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public class Options implements Iterable<Option<?>> {
  /** Yes/No enumeration. */
  public enum YesNo {
    /** Yes. */ YES,
    /** No.  */ NO;

    @Override
    public String toString() {
      return name().toLowerCase(Locale.ENGLISH);
    }
  }

  /** Yes/No/Omit enumeration. */
  public enum YesNoOmit {
    /** Yes.  */ YES,
    /** No.   */ NO,
    /** Omit. */ OMIT;

    @Override
    public String toString() {
      return name().toLowerCase(Locale.ENGLISH);
    }
  }

  /** Comment in configuration file. */
  private static final String PROPUSER = "# Local Options";

  /** Map with option names and definition. */
  private final TreeMap<String, Option<?>> options;
  /** Map with option names and values. */
  private final TreeMap<String, Object> values;
  /** Free option definitions. */
  private final HashMap<String, String> free;

  /** Options, cached from an input file. */
  private final StringList user = new StringList();
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
    options = new TreeMap<>();
    values = new TreeMap<>();
    free = new HashMap<>();
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
    if(opts != null) read(opts);
  }

  /**
   * Constructor with options to be copied.
   * @param opts options
   */
  @SuppressWarnings("unchecked")
  protected Options(final Options opts) {
    options = (TreeMap<String, Option<?>>) opts.options.clone();
    values = (TreeMap<String, Object>) opts.values.clone();
    free = (HashMap<String, String>) opts.free.clone();
    user.add(opts.user);
    file = opts.file;
  }

  /**
   * Writes the options to disk.
   */
  public final synchronized void write() {
    final StringList lines = new StringList();
    try {
      for(final Option<?> opt : options(getClass())) {
        final String name = opt.name();
        if(opt instanceof Comment) {
          if(!lines.isEmpty()) lines.add("");
          lines.add("# " + name);
        } else if(opt instanceof NumbersOption) {
          final int[] ints = get((NumbersOption) opt);
          final int is = ints == null ? 0 : ints.length;
          for(int i = 0; i < is; ++i) lines.add(name + i + " = " + ints[i]);
        } else if(opt instanceof StringsOption) {
          final String[] strings = get((StringsOption) opt);
          final int ss = strings == null ? 0 : strings.length;
          lines.add(name + " = " + ss);
          for(int i = 0; i < ss; ++i) lines.add(name + (i + 1) + " = " + strings[i]);
        } else {
          lines.add(name + " = " + get(opt));
        }
      }
      lines.add("").add(PROPUSER).add(user);

      // only write file if contents have changed
      if(update(lines)) {
        final TokenBuilder tb = new TokenBuilder();
        for(final String line : lines) tb.add(line).add(NL);
        file.write(tb.finish());
      }

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
   * Returns the requested map.
   * @param option option to be found
   * @return value
   */
  public final synchronized Map get(final MapOption option) {
    return (Map) get((Option<?>) option);
  }

  /**
   * Returns the requested function.
   * @param option option to be found
   * @return value
   */
  public final synchronized FuncItem get(final FuncOption option) {
    return (FuncItem) get((Option<?>) option);
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
  @SuppressWarnings({ "unchecked", "cast"})
  public final synchronized <O extends Options> O get(final OptionsOption<O> option) {
    return (O) get((Option<?>) option);
  }

  /**
   * Returns the requested enum value.
   * @param option option to be found
   * @param <V> enumeration value
   * @return value
   */
  @SuppressWarnings({ "unchecked", "cast"})
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
   * Assigns a value after casting it to the correct type. If the option is unknown,
   * it will be added as free option.
   * @param name name of option
   * @param value value
   * @param error error
   * @param ii input info
   * @throws BaseXException database exception
   * @throws QueryException query exception
   */
  public synchronized void assign(final Item name, final Item value, final boolean error,
      final InputInfo ii) throws BaseXException, QueryException {

    final String key = string(name.string(ii));
    if(options.isEmpty()) {
      final byte[] val;
      if(value instanceof Map) {
        final TokenBuilder tb = new TokenBuilder();
        final Map map = (Map) value;
        for(final Item it : map.keys()) {
          if(!tb.isEmpty()) tb.add(',');
          tb.add(it.string(ii)).add('=');
          final Value v = map.get(it, ii);
          if(v instanceof Item) tb.add(string(((Item) v).string(ii)).replace(",", ",,"));
          else throw new BaseXException(Text.OPT_EXPECT_X_X_X, AtomType.ITEM, v.seqType(), v);
        }
        val = tb.finish();
      } else {
        val = value.string(ii);
      }
      free.put(key, string(val));
    } else {
      assign(key, value, error, ii);
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
   * Returns a map representation of the entries of the requested string (separated by commas).
   * @param option option to be found
   * @return map
   */
  public final HashMap<String, String> toMap(final StringOption option) {
    final String input = get(option).trim();
    final HashMap<String, String> map = new HashMap<>();
    final StringBuilder key = new StringBuilder();
    final StringBuilder value = new StringBuilder();
    boolean first = true;
    final int sl = input.length();
    for(int s = 0; s < sl; s++) {
      final char ch = input.charAt(s);
      if(first) {
        if(ch == '=') {
          first = false;
        } else {
          key.append(ch);
        }
      } else {
        if(ch == ',') {
          if(s + 1 == sl || input.charAt(s + 1) != ',') {
            map.put(key.toString().trim(), value.toString());
            key.setLength(0);
            value.setLength(0);
            first = true;
            continue;
          }
          // literal commas are escaped by a second comma
          s++;
        }
        value.append(ch);
      }
    }
    if(!first) map.put(key.toString().trim(), value.toString());
    return map;
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
   * Overwrites the options with global options and system properties.
   * All properties starting with {@code org.basex.} will be assigned as options.
   */
  public final void setSystem() {
    // assign global options
    for(final Entry<String, String> entry : entries()) {
      String name = entry.getKey();
      final String value = entry.getValue();
      if(name.startsWith(DBPREFIX)) {
        name = name.substring(DBPREFIX.length()).toUpperCase(Locale.ENGLISH);
        try {
          if(assign(name, value, -1, false)) Util.debug(name + Text.COLS + value);
        } catch(final BaseXException ex) {
          Util.errln(ex);
        }
      }
    }
  }

  /**
   * Parses the specified options.
   * @param type media type
   * @throws BaseXException database exception
   */
  public final synchronized void assign(final MediaType type) throws BaseXException {
    for(final Entry<String, String> entry : type.parameters().entrySet()) {
      if(options.isEmpty()) {
        free.put(entry.getKey(), entry.getValue());
      } else {
        // ignore unknown options
        assign(entry.getKey(), entry.getValue(), -1, false);
      }
    }
  }

  /**
   * Parses and assigns options string from the specified string.
   * @param string options string
   * @throws BaseXException database exception
   */
  public final synchronized void assign(final String string) throws BaseXException {
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
   * Parses and assigns options from the specified map.
   * @param map map
   * @param error raise error if option is unknown
   * @param ii input info
   * @throws BaseXException database exception
   * @throws QueryException query exception
   */
  public final synchronized void assign(final Map map, final boolean error, final InputInfo ii)
      throws BaseXException, QueryException {

    for(final Item name : map.keys()) {
      if(!name.type.isStringOrUntyped())
        throw new BaseXException(Text.OPT_EXPECT_X_X_X, AtomType.STR, name.type, name);

      final Value value = map.get(name, ii);
      if(!(value instanceof Item))
        throw new BaseXException(Text.OPT_EXPECT_X_X_X, AtomType.ITEM, value.seqType(), value);

      assign(name, (Item) value, error, ii);
    }
  }

  /**
   * Returns the names of all options.
   * @return names
   */
  public final synchronized String[] names() {
    final StringList sl = new StringList(options.size());
    for(final Option<?> option : this) sl.add(option.name());
    return sl.finish();
  }

  @Override
  public final synchronized Iterator<Option<?>> iterator() {
    return options.values().iterator();
  }

  @Override
  public final synchronized String toString() {
    // only those options are listed whose value differs from default value
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
   * Returns a message with allowed keys.
   * @param option option
   * @param value supplied value
   * @param all allowed values
   * @return exception
   */
  public static String allowed(final Option<?> option, final String value, final Object... all) {
    final TokenBuilder vals = new TokenBuilder();
    for(final Object a : all) vals.add(vals.isEmpty() ? "" : ",").add(a.toString());
    return Util.info(Text.OPT_ONEOF_X_X_X, option.name(), value, vals);
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Returns all options from the specified class.
   * @param clz options class
   * @return option instances
   * @throws IllegalAccessException exception
   */
  private static Option<?>[] options(final Class<? extends Options> clz)
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
      try(NewlineInput nli = new NewlineInput(opts)) {
        boolean local = false;
        for(String line; (line = nli.readLine()) != null;) {
          line = line.trim();

          // start of local options
          if(line.equals(PROPUSER)) {
            local = true;
            continue;
          }
          if(local) user.add(line);

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
            // cache local options as global options
            Prop.put(DBPREFIX + name.toLowerCase(Locale.ENGLISH), val);
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
   * Checks if the options file needs to be updated.
   * @param lines lines of new file
   * @return result of check
   * @throws IOException I/O exception
   */
  private boolean update(final StringList lines) throws IOException {
    if(!file.exists()) return true;

    int l = 0;
    final int ls = lines.size();
    try(NewlineInput nli = new NewlineInput(file)) {
      for(String line; (line = nli.readLine()) != null;) {
        if(l == ls || !lines.get(l++).equals(line)) return true;
      }
    }
    return l != ls;
  }

  /**
   * Assigns the specified name and value.
   * @param name name of option
   * @param item value of option
   * @param error raise error if option is unknown
   * @param ii input info
   * @throws BaseXException database exception
   * @throws QueryException query exception
   */
  private synchronized void assign(final String name, final Item item, final boolean error,
      final InputInfo ii) throws BaseXException, QueryException {

    final Option<?> option = options.get(name);
    if(option == null) {
      if(error) throw new BaseXException(error(name));
      return;
    }

    if(option instanceof BooleanOption) {
      final boolean v;
      if(item.type.isStringOrUntyped()) {
        final String string = string(item.string(null));
        v = Strings.yes(string);
        if(!v && !Strings.no(string))
          throw new BaseXException(Text.OPT_BOOLEAN_X_X, option.name(), string);
      } else if(item instanceof Bln) {
        v = ((Bln) item).bool(null);
      } else {
        throw new BaseXException(Text.OPT_BOOLEAN_X_X, option.name(), item);
      }
      put(option, v);
    } else if(option instanceof NumberOption) {
      if(item instanceof ANum) {
        put(option, (int) ((ANum) item).itr(null));
      } else {
        throw new BaseXException(Text.OPT_NUMBER_X_X, option.name(), item);
      }
    } else if(option instanceof StringOption) {
      if(item.type.isStringOrUntyped()) {
        put(option, string(item.string(null)));
      } else {
        throw new BaseXException(Text.OPT_STRING_X_X, option.name(), item);
      }
    } else if(option instanceof EnumOption) {
      if(item.type.isStringOrUntyped()) {
        final EnumOption<?> eo = (EnumOption<?>) option;
        final String string = string(item.string(null));
        final Object v = eo.get(string);
        if(v == null) throw new BaseXException(allowed(option, string, (Object[]) eo.values()));
        put(option, v);
      } else {
        throw new BaseXException(Text.OPT_STRING_X_X, option.name(), item);
      }
    } else if(option instanceof OptionsOption) {
      final Options o = ((OptionsOption<?>) option).newInstance();
      if(item instanceof Map) {
        o.assign((Map) item, error, ii);
      } else {
        throw new BaseXException(Text.OPT_MAP_X_X, option.name(), item);
      }
      put(option, o);
    } else if(option instanceof MapOption) {
      if(!(item instanceof Map)) throw new BaseXException(Text.OPT_FUNC_X_X, option.name(), item);
      put(option, item);
    } else if(option instanceof FuncOption) {
      if(!(item instanceof FuncItem))
        throw new BaseXException(Text.OPT_FUNC_X_X, option.name(), item);
      put(option, item);
    } else {
      throw Util.notExpected("Unsupported option: " + option);
    }
  }

  /**
   * Assigns the specified name and value.
   * @param name name of option
   * @param val value of option
   * @param index index (optional, can be {@code -1})
   * @param error raise error if option is unknown
   * @return success flag
   * @throws BaseXException database exception
   */
  private synchronized boolean assign(final String name, final String val, final int index,
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
        if(b == null) throw new BaseXException(Text.OPT_BOOLEAN_X_X, option.name(), "");
        v = !b;
      } else {
        v = Strings.yes(val);
        if(!v && !Strings.no(val))
          throw new BaseXException(Text.OPT_BOOLEAN_X_X, option.name(), val);
      }
      put(option, v);
    } else if(option instanceof NumberOption) {
      final int v = Strings.toInt(val);
      if(v == MIN_VALUE) throw new BaseXException(Text.OPT_NUMBER_X_X, option.name(), val);
      put(option, v);
    } else if(option instanceof StringOption) {
      put(option, val);
    } else if(option instanceof EnumOption) {
      final EnumOption<?> eo = (EnumOption<?>) option;
      final Object v = eo.get(val);
      if(v == null) throw new BaseXException(allowed(option, val, (Object[]) eo.values()));
      put(option, v);
    } else if(option instanceof OptionsOption) {
      final Options o = ((OptionsOption<?>) option).newInstance();
      o.assign(val);
      put(option, o);
    } else if(option instanceof NumbersOption) {
      final int v = Strings.toInt(val);
      if(v == MIN_VALUE) throw new BaseXException(Text.OPT_NUMBER_X_X, option.name(), val);
      int[] ii = (int[]) get(option);
      if(index == -1) {
        if(ii == null) ii = new int[0];
        final IntList il = new IntList(ii.length + 1);
        for(final int i : ii) il.add(i);
        put(option, il.add(v).finish());
      } else {
        if(index < 0 || index >= ii.length)
          throw new BaseXException(Text.OPT_OFFSET_X, option.name());
        ii[index] = v;
      }
    } else if(option instanceof StringsOption) {
      String[] ss = (String[]) get(option);
      if(index == -1) {
        if(ss == null) ss = new String[0];
        final StringList sl = new StringList(ss.length + 1);
        for(final String s : ss) sl.add(s);
        put(option, sl.add(val).finish());
      } else if(index == 0) {
        final int v = Strings.toInt(val);
        if(v == MIN_VALUE) throw new BaseXException(Text.OPT_NUMBER_X_X, option.name(), val);
        values.put(name, new String[v]);
      } else {
        if(index <= 0 || index > ss.length)
          throw new BaseXException(Text.OPT_OFFSET_X, option.name());
        ss[index - 1] = val;
      }
    } else {
      throw Util.notExpected("Unsupported option: " + option);
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
