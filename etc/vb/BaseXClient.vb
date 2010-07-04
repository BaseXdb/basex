' Language Binding for BaseX.
' Works with BaseX 6.1.9 and later
' Documentation: http://basex.org/api
'
' (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License

Imports System
Imports System.Net.Sockets
Imports System.Security.Cryptography
Imports System.Text
Imports System.Collections.Generic
Imports System.IO

Namespace BaseXClient
  Class Session
    Private cache As Byte() = New Byte(4096) {}
    Public stream As NetworkStream
    Private socket As TcpClient
    Private m_info As String = ""
    Private bpos As Integer
    Private bsize As Integer

    '* see readme.txt
    Public Sub New(host As String, port As Integer, username As String, pw As String)
      socket = New TcpClient(host, port)
      stream = socket.GetStream()
      Dim ts As String = Receive()
      Send(username)
      Send(MD5(MD5(pw) & ts))
      If stream.ReadByte() <> 0 Then
        Throw New IOException("Access denied.")
      End If
    End Sub

    '* see readme.txt
    Public Function Execute(com As String, ms As Stream)
      Send(com)
      Init()
      Receive(ms)
      m_info = Receive()
      If Not Ok() Then
        Throw New IOException(m_info)
      End If
    End Function

    '* see readme.txt
    Public Function Execute(com As String) As [String]
      Dim ms As New MemoryStream()
      Execute(com, ms)
      Return System.Text.Encoding.UTF8.GetString(ms.ToArray())
    End Function

    '* see readme.txt
    Public Function Query(q As String) As Query
      Return New Query(Me, q)
    End Function

    '* see readme.txt
    Public ReadOnly Property Info() As String
      Get
        Return m_info
      End Get
    End Property

    '* see readme.txt 
    Public Sub Close()
      Send("exit")
      socket.Close()
    End Sub

    '* Initializes the byte transfer. 
    Private Sub Init()
      bpos = 0
      bsize = 0
    End Sub

    '* Returns a single byte from the socket. 
    Private Function Read() As Byte
      If bpos = bsize Then
        bsize = stream.Read(cache, 0, 4096)
        bpos = 0
      End If
      Return cache(bpos++)
    End Function

    '* Receives a string from the socket. 
    Private Sub Receive(ms As Stream)
      While True
        Dim b As Byte = Read()
        If b = 0 Then
          Exit While
        End If
        ms.WriteByte(b)
      End While
    End Sub

    '* Receives a string from the socket. 
    Public Function Receive() As String
      Dim ms As New MemoryStream()
      Receive(ms)
      Return System.Text.Encoding.UTF8.GetString(ms.ToArray())
    End Function

    '* Sends strings to server. 
    Public Sub Send(message As String)
      Dim msg As Byte() = System.Text.Encoding.UTF8.GetBytes(message)
      stream.Write(msg, 0, msg.Length)
      stream.WriteByte(0)
    End Sub

    '* Returns success check. 
    Public Function Ok() As Boolean
      Return Read() = 0
    End Function

    '* Returns the md5 hash of a string. 
    Private Function MD5(input As String) As String
      Dim MD5 As New MD5CryptoServiceProvider()
      Dim hash As Byte() = MD5.ComputeHash(Encoding.UTF8.GetBytes(input))

      Dim sb As New StringBuilder()
      For Each h As Byte In hash
        sb.Append(h.ToString("x2"))
      Next
      Return sb.ToString()
    End Function
  End Class

  Class Query
    Private session As Session
    Private id As String
    Private nextItem As String

    '* see readme.txt
    Public Sub New(s As Session, query As String)
      session = s
      session.stream.WriteByte(0)
      session.Send(query)
      id = session.Receive()
      If Not session.Ok() Then
        Throw New IOException(session.Receive())
      End If
    End Sub

    '* see readme.txt 
    Public Function More() As Boolean
      session.stream.WriteByte(1)
      session.Send(id)
      nextItem = session.Receive()
      If Not session.Ok() Then
        Throw New IOException(session.Receive())
      End If
      Return nextItem.Length <> 0
    End Function

    '* see readme.txt
    Public Function Next() As String
      Return nextItem
    End Function

    '* see readme.txt 
    Public Sub Close()
      session.stream.WriteByte(2)
      session.Send(id)
    End Sub
  End Class
End Namespace
