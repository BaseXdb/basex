(:~
 : Download log file.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'logs';

(:~
 : Downloads database logs.
 : @param  $name   name (date) of log file
 : @param  $input  search input
 : @return binary data
 :)
declare
  %rest:POST
  %rest:path("/dba/log-download")
  %rest:query-param("name",  "{$name}")
  %rest:query-param("input", "{$input}")
  %output:method("html")
function dba:drop(
  $name   as xs:string,
  $input  as xs:string
) as element()+ {
  cons:check(),

  let $ext := if($input) then ('-' || web:encode-url($input)) else ()
  return web:response-header(
    map { 'media-type': 'text/xml' },
    map { 'Cache-Control': '',
          'Content-Disposition': 'attachment; filename=logs' || $name || $ext || '.xml'
    }
  ),
  element entries {
    admin:logs($name, true())[matches(., $input, 'i')]
  }
};
