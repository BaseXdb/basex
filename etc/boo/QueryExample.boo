/*
 * This example shows how queries can be executed in an iterative manner.
 * Documentation: http://basex.org/api
 *
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 */
namespace BaseXClient

import System
import System.IO

[module]
public class QueryExample:

  public static def Main(args as (string)):
    try:
      // create session
      session = Session('localhost', 1984, 'admin', 'admin')

      try:
        // create query instance 
        input as string = "for $i in 1 to 10 return <xml>Text { $i }</xml>"
        query as Query = session.Query(input)

        // loop through all results
        while query.More():
          Console.WriteLine(query.Next())

        // close query instance
        query.Close()

      except e as IOException:
        // print exception
        Console.WriteLine(e.Message)
        
      // close session
      session.Close()
      
    except e as IOException:
      // print exception
      Console.WriteLine(e.Message)

