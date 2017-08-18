(:~
 : Evaluates a file.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/files';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Evaluates a file.
 : @param  $file  file name
 : @return redirection
 :)
declare
  %rest:GET
  %rest:path("/dba/file-eval")
  %rest:query-param("file", "{$file}", "")
function dba:file-eval(
  $file  as xs:string
) as item()+ {
  cons:check(),

  let $name := replace($file, '\.\.+|/|\\', '')
  let $params := try {
    let $id := jobs:invoke($cons:DBA-DIR || $name, (), map { 'cache': 'true', 'id': $file })
    return map { 'info': 'Job "' || $id || '" started.' }
  } catch * {
    map { 'error': 'Could not start ' || $name || '.' }
  }
  return web:redirect($dba:CAT, $params)
};
