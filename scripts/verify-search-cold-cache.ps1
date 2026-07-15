param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$MaxElapsedMs = 5000,
    [string[]]$Keywords = @(
        (-join [char[]](0xAD00, 0xAD11, 0xC9C0)),
        (-join [char[]](0xB9DB, 0xC9D1)),
        (-join [char[]](0xC219, 0xC18C)),
        (-join [char[]](0xD574, 0xBCC0)),
        (-join [char[]](0xC790, 0xC5F0)),
        (-join [char[]](0xBB38, 0xD654))
    )
)

$ErrorActionPreference = "Stop"
$results = @()

foreach ($keyword in $Keywords) {
    $encodedKeyword = [Uri]::EscapeDataString($keyword)
    $uri = "$($BaseUrl.TrimEnd('/'))/api/v1/search/results?keyword=$encodedKeyword&page=0&size=20"
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()

    try {
        $response = Invoke-RestMethod -Method Get -Uri $uri
        $stopwatch.Stop()
        $success = $response.success -eq $true
        $totalCount = if ($null -ne $response.data) { [int]$response.data.totalCount } else { 0 }
        $passed = $success -and $totalCount -gt 0 -and $stopwatch.ElapsedMilliseconds -le $MaxElapsedMs
        $results += [PSCustomObject]@{
            Keyword = $keyword
            ElapsedMs = $stopwatch.ElapsedMilliseconds
            TotalCount = $totalCount
            Code = $response.code
            Passed = $passed
        }
    }
    catch {
        $stopwatch.Stop()
        $results += [PSCustomObject]@{
            Keyword = $keyword
            ElapsedMs = $stopwatch.ElapsedMilliseconds
            TotalCount = 0
            Code = $_.Exception.Message
            Passed = $false
        }
    }
}

$results | Format-Table -AutoSize

if ($results.Where({ -not $_.Passed }).Count -gt 0) {
    Write-Error "Cold-cache verification failed. Each category must return at least one result within ${MaxElapsedMs}ms."
    exit 1
}

Write-Host "All six cold-cache searches passed within ${MaxElapsedMs}ms."
