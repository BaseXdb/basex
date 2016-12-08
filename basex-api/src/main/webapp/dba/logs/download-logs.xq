(:~
 : Download log file.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'logs';

(:~
 : Deletes database logs.
 : @param  $name  name of log files
 :)
declare
  %rest:POST
  %rest:path("/dba/download-logs")
  %rest:query-param("name", "{$name}")
  %rest:query-param("logs", "{$logs}")
  %output:method("html")
function dba:drop(
  $name  as xs:string,
  $logs  as xs:string
) as item()+ {
  cons:check(),
  try {
    let $entries := admin:logs($name, true())[matches(., $logs, 'i')]
    return (
      web:response-header(
        map { 'media-type': 'text/xml' },
        map { 'Content-Disposition': 'attachment; filename=logs-' || $name ||
          (if($logs) then ('-' || web:encode-url($logs)) else ()) || '.xml'
        }
      ),
      element entries { $entries }
    )
  } catch * {
    <rest:response>
      <http:response status="400" message="{ $err:description }"/>
    </rest:response>
  }
};
