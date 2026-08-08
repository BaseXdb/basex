(:~
 : Waits for a query of the editor and returns its outcome.
 :
 : Started by editor-ws.xqm as a job of its own. The job cannot import the DBA modules, so the
 : function for serializing the result is supplied as an external variable.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)

(:~ Id of the query job. :)
declare variable $id as xs:string external;
(:~ Number of the run. :)
declare variable $run as xs:integer external;
(:~ Function for serializing the result. :)
declare variable $serialize as fn(item()*) as xs:string external;

job:wait($id),
try {
  {
    'type'  : 'result',
    'run'   : $run,
    'result': $serialize(job:result($id))
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
