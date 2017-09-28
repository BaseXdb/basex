(:~
 : Global constants and functions.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace cons = 'dba/cons';

import module namespace Request = 'http://exquery.org/ns/request';
import module namespace Session = 'http://basex.org/modules/session';

(:~ Session key. :)
declare variable $cons:SESSION-KEY := "dba";
(:~ Current session. :)
declare variable $cons:SESSION-VALUE := Session:get($cons:SESSION-KEY);

(:~ Directory for DBA files. :)
declare variable $cons:DBA-DIR := file:temp-dir() || 'dba/';
(:~ Configuration file. :)
declare variable $cons:DBA-SETTINGS-FILE := $cons:DBA-DIR || 'dba-settings.xml';

(:~ Query file suffix. :)
declare variable $cons:SUFFIX := '.xq';

(:~ Permissions. :)
declare variable $cons:PERMISSIONS := ('none', 'read', 'write', 'create', 'admin');

(:~ Maximum length of XML characters. :)
declare variable $cons:K-MAXCHARS := 'maxchars';
(:~ Maximum number of table entries. :)
declare variable $cons:K-MAXROWS := 'maxrows';
(:~ Query timeout. :)
declare variable $cons:K-TIMEOUT := 'timeout';
(:~ Maximal memory consumption. :)
declare variable $cons:K-MEMORY := 'memory';
(:~ Permission when running queries. :)
declare variable $cons:K-PERMISSION := 'permission';

(:~ Configuration. :)
declare variable $cons:OPTION :=
  let $defaults := map {
    'maxchars': 100000,
    'maxrows': 100,
    'timeout': 10,
    'memory': 500,
    'permission': 'admin'
  }
  return if(file:exists($cons:DBA-SETTINGS-FILE)) then (
    try {
      (: merge defaults with options from settings file :)
      let $configs := fetch:xml($cons:DBA-SETTINGS-FILE)/config
      return map:merge(
        map:for-each($defaults, function($key, $value) {
          map:entry($key,
            let $config := $configs/*[name() = $key]
            return if($config) then (
              if($value instance of xs:numeric) then xs:integer($config) else xs:string($config)
            ) else (
              $value
            )
          )
        })
      )
    } catch * {
      (: use defaults if an error occurs while parsing the configuration file :)
      $defaults
    }
  ) else (
    $defaults
  );

(:~
 : Checks if the current client is logged in. If not, raises an error.
 :)
declare function cons:check(
) as empty-sequence() {
  if($cons:SESSION-VALUE) then () else
    error(xs:QName('basex:login'), 'Please log in again', Request:path())
};

(:~
 : Convenience function for redirecting to another page from update operations.
 : @param  $url     URL
 : @param  $params  query parameters
 :)
declare %updating function cons:redirect(
  $url     as xs:string,
  $params  as map(*)
) as empty-sequence() {
  db:output(web:redirect($url, $params))
};
