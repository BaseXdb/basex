' Visual Basic client for BaseX.
' Works with BaseX 7.x (but not with BaseX 8.0 and later)
' Does not support all bindings yet; your extensions are welcome.
'
' Documentation: http://docs.basex.org/wiki/Clients
'
' (C) BaseX Team 2005-12, BSD License

Imports System
Imports System.Net.Sockets
Imports System.Security.Cryptography
Imports System.Text
Imports System.Collections.Generic
Imports System.IO

Module BaseXClient
  Class Session	
    Private cache As Byte() = New Byte(4096) {}
    Public stream As NetworkStream
    Private socket As TcpClient
    Private m_info As String = ""
    Private bpos As Integer
    Private bsize As Integer
    
    Public Sub New(host As String, port As Integer, username As String, pw As String)
      socket = New TcpClient(host, port)
      stream = socket.GetStream()
      Dim ts As String = Receive()
      Send(username)
      Dim tmp As String = MD5StringHash(pw)
      Send(MD5StringHash(tmp & ts))
      If stream.ReadByte() <> 0 Then
        Throw New IOException("Access denied.")
      End If	
    End Sub
    
    Public Sub Execute(com As String, ms As Stream)
      Send(com)
      Init()
      Receive(ms)
      m_info = Receive()
      If Not Ok() Then
        Throw New IOException(m_info)
      End If
    End Sub

    Public Function Execute(com As String) As [String]
      Dim ms As New MemoryStream()
      Execute(com, ms)
      Return System.Text.Encoding.UTF8.GetString(ms.ToArray())
    End Function
    
    Public Sub Create(name As String, ms As Stream)
      stream.WriteByte(8)
      Send(name)
      While True:
      	Dim t As Integer = ms.ReadByte()
      	If t = -1 Then
      		Exit While
      	End If
      	stream.WriteByte(Convert.ToByte(t))
      End While
      stream.WriteByte(0)
      m_info = Receive()
      If Not Ok() Then
        Throw New IOException(m_info)
      End If
    End Sub
    
    Public Sub Create(name As String, target As String, ms As Stream)
      stream.WriteByte(9)
      Send(name)
      Send(target)
      While True:
      	Dim t As Integer = ms.ReadByte()
      	If t = -1 Then
      		Exit While
      	End If
      	stream.WriteByte(Convert.ToByte(t))
      End While
      stream.WriteByte(0)
      m_info = Receive()
      If Not Ok() Then
        Throw New IOException(m_info)
      End If
    End Sub

    Public Function Query(q As String) As Query
      Return New Query(Me, q)
    End Function

    Public ReadOnly Property Info() As String
      Get
        Return m_info
      End Get
    End Property

    Public Sub Close()
      Send("exit")
      socket.Close()
    End Sub

    ' Initializes the byte transfer. 
    Private Sub Init()
      bpos = 0
      bsize = 0
    End Sub

    ' Returns a single byte from the socket. 
    Private Function Read() As Byte
      If bpos = bsize Then
        bsize = stream.Read(cache, 0, 4096)
        bpos = 0
      End If
      Dim b as Byte = cache(bpos)
      bpos += 1
      Return b
    End Function

    ' Receives a string from the socket. 
    Private Sub Receive(ms As Stream)
      While True
        Dim b As Byte = Read()
        If b = 0 Then
          Exit While
        End If
        ms.WriteByte(b)
      End While
    End Sub

    ' Receives a string from the socket. 
    Public Function Receive() As String
      Dim ms As New MemoryStream()
      Receive(ms)
      Return System.Text.Encoding.UTF8.GetString(ms.ToArray())
    End Function

    ' Sends strings to server. 
    Public Sub Send(message As String)
      Dim msg As Byte() = System.Text.Encoding.UTF8.GetBytes(message)
      stream.Write(msg, 0, msg.Length)
      stream.WriteByte(0)
    End Sub

    ' Returns success check. 
    Public Function Ok() As Boolean
      Return Read() = 0
    End Function

    ' Returns the md5 hash of a string. 
  Private Function MD5StringHash(ByVal strString As String) As String
    Dim MD5 As New MD5CryptoServiceProvider
    Dim Data As Byte()
    Dim Result As Byte()
    Dim Res As String = ""
    Dim Tmp As String = ""

    Data = Encoding.ASCII.GetBytes(strString)
    Result = MD5.ComputeHash(Data)
    For i As Integer = 0 To Result.Length - 1
        Tmp = Hex(Result(i))
        If Len(Tmp) = 1 Then Tmp = "0" & Tmp
        Res += Tmp
    Next
    Return Res.ToLower
  End Function
End Class

  Class Query
    Private session As Session
    Private id As String

    Public Sub New(s As Session, query As String)
      session = s
      id = Exec(0, query)
    End Sub
      
    Public Sub Bind(name As String, value As String)
      Bind(name, value, "")
    End Sub

    Public Sub Bind(name As String, value As String, type As String)
      session.stream.WriteByte(3)
      session.Send(id)
      session.Send(name)
      session.Send(value)
      session.Send(type)
      Dim Res As String = session.Receive()
      If Not session.Ok() Then
        Throw New IOException(session.Receive())
      End If  
    End Sub

    Public Sub Context(value As String)
      Context(value, "")
    End Sub

    Public Sub Context(value As String, type As String)
      session.stream.WriteByte(14)
      session.Send(id)
      session.Send(value)
      session.Send(type)
      Dim Res As String = session.Receive()
      If Not session.Ok() Then
        Throw New IOException(session.Receive())
      End If  
    End Sub

    Public Function Execute() As String
      Return Exec(5, id)
    End Function

    Public Function Info() As String
      Return Exec(6, id)
    End Function

    Public Function Close()
      Exec(2, id)
    End Function
    
    Public Function Exec(cmd As Integer, arg As String) As String
      session.stream.WriteByte(cmd)
      session.Send(arg)
      Dim Res As String = session.Receive()
      If Not session.Ok() Then
        Throw New IOException(session.Receive())
      End If
      Return Res
    End Function
          
  End Class
End Module
