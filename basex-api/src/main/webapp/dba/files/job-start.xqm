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
  %rest:path("/dba/job-start")
  %rest:query-param("file", "{$file}", "")
function dba:job-start(
  $file  as xs:string
) as element(rest:response) {
  cons:check(),
  let $name := replace($file, '\.\.+|/|\\', '')
  let $params := try {
    prof:void(jobs:invoke($cons:DBA-DIR || $name, (), map { 'cache': 'true', 'id': $file })),
    map { 'info': 'Job was started.' }
  } catch * {
    map { 'error': $err:description }
  }
  return web:redirect($dba:CAT, $params)
};
