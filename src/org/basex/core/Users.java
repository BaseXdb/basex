package org.basex.core;

import static org.basex.core.Text.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.basex.util.Crypter;
import org.basex.util.TokenBuilder;

/**
 * This class organizes all users.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class Users {
  /** User list. */
  private final ArrayList<Object[]> users;
  /** Filename. */
  private final String file;
  /** Crypter. */
  private Crypter crypt = new Crypter();

  /**
   * Standard constructor.
   * @param ctx Context
   */
  public Users(final Context ctx) {
    file = ctx.prop.get(Prop.DBPATH) + "\\user.basex";
    users = getList();
  }
  
  /**
   * Strores a user and encrypted password.
   * @param usern Username
   * @param pass Password
   * @return success of operation
   */
  public boolean createUser(final String usern, final String pass) {
    // username, read, write, create, admin, password
    final Object[] item =
      { usern, false, false, false, false, crypt.encrypt(pass)};
    boolean in = false;
    for(Object[] s : users) {
      if(s[0].equals(usern)) in = true;
    }
    if(!in) {
      users.add(item);
      return true;
    }
    return false;
  }

  /**
   * Drops a user from the list.
   * @param usern String
   * @return success of operation
   */
  public boolean dropUser(final String usern) {
    int i = 0;
    int t = 0;
    boolean found = false;
    for(Object[] s : users) {
      if(s[0].equals(usern)) {
        found = true;
        t = i;
      }
      i++;
    }
    if(found) {
      users.remove(t);
    }
    return found;
  }

  /**
   * Returns list with all users and passwords.
   * @return ArrayList
   */
  @SuppressWarnings("unchecked")
  ArrayList<Object[]> getList() {
    if(new File(file).exists()) {
      try {
        final ObjectInputStream in =
          new ObjectInputStream(new FileInputStream(file));
        return (ArrayList<Object[]>) in.readObject();
      } catch(final Exception e) {
        e.printStackTrace();
      }
    }
    return new ArrayList<Object[]>();
  }

  /**
   * Writes list to the file.
   */
  void writeList() {
    ObjectOutputStream out;
    try {
      out = new ObjectOutputStream(new FileOutputStream(file));
      out.writeObject(users);
      out.close();
    } catch(final Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Returns all users.
   * @return String
   */
  public String show() {
    final int size = users.size();
    final TokenBuilder tb = new TokenBuilder();
    tb.add("% Users", size);
    tb.add(size != 0 ? COL : DOT);
    for(int i = 0; i < size; i++) tb.add(NL + LI + users.get(i)[0]);
    return tb.toString();
  }
  
  /**
   * Returns information about all users.
   * @return String
   */
  public String info() {
    final int size = users.size();
    final TokenBuilder tb = new TokenBuilder();
    tb.add("% Users", size);
    tb.add(size != 0 ? COL : DOT);
    if(size != 0) tb.add(NL + "Username\tRead\tWrite\tAdmin\tCreate");
    for(int i = 0; i < size; i++) {
      tb.add(NL + users.get(i)[0] + "\t\t");
      for(int j = 1; j <= 4; j++) {
        if((Boolean) users.get(i)[j]) tb.add("  X\t");
        else tb.add("\t");
        }
      }
    return tb.toString();
  }
  
  /**
   * Returns the list of users.
   * @return userlist
   */
  public ArrayList<Object[]> getUsers() {
    return users;
  }
}
