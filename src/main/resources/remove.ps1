$ruleName = 'IDEA'
Start-Process powershell -Verb RunAs -ArgumentList "-Command Remove-NetFirewallRule -DisplayName '$ruleName'"