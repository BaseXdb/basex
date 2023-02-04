/*
 * This example shows how database commands can be executed.
 *
 * Documentation: https://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-23, BSD License
 */
using System;
using System.Diagnostics;
using System.IO;
using System.Threading;
using System.Threading.Tasks;

namespace BaseXClient
{
  public class Example
  {
    public static async Task Main(string[] args)
    {
      CancellationTokenSource cts = new CancellationTokenSource();
      await Demo(cts.Token);
    }
    
    private static async Task Demo(CancellationToken cancellationToken)
    {
      try
      {
        // initialize timer
        Stopwatch watch = new Stopwatch();
        watch.Start();

        // create session
        Session session = await Session.CreateAsync("localhost", 1984, "admin", "admin", cancellationToken);

        // version 1: perform command and print returned string
        Console.WriteLine(await session.ExecuteAsync("info", cancellationToken));

        // version 2 (faster): perform command and pass on result to output stream
        Stream stream = Console.OpenStandardOutput();
        await session.ExecuteAsync("xquery 1 to 10", stream, cancellationToken);

        // close session
        await session.CloseAsync(cancellationToken);

        // print time needed
        Console.WriteLine("\n" + watch.ElapsedMilliseconds + " ms.");
      }
      catch (IOException e)
      {
        // print exception
        Console.WriteLine(e.Message);
      }
    }
  }
}