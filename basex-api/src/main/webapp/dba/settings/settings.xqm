(:~
 : Settings page.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/settings';

import module namespace Request = 'http://exquery.org/ns/request';
import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'settings';

(:~
 : Settings page.
 :)
declare
  %rest:GET
  %rest:path("/dba/settings")
  %output:method("html")
function dba:settings(
) as element() {
  cons:check(),

  tmpl:wrap(map { 'top': $dba:CAT },
    <tr>
      <td>
        <form action="settings" method="post">
          <h2>Settings » <button>Save</button></h2>
          <table>
            <tr>
              <td colspan='2'><h3>Querying</h3></td>
            </tr>
            {
              for $option in element options {
                element { $cons:K-TIMEOUT  } { '…timeout for queries, in seconds (0: disabled)' },
                element { $cons:K-MEMORY   } { '…memory limit for queries, in MB (0: disabled)' },
                element { $cons:K-MAXCHARS } { '…maximum number of characters in query results' },
                element { $cons:K-MAXROWS  } { '…maximum number of displayed table rows'        }
              }/*
              let $key := name($option)
              return <tr>
                <td><b>{ upper-case($key) }:</b></td>
                <td><input name="{ $key }" type="number" value="{ $cons:OPTION($key) }"/>
                  <span class='note'> &#xa0; { $option/text() }</span>
                </td>
              </tr>
            }
            <tr>
              <td><b>PERMISSION:</b></td>
              <td>
                <select name="permission">{
                  let $pm := $cons:OPTION($cons:K-PERMISSION)
                  for $p in $cons:PERMISSIONS
                  return element option { attribute selected { }[$p = $pm], $p }
                }</select>
                <span class='note'> &#xa0; …for query evaluation</span>
              </td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  )
};

(:~
 : Saves the settings.
 :)
declare
  %rest:POST
  %rest:path("/dba/settings")
  %output:method("html")
function dba:settings-save(
) as element(rest:response) {
  cons:check(),

  let $config := element config {
    for $key in Request:parameter-names()
    return element { $key } { Request:parameter($key) }
  }
  return (
    file:create-dir($cons:DBA-DIR),
    file:write($cons:DBA-SETTINGS-FILE, $config),
    web:redirect("settings")
  )
};
