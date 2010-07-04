/*
 * This example shows how database commands can be executed.
 * Documentation: http://basex.org/api
 *
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 */
namespace BaseXClient

import System
import System.Diagnostics
import System.IO

public class Example:
  [STAThread]
  public static def Main(args as (string)):
    try:
      // initialize timer
      watch = Stopwatch()
      watch.Start()

      // create session
      session = Session('localhost', 1984, 'admin', 'admin')
      
      // version 1: perform command and print returned string
      Console.WriteLine(session.Execute("info"))
      
      // version 2 (faster): perform command and pass on result to output stream
      stream as Stream = Console.OpenStandardOutput()
      session.Execute("xquery 1 to 10", stream):
        
      // close session
      session.Close()
      
      // print time needed
      Console.WriteLine((('\n' + watch.ElapsedMilliseconds) + ' ms.'))

    except e as IOException:
      // print exception
      Console.WriteLine(e.Message)
