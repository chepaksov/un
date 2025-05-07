param (
    [string]$ips
)

Start-Process powershell -Verb runas -ArgumentList "-Command New-NetFirewallRule -DisplayName 'IDEA' -Direction Outbound -Action Block -RemoteAddress $ips"