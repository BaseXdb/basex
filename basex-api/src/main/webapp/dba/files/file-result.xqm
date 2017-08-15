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
 : Stops a running job.
 : @param  $id    job id
 : @param  $file  file name
 : @return redirection
 :)
declare
  %rest:GET
  %rest:path("/dba/file-result/{$file}")
  %rest:query-param("id", "{$id}", "")
function dba:file-result(
  $id    as xs:string,
  $file  as xs:string
) as item()+ {
  cons:check(),
  web:response-header(map { }, map { 'Cache-Control': '' }),
  try {
    jobs:result($id)
  } catch * {
    'Stopped at ' || $err:module || ', ' || $err:line-number || '/' ||
      $err:column-number || ':' || out:nl() || $err:description
  }
};
