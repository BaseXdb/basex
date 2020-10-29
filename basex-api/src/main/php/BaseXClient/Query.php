<?php
/*
 * PHP client for BaseX.
 * Works with BaseX 7.0 and later
 *
 * Documentation: https://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-15, BSD License
 */

namespace BaseXClient;

class Query implements \Iterator
{
    protected $session;
    protected $id;
    protected $cache;
    protected $pos;

    /**
     * Query constructor.
     *
     * @param Session $session
     * @param string $query
     */
    public function __construct($session, $query)
    {
        $this->session = $session;
        $this->id = $this->exec(chr(0), $query);
    }

    public function bind($name, $value, $type = "")
    {
        $this->exec(chr(3), $this->id.chr(0).$name.chr(0).$value.chr(0).$type);
    }

    public function context($value, $type = "")
    {
        $this->exec(chr(14), $this->id.chr(0).$value.chr(0).$type);
    }

    public function execute()
    {
        return $this->exec(chr(5), $this->id);
    }

    public function more()
    {
        if ($this->cache === null) {
            $this->pos = 0;
            $this->session->send(chr(4).$this->id.chr(0));
            while (!$this->session->ok()) {
                $this->cache[] = $this->session->readString();
            }
            if (!$this->session->ok()) {
                throw new BaseXException($this->session->readString());
            }
        }
        if ($this->pos < count($this->cache)) {
            return true;
        }
        $this->cache = null;
        return false;
    }

    public function next()
    {
        if ($this->more()) {
            return $this->cache[$this->pos++];
        }
    }

    public function info()
    {
        return $this->exec(chr(6), $this->id);
    }

    public function options()
    {
        return $this->exec(chr(7), $this->id);
    }

    public function close()
    {
        $this->exec(chr(2), $this->id);
    }

    public function exec($cmd, $arg)
    {
        $this->session->send($cmd.$arg);
        $s = $this->session->receive();
        if ($this->session->ok() !== true) {
            throw new BaseXException($this->session->readString());
        }
        return $s;
    }

    public function current()
    {
        return $this->cache[$this->pos];
    }

    public function key()
    {
        return $this->pos;
    }

    public function valid()
    {
        return $this->more();
    }

    public function rewind()
    {
    }
}
