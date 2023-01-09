(:~
 : Simple WebSocket chat. Utility functions.
 : @author BaseX Team 2005-23, BSD License
 :)
module namespace chat-util = 'chat/util';

import module namespace session = 'http://basex.org/modules/session';
import module namespace ws = 'http://basex.org/modules/ws';

(:~ User id (bound to sessions and WebSockets). :)
declare variable $chat-util:id := 'id';

(:~
 : Sends a users list (all, active) to all registered clients.
 :)
declare function chat-util:users() as empty-sequence() {
  ws:emit(map {
    'type': 'users',
    'users': array { sort(user:list()) },
    'active': array { distinct-values(
      for $id in ws:ids()
      return ws:get($id, $chat-util:id)
    )}
  })
};

(:~ 
 : Sends a message to all clients, or to the clients of a specific user.
 : @param  $text  text to be sent
 : @param  $to    receiver of a private message (optional)
 :)
declare function chat-util:message(
  $text  as xs:string,
  $to    as xs:string?
) as empty-sequence() {
  let $ws-ids := ws:ids()[not($to) or ws:get(., $chat-util:id) = $to]
  return ws:send(map {
    'type': 'message',
    'text': serialize($text),
    'from': ws:get(ws:id(), $chat-util:id),
    'date': format-time(current-time(), '[H02]:[m02]:[s02]'),
    'private': boolean($to)
  }, $ws-ids)
};

(:~
 : Closes all WebSocket connections from the specified user.
 : @param  $name  username
 :)
declare function chat-util:close(
  $name  as  xs:string
) as empty-sequence() {
  for $id in ws:ids()
  where ws:get($id, $chat-util:id) = $name
  return ws:close($id)
};
