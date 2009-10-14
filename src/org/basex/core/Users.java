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
  /** Default permissions. */
  private static final int PERM = User.READ | User.WRITE;
  /** User list. */
  private final ArrayList<User> users = new ArrayList<User>();
  /** Filename. */
  private final String file;
  /** Crypter. */
  private final Crypter crypt = new Crypter();

  /**
   * Standard constructor.
   */
  public Users() {
    file = Prop.HOME + ".basexperm";
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
    if(get(usern) != null) return false;

    final User user = new User(usern, crypt.encrypt(Token.token(pass)), PERM);
    users.add(user);
    write();
    return true;
  }

  /**
   * Drops a user from the list.
   * @param usern user name
   * @return success of operation
   */
  public boolean drop(final String usern) {
    final User user = get(usern);
    if(user == null) return false;
    users.remove(user);
    write();
    return true;
  }

  /**
   * Returns a user reference if the name/password combination is correct.
   * @param usern user name 
   * @param pw password
   * @return success of operation
   */
  public User get(final String usern, final String pw) {
    final User user = get(usern);
    return user != null && Token.eq(crypt.encrypt(Token.token(pw)), user.pw) ?
      user : null;
  }

  /**
   * Returns a user reference with the specified name.
   * @param usern user name
   * @return success of operation
   */
  public User get(final String usern) {
    for(final User user : users) if(user.name.equals(usern)) return user;
    return null;
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
          final User user = new User(in.readString(), in.readBytes(),
              in.readNum());
          users.add(user);
        }
      } catch(final Exception ex) {
        Main.debug(ex);
      }
    } else {
      // create admin user with all rights
      users.add(new User(ADMIN, crypt.encrypt(Token.token(ADMIN)),
          User.READ | User.WRITE | User.CREATE | User.ADMIN));
      write();
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
        out.writeNum(user.perm);
      }
      out.close();
    } catch(final Exception ex) {
      Main.debug(ex);
    }
  }

  /**
   * Returns information on all users.
   * @return user information
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
        tb.add(ind[1], user.perm(User.READ) ? "X" : "");
        tb.add(ind[2], user.perm(User.WRITE) ? "X" : "");
        tb.add(ind[3], user.perm(User.CREATE) ? "X" : "");
        tb.add(ind[4], user.perm(User.ADMIN) ? "X" : "");
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
}
