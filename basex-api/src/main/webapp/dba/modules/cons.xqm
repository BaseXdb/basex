(:~
 : Global constants and functions.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace cons = 'dba/cons';

import module namespace Session = 'http://basex.org/modules/session';

(:~ An error occured while retrieving data. :)
declare variable $cons:DATA-ERROR := 'Could not retrieve data';

(:~ Session key. :)
declare variable $cons:SESSION-KEY := "session";
(:~ Current session. :)
declare variable $cons:SESSION := Session:get($cons:SESSION-KEY);

(:~ Directory for DBA files. :)
declare variable $cons:DBA-DIR := file:resolve-path(file:temp-dir() || 'dba/') !
  (file:create-dir(.), .);
(:~ Configuration file. :)
declare variable $cons:DBA-SETTINGS-FILE := $cons:DBA-DIR || 'dba-settings.xml';

(:~ Permissions. :)
declare variable $cons:PERMISSIONS := ('none', 'read', 'write', 'create', 'admin');

(:~ Maximum length of XML characters. :)
declare variable $cons:K-MAX-CHARS := 'maxchars';
(:~ Maximum number of table entries. :)
declare variable $cons:K-MAX-ROWS := 'maxrows';
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
    'maxrows': 500,
    'timeout': 10,
    'memory': 200,
    'permission': 'admin'
  }
  return try {
    (: merge defaults with options in settings file :)
    map:merge((
      $defaults,
      for $opt in doc($cons:DBA-SETTINGS-FILE)/config/*
      let $key := $opt/name()
      let $val := if($defaults($key) instance of xs:integer) then xs:integer($opt) else string($opt)
      return map { $key: $val }
    ))
  } catch * {
    (: settings file does not exist or is not well-formed... use defaults :)
    $defaults
  };

(:~
 : Checks if the current client is logged in. If not, raises an error.
 :)
declare function cons:check(
) as empty-sequence() {
  if($cons:SESSION) then () else error(xs:QName("basex:login"), 'Please log in again.')
};
