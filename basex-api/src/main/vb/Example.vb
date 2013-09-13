' This example shows how database commands can be executed.
'
' Documentation: http://docs.basex.org/wiki/Clients
'
' (C) BaseX Team 2005-12, BSD License

Imports System
Imports System.Diagnostics
Imports System.IO

Module Example
  Sub Main()
    Try
      ' initialize timer
      Dim watch As New Stopwatch()
      watch.Start()

      ' create session
      Dim session As New Session("localhost", 1984, "admin", "admin")
 
      ' version 1: perform command and print returned string
      Console.WriteLine(session.Execute("info"))

      ' version 2 (faster): perform command and pass on result to output stream
      Dim stream As Stream = Console.OpenStandardOutput()
      session.Execute("xquery 1 to 10", stream)

      ' close session
      session.Close()

      ' print time needed
      Console.WriteLine(vbLf & watch.ElapsedMilliseconds & " ms.")
      
    Catch e As IOException
      ' print exception
      Console.WriteLine(e.Message)
      Console.ReadLine()
    End Try
  End Sub
End Module