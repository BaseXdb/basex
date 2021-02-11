(:~
 : Download log file.
 :
 : @author Christian Grün, BaseX Team 2005-21, BSD License
 :)
module namespace dba = 'dba/logs';

(:~ Top category :)
declare variable $dba:CAT := 'logs';

(:~
 : Downloads database logs.
 : @param  $name  name (date) of log file
 : @return binary data
 :)
declare
  %rest:POST
  %rest:path('/dba/log-download')
  %rest:query-param('name', '{$name}')
function dba:log-download(
  $name  as xs:string
) as item()+ {
  web:response-header(
    map { 'media-type': 'text/plain' },
    map { 'Content-Disposition': 'attachment; filename=' || $name || '.log' }
  ),
  file:read-binary(db:option('dbpath') || '/.logs/' || $name || '.log')
};
