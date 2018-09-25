module namespace chat-util = 'chat/util';

import module namespace session = 'http://basex.org/modules/session';
import module namespace sessions = 'http://basex.org/modules/sessions';
import module namespace ws = "http://basex.org/modules/ws";

(:~ Session id. :)
declare variable $chat-util:session-ID := 'chat';
(:~ Id of the WebSocket chat instance :)
declare variable $chat-util:ws-ID := 'ws-id';

(:~
 : Sends a users list (all, active) to all registered clients.
 :)
declare function chat-util:users() as empty-sequence() {
  ws:emit(json:serialize(map {
    'type': 'users',
    'users': array { sort(user:list()) },
    'active': array { distinct-values(
      (: returns all logged in users who are currently connected to a chat :)
      for $id in sessions:ids()
      where exists(sessions:get($id, $chat-util:ws-ID))
      return sessions:get($id, $chat-util:session-ID)
    )}
  }))
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
  let $ws-ids := if($to) then (
    for $id in sessions:ids()
    where sessions:get($id, $chat-util:session-ID) = $to
    return sessions:get($id, $chat-util:ws-ID)
  ) else (
    ws:ids()
  )
  return ws:send(json:serialize(map {
    'type': 'message',
    'text': serialize($text),
    'from': session:get($chat-util:session-ID),
    'date': format-time(current-time(), '[H02]:[m02]:[s02]'),
    'private': boolean($to)
  }), $ws-ids)
};
