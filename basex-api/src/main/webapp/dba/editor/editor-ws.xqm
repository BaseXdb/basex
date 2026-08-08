(:~
 : Evaluate queries of the editor via WebSockets.
 :
 : A query is not evaluated by the message handler itself. It is registered as a job, and a second
 : job (editor-eval.xq) waits for its outcome and pushes it to the client. Both steps return
 : immediately, so the connection stays responsive while a query runs, and no database locks are
 : acquired by the handler. As WebSocket URLs have their own address space, the connection is
 : guarded by an additional permission check (see login.xqm).
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/editor';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ WebSocket attribute: id of the running query job. :)
declare %private variable $dba:JOB := 'dba-job';

(:~
 : Handles a message of the editor.
 : @param  $message  message
 :)
declare
  %ws:message('/dba', '{$message}')
function dba:ws-message(
  $message  as xs:string
) as empty-sequence() {
  let $json := parse-json($message)
  return switch ($json?type) {
    case 'run'  return dba:ws-run($json?query, xs:integer($json?run), $json?indent = true())
    case 'stop' return (dba:ws-stop(), dba:ws-send({ 'type': 'stopped' }))
    default     return error((), 'Unknown message type: ' || $json?type)
  }
};

(:~
 : Reports an error of a handler, of the transport, or of the pushing job to the client.
 : @param  $message  error message
 :)
declare
  %ws:error('/dba', '{$message}')
function dba:ws-error(
  $message  as xs:string
) as empty-sequence() {
  admin:write-log('Editor: ' || $message, 'DBA'),
  dba:ws-send({ 'type': 'error', 'message': $message })
};

(:~
 : Registers a query and a job that pushes its outcome to the client.
 : @param  $query   query string
 : @param  $run     number of the run (echoed to the client, which drops outdated results)
 : @param  $indent  indent result
 :)
declare %private function dba:ws-run(
  $query   as xs:string,
  $run     as xs:integer,
  $indent  as xs:boolean
) as empty-sequence() {
  dba:ws-stop(),
  let $options := map:merge((utils:job-options(), {
    'cache': true(),
    'log'  : 'DBA query' || (config:edited-file()[.] ! (': ' || file:name(.)))
  }))
  let $id := job:eval($query, (), $options)
  return (
    ws:set(ws:id(), $dba:JOB, $id),
    void(ws:eval(xs:anyURI('editor-eval.xq'), {
      'id'       : $id,
      'run'      : $run,
      'serialize': utils:serialize(?, $indent)
    }, { 'serializer': { 'method': 'json' } }))
  )
};

(:~
 : Stops the query that is currently evaluated for this connection, and when the connection is
 : closed. The waiting job pushes an empty result, which the client drops, as it no longer waits
 : for this run.
 :)
declare
  %ws:close('/dba')
function dba:ws-stop() as empty-sequence() {
  let $id := ws:get(ws:id(), $dba:JOB)
  return if ($id) {
    ws:delete(ws:id(), $dba:JOB),
    job:remove($id)
  }
};

(:~
 : Sends a message to the client of the current connection.
 : @param  $message  message
 :)
declare %private function dba:ws-send(
  $message  as map(*)
) as empty-sequence() {
  ws:send(serialize($message, { 'method': 'json' }), ws:id())
};
