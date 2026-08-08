(:~
 : Query resources.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace utils = 'dba/utils' at '../../lib/utils.xqm';

(:~
 : Runs a query on a database resource and sends the result to the client.
 : @param  $message  message
 :)
declare
  %ws:message('/dba/db-query', '{$message}')
function dba:ws-message(
  $message  as xs:string
) as empty-sequence() {
  let $json := parse-json($message)
  let $run := xs:integer($json?run)
  return (
    (: a query on a large resource takes time: stop one that is superseded by this one :)
    utils:ws-stop(),
    let $id := job:eval(xs:anyURI('db-query-eval.xq'), {
      'name'    : $json?name,
      'resource': $json?resource,
      'query'   : $json?query
    }, map:put(utils:job-options(), 'cache', true()))
    return utils:ws-start($id, $run, utils:serialize-options($json?indent = true()))
  )
};

(:~
 : Stops a running query if the connection is closed.
 :)
declare
  %ws:close('/dba/db-query')
function dba:ws-close() as empty-sequence() {
  utils:ws-stop()
};

(:~
 : Reports an error to the client.
 : @param  $message  error message
 :)
declare
  %ws:error('/dba/db-query', '{$message}')
function dba:ws-error(
  $message  as xs:string
) as empty-sequence() {
  utils:ws-error('Databases', $message)
};
