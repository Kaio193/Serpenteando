param(
    [Parameter(Mandatory=$true)][string]$Image,
    [Parameter(Mandatory=$true)][string]$Prompt,
    [string]$Model = "gemini-3.5-flash",
    [string]$Out = ""
)

$ErrorActionPreference = "Stop"
$key = (Get-Content -LiteralPath (Join-Path $PSScriptRoot ".gemini_key") -Raw).Trim()

$bytes = [System.IO.File]::ReadAllBytes($Image)
$b64 = [Convert]::ToBase64String($bytes)
$mime = if ([System.IO.Path]::GetExtension($Image) -match "png") { "image/png" } else { "image/jpeg" }

$body = @{
    contents = @(
        @{
            role = "user"
            parts = @(
                @{ text = $Prompt },
                @{ inline_data = @{ mime_type = $mime; data = $b64 } }
            )
        }
    )
} | ConvertTo-Json -Depth 10

$url = "https://generativelanguage.googleapis.com/v1beta/models/$Model`:generateContent?key=$key"
$resp = Invoke-RestMethod -Uri $url -Method Post -ContentType "application/json; charset=utf-8" -Body $body

$text = $resp.candidates[0].content.parts[0].text

if ($Out) {
    Set-Content -LiteralPath $Out -Value $text -Encoding utf8
    Write-Host "Salvo em: $Out"
} else {
    Write-Output $text
}
