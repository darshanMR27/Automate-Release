#Configure
#https://developer.atlassian.com/server/jira/platform/basic-authentication/
$JiraCreds = Get-Credential -Message "Credentials for Jira"
$prompted_auth = $JiraCreds.UserName + ":" + $JiraCreds.GetNetworkCredential().Password
$prompted_jira =[Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($prompted_auth))
$project = "CIR"
$fixversion = "Apollo 2.6"
$jql = "project = ""$($project)"" AND issuetype in (Bug, Feature, Release, Story) AND fixVersion = ""$($fixversion)"""
function Get-Ticket-Info {
    try {
        $header_Out = @{
            "Accept" = "application/json";
            "Authorization" = "Basic $($prompted_jira)"
        }
        $search = "https://jira.bottomline.tech/rest/api/latest/search?jql=$($jql)&maxResults=99999"
        $list = Invoke-RestMethod $search -Headers $header_Out -SessionVariable session | % {$_.Issues} | % {$_.key}
        # $list = Invoke-RestMethod $search -Headers $header_Out -SessionVariable session | % {$_.Issues}
        # Write-Output ${list}
        Write-Host ${list}
    } catch {
        Write-Warning "Could not connect to the $($name) jira project $($project) : $($Error[0])"
        $sumfailed++
    }
}
Get-Ticket-Info