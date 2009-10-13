package org.basex.core;

import static org.basex.core.Text.*;
import java.util.ArrayList;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.Crypter;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class organizes all users.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class Users {
  /** Default read permission. */
  private static final boolean READ = true;
  /** Default write permission. */
  private static final boolean WRITE = true;
  /** Default create permission. */
  private static final boolean CREATE = false;
  /** Default admin permission. */
  private static final boolean ADMIN = false;

  /** User list. */
  private final ArrayList<User> users = new ArrayList<User>();
  /** Filename. */
  private final String file;
  /** Crypter. */
  private final Crypter crypt = new Crypter();

  /**
   * Standard constructor.
   * @param ctx Context
   */
  public Users(final Context ctx) {
    file = ctx.prop.get(Prop.DBPATH) + "/users" + IO.BASEXSUFFIX;
    read();
  }

  /**
   * Stores a user and encrypted password.
   * @param usern user name
   * @param pass password
   * @return success of operation
   */
  public boolean create(final String usern, final String pass) {
    // check if user exists already
    if(users.contains(usern)) return false;

    final User user = new User();
    user.name = usern;
    user.pw = crypt.encrypt(Token.token(pass));
    user.read = READ;
    user.write = WRITE;
    user.create = CREATE;
    user.admin = ADMIN;
    users.add(user);
    write();
    return true;
  }

  /**
   * Drops a user from the list.
   * @param usern String
   * @return success of operation
   */
  public boolean drop(final String usern) {
    final int i = users.indexOf(usern);
    if(i == -1) return false;
    users.remove(i);
    write();
    return true;
  }

  /**
   * Reads users from disk.
   */
  private void read() {
    if(IO.get(file).exists()) {
      try {
        final DataInput in = new DataInput(file);
        final int s = in.readNum();
        for(int u = 0; u < s; u++) {
          final User user = new User();
          user.name = in.readString();
          user.pw = in.readBytes();
          user.read = in.readBool();
          user.write = in.readBool();
          user.create = in.readBool();
          user.admin = in.readBool();
          users.add(user);
        }
      } catch(final Exception ex) {
        Main.debug(ex);
      }
    }
  }

  /**
   * Writes users to disk.
   */
  private void write() {
    try {
      final DataOutput out = new DataOutput(file);
      final int s = users.size();
      out.writeNum(s);
      for(int u = 0; u < s; u++) {
        final User user = users.get(u);
        out.writeString(user.name);
        out.writeBytes(user.pw);
        out.writeBool(user.read);
        out.writeBool(user.write);
        out.writeBool(user.create);
        out.writeBool(user.admin);
      }
      out.close();
    } catch(final Exception ex) {
      Main.debug(ex);
    }
  }

  /**
   * Returns information on all users.
   * @return String
   */
  public String info() {
    final int size = users.size();
    final TokenBuilder tb = new TokenBuilder();
    if(size != 0) {
      // get maximum column widths
      int[] ind = new int[5];
      for(final User u : users) ind[0] = Math.max(ind[0], u.name.length());
      for(int u = 0; u < USERS.length; u++) {
        ind[u] = Math.max(ind[u], USERS[u].length()) + 2;
        tb.add(ind[u], USERS[u]);
      }
      tb.add(NL);
      for(int u = 0; u < USERS.length; u++) {
        for(int i = 0; i < ind[u]; i++) tb.add('-');
      }
      tb.add(NL);
      for(int i = 0; i < size; i++) {
        final User user = users.get(i);
        tb.add(ind[0], user.name);
        tb.add(ind[1], user.read ? "X" : "");
        tb.add(ind[2], user.write ? "X" : "");
        tb.add(ind[3], user.create ? "X" : "");
        tb.add(ind[4], user.admin ? "X" : "");
        tb.add(NL);
      }
      tb.add(NL);
    }
    tb.add(size + " Users registered.");
    return tb.toString();
  }

  /**
   * Get list (to be removed).
   * @return list
   */
  public ArrayList<Object[]> getUsers() {
    return new ArrayList<Object[]>();
  }

  /**
   * Contains a single user.
   */
  static class User {
    /** User name. */
    String name;
    /** Password. */
    byte[] pw;
    /** Read permission. */
    boolean read;
    /** Write permission. */
    boolean write;
    /** Create permission. */
    boolean create;
    /** Admin rights. */
    boolean admin;
    
    @Override
    public boolean equals(final Object u) {
      return u instanceof User && ((User) u).name.equals(name);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }
  }
}
