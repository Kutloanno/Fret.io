$notes = [ordered]@{
    "C"  = "c"
    "Db" = "cs"
    "D"  = "d"
    "Eb" = "ds"
    "E"  = "e"
    "F"  = "f"
    "Gb" = "fs"
    "G"  = "g"
    "Ab" = "gs"
    "A"  = "a"
    "Bb" = "as"
    "B"  = "b"
}

$octaves = @("3", "4")
$baseUrl = "https://raw.githubusercontent.com/fuhton/piano-mp3/master/piano-mp3/"
$outputDir = "c:\Users\anomo\OneDrive\Documentos\Guitar Kaizen\app\src\main\res\raw"

# Create raw resources folder if it doesn't exist
if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Force -Path $outputDir
    Write-Host "Created directory $outputDir"
}

Write-Host "Starting dynamic sampler audio downloads..."

foreach ($octave in $octaves) {
    foreach ($key in $notes.Keys) {
        $sourceName = "$key$octave.mp3"
        $targetName = "note_$($notes[$key])$octave.mp3"
        
        $url = "$baseUrl$sourceName"
        $outputPath = Join-Path $outputDir $targetName
        
        Write-Host "Downloading $sourceName -> $targetName..."
        try {
            # Use standard PowerShell web client or Invoke-WebRequest
            [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12
            Invoke-WebRequest -Uri $url -OutFile $outputPath -ErrorAction Stop
            Write-Host "Successfully saved to $targetName"
        } catch {
            Write-Error "Failed to download $sourceName from $url. Error: $_"
        }
    }
}

Write-Host "All downloads completed!"
