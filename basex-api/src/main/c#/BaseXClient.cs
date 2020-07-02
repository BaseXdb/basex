/*
 * Language Binding for BaseX.
 * Works with BaseX 7.0 and later
 *
 * Documentation: https://docs.basex.org/wiki/Clients
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
  public class Session
  {
    private byte[] cache = new byte[4096];
    public NetworkStream stream;
    private TcpClient socket;
    private string info = "";
    private int bpos;
    private int bsize;

    public Session(string host, int port, string username, string pw)
    {
      socket = new TcpClient(host, port);
      stream = socket.GetStream();
      string[] response = Receive().Split(':');

      string nonce;
      string code;
      if (response.Length > 1)
      {
          code = username + ":" + response[0] + ":" + pw;
          nonce = response[1];
      }
      else
      {
          code = pw;
          nonce = response[0];
      }

      Send(username);
      Send(MD5(MD5(code) + nonce));
      if (stream.ReadByte() != 0)
      {
          throw new IOException("Access denied.");
      }
    }

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

    public String Execute(string com)
    {
      MemoryStream ms = new MemoryStream();
      Execute(com, ms);
      return System.Text.Encoding.UTF8.GetString(ms.ToArray());
    }

    public Query Query(string q)
    {
      return new Query(this, q);
    }

    public void Create(string name, Stream s)
    {
      stream.WriteByte(8);
      Send(name);
      Send(s);
    }
    
    public void Add(string path, Stream s)
    {
      stream.WriteByte(9);
      Send(path);
      Send(s);
    }
    
    public void Replace(string path, Stream s)
    {
      stream.WriteByte(12);
      Send(path);
      Send(s);
    }
    
    public void Store(string path, Stream s)
    {
      stream.WriteByte(13);
      Send(path);
      Send(s);
    }

    public string Info
    {
      get
      {
        return info;
      }
    }

    public void Close()
    {
      Send("exit");
      socket.Close();
    }

    private void Init()
    {
      bpos = 0;
      bsize = 0;
    }

    public byte Read()
    {
      if (bpos == bsize)
      {
        bsize = stream.Read(cache, 0, 4096);
        bpos = 0;
      }
      return cache[bpos++];
    }

    private void Receive(Stream ms)
    {
      while (true)
      {
        byte b = Read();
        if (b == 0) break;
        // read next byte if 0xFF is received
        ms.WriteByte(b == 0xFF ? Read() : b);
      }
    }

    public string Receive()
    {
      MemoryStream ms = new MemoryStream();
      Receive(ms);
      return System.Text.Encoding.UTF8.GetString(ms.ToArray());
    }

    public void Send(string message)
    {
      byte[] msg = System.Text.Encoding.UTF8.GetBytes(message);
      stream.Write(msg, 0, msg.Length);
      stream.WriteByte(0);
    }

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
    

    public bool Ok()
    {
      return Read() == 0;
    }

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

  public class Query
  {
    private Session session;
    private string id;
    private ArrayList cache;
    private int pos;

    public Query(Session s, string query)
    {
      session = s;
      id = Exec(0, query);
    }

    public void Bind(string name, string value)
    {
      Bind(name, value, "");
    }

    public void Bind(string name, string value, string type)
    {
      cache = null;
      Exec(3, id + '\0' + name + '\0' + value + '\0' + type);
    }

    public void Context(string value)
    {
      Context(value, "");
    }

    public void Context(string value, string type)
    {
      cache = null;
      Exec(14, id + '\0' + value + '\0' + type);
    }

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
        pos = 0;
      }
      if(pos < cache.Count) return true;
      cache = null;
      return false;
    }

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

    public string Execute()
    {
      return Exec(5, id);
    }

    public string Info()
    {
      return Exec(6, id);
    }

    public string Options()
    {
      return Exec(7, id);
    }

    public void Close()
    {
      Exec(2, id);
    }

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
}
