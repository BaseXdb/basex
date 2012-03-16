/*
 * Language Binding for BaseX.
 * Works with BaseX 7.0 and later
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-12, BSD License
 */
using System;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using System.IO;
using System.Threading;
using System.Collections;
using System.Collections.Generic;

namespace BaseXClient
{
  class Session
  {
    private byte[] cache = new byte[4096];
    public NetworkStream stream;
    private TcpClient socket;
    private string info = "";
    private string ehost;
    private int bpos;
    private int bsize;
    private TcpClient esocket;
    private NetworkStream estream;
    private Dictionary<string, EventNotification> en;

    /** see readme.txt */
    public Session(string host, int port, string username, string pw)
    {
      socket = new TcpClient(host, port);
      stream = socket.GetStream();
      ehost = host;
      string ts = Receive();
      Send(username);
      Send(MD5(MD5(pw) + ts));
      if (stream.ReadByte() != 0)
      {
        throw new IOException("Access denied.");
      }
    }

    /** see readme.txt */
    public void Execute(string com, Stream ms)
    {
      Send(com);
      Init();
      Receive(ms);
      info = Receive();
      if(!Ok())
      {
        throw new IOException(info);
      }
    }

    /** see readme.txt */
    public String Execute(string com)
    {
      MemoryStream ms = new MemoryStream();
      Execute(com, ms);
      return System.Text.Encoding.UTF8.GetString(ms.ToArray());
    }

    /** see readme.txt */
    public Query Query(string q)
    {
      return new Query(this, q);
    }

    /** see readme.txt */
    public void Create(string name, Stream s)
    {
      stream.WriteByte(8);
      Send(name);
      Send(s);
    }
    
    /** see readme.txt */
    public void Add(string path, Stream s)
    {
      stream.WriteByte(9);
      Send(path);
      Send(s);
    }
    
    /** see readme.txt */
    public void Replace(string path, Stream s)
    {
      stream.WriteByte(12);
      Send(path);
      Send(s);
    }
    
    /** see readme.txt */
    public void Store(string path, Stream s)
    {
      stream.WriteByte(13);
      Send(path);
      Send(s);
    }
    
    /* Watches an event. */
    public void Watch(string name, EventNotification notify)
    {
      stream.WriteByte(10);
      if(esocket == null)
      {    
        int eport = Convert.ToInt32(Receive());
        en = new Dictionary<string, EventNotification>();
        esocket = new TcpClient(ehost, eport);
        estream = esocket.GetStream();
        string id = Receive();
        byte[] msg = System.Text.Encoding.UTF8.GetBytes(id);
        estream.Write(msg, 0, msg.Length);
        estream.WriteByte(0);
        estream.ReadByte();
        new Thread(Listen).Start();
      }
      Send(name);
      info = Receive();
      if(!Ok())
      {
        throw new IOException(info);
      }
      en.Add(name, notify);
    }
    
    /** Listens to event socket */
    private void Listen() 
    {
      while (true)
      {
        String name = readS();
        String val = readS();
        en[name].Update(val);
      }
    }
    
    /** Returns event message */
    private string readS() 
    {
      MemoryStream ms = new MemoryStream();
      while (true) 
      {
        int b = estream.ReadByte();
        if (b == 0) break;
        ms.WriteByte((byte) b);
      }
      return System.Text.Encoding.UTF8.GetString(ms.ToArray()); 
    }
    
    /* Unwatches an event. */
    public void Unwatch(string name)
    {
      stream.WriteByte(11);
      Send(name);
      info = Receive();
      if(!Ok())
      {
        throw new IOException(info);
      }
      en.Remove(name);
    }

    /** see readme.txt */
    public string Info
    {
      get
      {
        return info;
      }
    }

    /** see readme.txt */
    public void Close()
    {
      Send("exit");
      if (esocket != null) 
      {
        esocket.Close();
      }
      socket.Close();
    }

    /** Initializes the byte transfer. */
    private void Init()
    {
      bpos = 0;
      bsize = 0;
    }

    /** Returns a single byte from the socket. */
    public byte Read()
    {
      if (bpos == bsize)
      {
        bsize = stream.Read(cache, 0, 4096);
        bpos = 0;
      }
      return cache[bpos++];
    }

    /** Receives a string from the socket. */
    private void Receive(Stream ms)
    {
      while (true)
      {
        byte b = Read();
        if (b == 0) break;
        ms.WriteByte(b);
      }
    }

    /** Receives a string from the socket. */
    public string Receive()
    {
      MemoryStream ms = new MemoryStream();
      Receive(ms);
      return System.Text.Encoding.UTF8.GetString(ms.ToArray());
    }

    /** Sends strings to server. */
    public void Send(string message)
    {
      byte[] msg = System.Text.Encoding.UTF8.GetBytes(message);
      stream.Write(msg, 0, msg.Length);
      stream.WriteByte(0);
    }

    /** see readme.txt */
    private void Send(Stream s)
    {
      while (true)
      {
          int t = s.ReadByte();
          if (t == -1) break;
          if (t == 0x00 || t == 0xFF) stream.WriteByte(Convert.ToByte(0xFF));
          stream.WriteByte(Convert.ToByte(t));
      }
      stream.WriteByte(0);
      info = Receive();
      if(!Ok())
      {
        throw new IOException(info);
      }
    }
    

    /** Returns success check. */
    public bool Ok()
    {
      return Read() == 0;
    }

    /** Returns the md5 hash of a string. */
    private string MD5(string input)
    {
      MD5CryptoServiceProvider MD5 = new MD5CryptoServiceProvider();
      byte[] hash = MD5.ComputeHash(Encoding.UTF8.GetBytes(input));

      StringBuilder sb = new StringBuilder();
      foreach (byte h in hash)
      {
        sb.Append(h.ToString("x2"));
      }
      return sb.ToString();
    }
  }

  class Query
  {
    private Session session;
    private string id;
    private ArrayList cache;
    private int pos;

    /** see readme.txt */
    public Query(Session s, string query)
    {
      pos = 0;
      session = s;
      id = Exec(0, query);
    }

    /** see readme.txt */
    public void Bind(string name, string value)
    {
      Bind(name, value, "");
    }

    /** see readme.txt */
    public void Bind(string name, string value, string type)
    {
      Exec(3, id + '\0' + name + '\0' + value + '\0' + type);
    }

    /** see readme.txt */
    public void Context(string value)
    {
      Context(value, "");
    }

    /** see readme.txt */
    public void Context(string value, string type)
    {
      Exec(14, id + '\0' + value + '\0' + type);
    }

    /** see readme.txt */
    public bool More()
    {
      if(cache == null) 
      {
        session.stream.WriteByte(4);
        session.Send(id);
        cache = new ArrayList();
        while (session.Read() > 0)
        {
          cache.Add(session.Receive());
        }
        if(!session.Ok())
        {
          throw new IOException(session.Receive());
        }
      }
      return pos < cache.Count;
    }

    /** see readme.txt */
    public string Next()
    {
      if(More()) 
      {
        return cache[pos++] as string;
      }
      else
      {
        return null;
      }
    }

    /** see readme.txt */
    public string Execute()
    {
      return Exec(5, id);
    }

    /** see readme.txt */
    public string Info()
    {
      return Exec(6, id);
    }

    /** see readme.txt */
    public string Options()
    {
      return Exec(7, id);
    }

    /** see readme.txt */
    public void Close()
    {
      Exec(2, id);
    }

    /** see readme.txt */
    private string Exec(byte cmd, string arg)
    {
      session.stream.WriteByte(cmd);
      session.Send(arg);
      string s = session.Receive();
    bool ok = session.Ok();
      if(!ok)
      {
        throw new IOException(session.Receive());
      }
      return s;
    }
  }
    
  interface EventNotification 
    {
        void Update(string data);
    }
}