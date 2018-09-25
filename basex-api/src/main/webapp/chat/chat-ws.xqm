module namespace chat = 'chat';

import module namespace chat-util = 'chat/util' at 'chat-util.xqm';

(:~ 
 : Processes a WebSocket message.
 : @param  $message  message
 :)
declare
  %ws:message('/chat', '{$message}')
function chat:ws-message(
  $message  as xs:string
) as empty-sequence() {
  let $json := parse-json($message)
  let $type := $json?type
  return if($type = 'message') then (
    chat-util:message($json?text, $json?to)
  ) else if($type = 'users') then (
    chat-util:users()
  ) else if($type = "ping") then(
    (: do nothing :)
  ) else error()
};

(:~ 
 : Creates a WebSocket connection: Adds the WebSocket id to the list of ids
 : that is stored in the session.
 :)
declare
  %ws:connect('/chat')
function chat:ws-connect() as empty-sequence() {
  let $ws-ids := session:get($chat-util:ws-ID)
  return session:set($chat-util:ws-ID, ($ws-ids, ws:id()))
};

(:~ 
 : Closes a WebSocket connection: Removes the WebSocket id from the list of ids
 : that is stored in the session.
 :)
declare
  %ws:close('/chat')
function chat:ws-close() as empty-sequence() {
  let $ws-ids := session:get($chat-util:ws-ID)
  return session:set($chat-util:ws-ID, $ws-ids[. != ws:id()]),
  chat-util:users()
};
