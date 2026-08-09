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
    let $id := job:eval(fn(
      $name      as xs:string,
      $resource  as xs:string,
      $query     as xs:string
    ) {
      let $type := db:type($name, $resource)
      let $context := head(if ($type = 'xml') {
        db:get($name, $resource)
      } else if ($type = 'binary') {
        db:get-binary($name, $resource)
      } else {
        db:get-value($name, $resource)
      })
      return xquery:eval($query, { '': $context }, { 'pass': true() })
    }, [ $json?name, $json?resource, $json?query ], utils:job-options())
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
