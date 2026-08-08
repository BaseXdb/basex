(:~
 : Waits for a job of the current WebSocket connection and returns its outcome.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
import module namespace utils = 'dba/utils' at 'utils.xqm';
import module namespace config = 'dba/config' at 'config.xqm';

(:~ Id of the job. :)
declare variable $id as xs:string external;
(:~ Number of the run. :)
declare variable $run as xs:integer external;
(:~ Serialization parameters. :)
declare variable $options as map(*) external;

job:wait($id),
try {
  let $string := serialize(job:result($id), $options)
  return {
    'type'  : 'result',
    'run'   : $run,
    'result': if ($options?limit) {
      utils:chop($string, config:get($config:MAXCHARS))
    } else {
      $string
    }
  }
} catch * {
  {
    'type'   : 'error',
    'run'    : $run,
    'message': $err:description,
    'line'   : $err:line-number,
    'column' : $err:column-number
  }
}
