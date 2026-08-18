(:~
 : WebSocket chat with rooms. Utility functions.
 :
 : These functions are used by both the web pages (chat.xqm) and the
 : WebSocket functions (chat-ws.xqm). They build on the ws module:
 :
 : - ws:ids() lists all open connections,
 : - ws:get() and ws:set() store extra information with a connection,
 : - ws:path() returns the path a connection was opened on (used here to
 :   find out which room a connection belongs to), and
 : - ws:send(), ws:emit() and ws:broadcast() push data to clients:
 :   ws:send() targets selected connections, ws:emit() reaches everyone,
 :   and ws:broadcast() reaches everyone except the current connection.
 :
 : The chat history is kept in the 'chat' store: one entry per room, and one
 : per private conversation (see chat-util:key).
 :
 : @author BaseX Team, BSD License
 :)
module namespace chat-util = 'chat/util';

import module namespace ws = 'http://basex.org/modules/ws';

(:~ Key under which the username is stored with each connection. :)
declare variable $chat-util:id := 'id';
(:~ Key under which the HTTP session is stored with each connection. :)
declare variable $chat-util:session := 'session';
(:~ WebSocket path prefix; the room name is appended (see chat-ws.xqm). :)
declare variable $chat-util:prefix := '/chat/';
(:~ Rooms offered on the page. The server accepts any room whose name
 : matches the pattern in the %ws annotations, so this list only drives
 : the buttons in the user interface (see chat.xqm). :)
declare variable $chat-util:rooms := ('lobby', 'tech', 'random');
(:~ Name of the store that keeps the chat history. :)
declare variable $chat-util:store := 'chat';

(:~
 : Returns the room a connection belongs to. It is derived from the path
 : the connection was opened on: ws:path('websocket3') = '/chat/lobby'.
 : @param  $id  connection id
 : @return room name
 :)
declare function chat-util:room(
  $id  as xs:string
) as xs:string {
  substring-after(ws:path($id), $chat-util:prefix)
};

(:~
 : Returns a room name with a capital first letter, for display. The room id
 : itself stays lower case: the path templates in chat-ws.xqm constrain room
 : names to [a-z0-9-]+, so every room has a single canonical (lower-case) id.
 : @param  $room  room name (lower case)
 : @return capitalised name
 :)
declare function chat-util:name(
  $room  as xs:string
) as xs:string {
  upper-case(substring($room, 1, 1)) || substring($room, 2)
};

(:~
 : Sends the list of users to all clients: everyone known to the server, with
 : the rooms they are connected to right now (a user may have one tab open per
 : room; no rooms means offline). The map is turned into JSON and read again
 : by the browser (chat.js).
 :)
declare function chat-util:users() as empty-sequence() {
  (: ws:emit sends the data to everyone who is connected :)
  ws:emit({
    'type': 'users',
    'users': array {
      for $name in sort(user:list())
      let $ids := ws:ids()[ws:get(., $chat-util:id) = $name]
      return {
        'name': $name,
        'rooms': array { sort(distinct-values($ids ! chat-util:room(.))) ! chat-util:name(.) }
      }
    }
  })
};

(:~
 : Returns the user of the current connection. ws:id() is that connection; the
 : name was stored with it when the client connected (see chat-ws:connect).
 : @return user name, empty if the connection has already been unregistered
 :)
declare function chat-util:user() as xs:string? {
  ws:get(ws:id(), $chat-util:id)
};

(:~
 : Returns the store key of a private conversation: the two user names, sorted
 : and separated by a slash. A room is stored under its own name; as a slash
 : occurs in neither kind of name, the two cannot collide.
 : @param  $user1  one user
 : @param  $user2  other user
 : @return store key
 :)
declare function chat-util:key(
  $user1  as xs:string,
  $user2  as xs:string
) as xs:string {
  string-join(sort(($user1, $user2)), '/')
};

(:~
 : Sends a chat message: to everyone in the given room, or privately to the
 : sender and the receiver (in whichever room they are). Every message is
 : added to the history of its conversation before it is delivered.
 : @param  $text  text to be sent
 : @param  $to    receiver of a private message (empty: public message)
 : @param  $room  room the message was sent from
 :)
declare function chat-util:message(
  $text  as xs:string,
  $to    as xs:string?,
  $room  as xs:string
) as empty-sequence() {
  (: the receiver is supplied by the client: a message to someone who is not a
   : registered user is ignored, it would only add an entry to the store :)
  if (empty($to) or $to = user:list()) {
    let $from := chat-util:user()
    let $key := if ($to) { chat-util:key($from, $to) } else { $room }
    let $message := { 'from': $from, 'text': $text, 'date': current-dateTime() }
    (: pick the target connections: both users for a private message,
     : otherwise everyone who is in the same room :)
    let $ids := if ($to) {
      ws:ids()[ws:get(., $chat-util:id) = ($from, $to)]
    } else {
      ws:ids()[chat-util:room(.) = $room]
    }
    return (
      store:put($key, (store:get($key, $chat-util:store), $message), $chat-util:store),
      (: the store is written to disk when the server shuts down; writing it
       : here as well keeps the history if the process is killed :)
      store:write($chat-util:store),
      ws:send(map:put(chat-util:entry($message, $key), 'type', 'message'), $ids)
    )
  }
};

(:~
 : Sends the stored messages of a conversation to the current connection.
 : @param  $key  store key
 :)
declare function chat-util:history(
  $key  as xs:string
) as empty-sequence() {
  ws:send({
    'type': 'history',
    'key': $key,
    'messages': array { store:get($key, $chat-util:store) ! chat-util:entry(., $key) }
  }, ws:id())
};

(:~
 : Turns a stored message into the map that is sent to the clients.
 : @param  $message  stored message
 : @param  $key      store key of the conversation
 : @return message map
 :)
declare %private function chat-util:entry(
  $message  as map(*),
  $key      as xs:string
) as map(*) {
  {
    'key': $key,
    'from': $message?from,
    (: serialize() replaces the characters <, > and &, so the text
     : cannot smuggle HTML into the page (see chat.js) :)
    'text': serialize($message?text),
    'date': format-dateTime($message?date, '[H02]:[m02]:[s02]'),
    'private': contains($key, '/')
  }
};

(:~
 : Sends a system line (a server notice, not a chat message) to the
 : specified connections.
 : @param  $text  notice text
 : @param  $ids   target connections
 :)
declare function chat-util:system(
  $text  as xs:string,
  $ids   as xs:string*
) as empty-sequence() {
  ws:send(chat-util:notice($text), $ids)
};

(:~
 : Sends a system line to every client except the current connection.
 : Used for join and leave notices: the one who triggers the event does
 : not need to be told about it (ws:broadcast leaves it out).
 : @param  $text  notice text
 :)
declare function chat-util:announce(
  $text  as xs:string
) as empty-sequence() {
  ws:broadcast(chat-util:notice($text))
};

(:~
 : Builds a system-line message.
 : @param  $text  notice text
 : @return message map
 :)
declare %private function chat-util:notice(
  $text  as xs:string
) as map(*) {
  {
    'type': 'system',
    (: the client shows a notice as text (see showInfo in chat.js), so it is
     : sent unescaped; escaping it would show the entities :)
    'text': $text,
    'date': format-time(current-time(), '[H02]:[m02]:[s02]')
  }
};

(:~
 : Closes the WebSocket connections of the current session. Other browsers can
 : be logged in with the same user; their connections stay open.
 :)
declare function chat-util:close() as empty-sequence() {
  let $session := session:id()
  for $id in ws:ids()
  where ws:get($id, $chat-util:session) = $session
  (: 1000 means: closed normally, nothing went wrong :)
  return ws:close($id, 1000, 'logout')
};
