$ErrorActionPreference = "Stop"

function Normalize-Text {
    param([string]$Text)
    if ([string]::IsNullOrWhiteSpace($Text)) { return "" }
    $clean = $Text -replace "`r", "" -replace "\s+", " "
    return $clean.Trim()
}

function Write-SplitMarkdown {
    param(
        [Parameter(Mandatory = $true)][string]$Directory,
        [Parameter(Mandatory = $true)][string]$Prefix,
        [Parameter(Mandatory = $true)][string]$Content,
        [int]$MaxBytes = 1048576
    )

    if (!(Test-Path $Directory)) {
        New-Item -ItemType Directory -Path $Directory -Force | Out-Null
    }

    Get-ChildItem -Path $Directory -Filter "$Prefix*" -File -ErrorAction SilentlyContinue |
        Remove-Item -Force

    $lines = $Content -split "`r?`n"
    $parts = New-Object System.Collections.Generic.List[string]
    $builder = New-Object System.Text.StringBuilder

    foreach ($line in $lines) {
        $candidate = if ($builder.Length -eq 0) { $line } else { $builder.ToString() + "`n" + $line }
        $size = [Text.Encoding]::UTF8.GetByteCount($candidate)
        if ($size -gt $MaxBytes -and $builder.Length -gt 0) {
            $parts.Add($builder.ToString())
            $builder = New-Object System.Text.StringBuilder
            [void]$builder.Append($line)
        } else {
            if ($builder.Length -gt 0) { [void]$builder.Append("`n") }
            [void]$builder.Append($line)
        }
    }

    if ($builder.Length -gt 0) { $parts.Add($builder.ToString()) }
    if ($parts.Count -eq 0) { $parts.Add("") }

    for ($i = 0; $i -lt $parts.Count; $i++) {
        $index = "{0:D3}" -f ($i + 1)
        $file = Join-Path $Directory ("{0}{1}" -f $Prefix, $index)
        Set-Content -Path $file -Value $parts[$i] -Encoding UTF8
    }

    return $parts.Count
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = (Resolve-Path (Join-Path $scriptDir "..\..")).Path
$aiDocs = Join-Path $projectRoot "ai_docs"
$logsDir = Join-Path $aiDocs "logs"
$ctxDir = Join-Path $aiDocs "context"

if (!(Test-Path $aiDocs)) {
    throw "ai_docs not found at: $aiDocs"
}

$sessionRoots = @(
    (Join-Path $env:USERPROFILE ".codex\sessions"),
    (Join-Path $projectRoot "dokumanlar\oturum-kayitlari\codex-sessions")
)

$sessionFiles = @()
foreach ($root in $sessionRoots) {
    if (Test-Path $root) {
        $sessionFiles += Get-ChildItem -Path $root -Recurse -File -Filter "*.jsonl" -ErrorAction SilentlyContinue
    }
}
$sessionFiles = $sessionFiles | Sort-Object FullName -Unique

$sessions = New-Object System.Collections.Generic.List[object]

foreach ($file in $sessionFiles) {
    $meta = [ordered]@{
        file = $file.FullName
        id = ""
        timestamp = ""
        cwd = ""
    }
    $messages = New-Object System.Collections.Generic.List[object]

    foreach ($line in Get-Content -Path $file.FullName -Encoding UTF8) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }

        try {
            $obj = $line | ConvertFrom-Json
        } catch {
            continue
        }

        $ts = [string]$obj.timestamp

        if ($obj.type -eq "session_meta" -and $obj.payload) {
            $meta.id = [string]$obj.payload.id
            $meta.timestamp = [string]$obj.payload.timestamp
            $meta.cwd = [string]$obj.payload.cwd
            continue
        }

        if ($obj.type -eq "response_item" -and $obj.payload -and $obj.payload.type -eq "message") {
            $role = [string]$obj.payload.role
            if ($role -notin @("user", "assistant", "developer", "system")) { continue }

            $texts = New-Object System.Collections.Generic.List[string]
            if ($obj.payload.content) {
                foreach ($c in $obj.payload.content) {
                    if ($null -ne $c.text -and ![string]::IsNullOrWhiteSpace([string]$c.text)) {
                        $texts.Add([string]$c.text)
                    }
                }
            }
            $text = Normalize-Text ($texts -join "`n")
            if ($text.Length -gt 0) {
                $messages.Add([pscustomobject]@{
                    timestamp = $ts
                    role = $role
                    text = $text
                })
            }
            continue
        }

        if ($obj.type -eq "event_msg" -and $obj.payload -and $obj.payload.type -eq "agent_message") {
            $agentText = Normalize-Text ([string]$obj.payload.message)
            if ($agentText.Length -gt 0) {
                $messages.Add([pscustomobject]@{
                    timestamp = $ts
                    role = "assistant"
                    text = $agentText
                })
            }
        }
    }

    $sessions.Add([pscustomobject]@{
        Meta = [pscustomobject]$meta
        Messages = $messages
    })
}

# Deduplicate sessions by id first, then by file path
$sessionKeys = New-Object System.Collections.Generic.HashSet[string]
$dedupedSessions = New-Object System.Collections.Generic.List[object]
foreach ($session in $sessions) {
    $key = if (![string]::IsNullOrWhiteSpace($session.Meta.id)) {
        "id:$($session.Meta.id)"
    } else {
        "file:$($session.Meta.file)"
    }
    if ($sessionKeys.Add($key)) {
        $dedupedSessions.Add($session)
    }
}
$sessions = $dedupedSessions

$archiveBuilder = New-Object System.Text.StringBuilder
[void]$archiveBuilder.AppendLine("# Conversation Archive (Aggregated)")
[void]$archiveBuilder.AppendLine("")
[void]$archiveBuilder.AppendLine("Generated: $(Get-Date -Format o)")
[void]$archiveBuilder.AppendLine("Session files: $($sessionFiles.Count)")
[void]$archiveBuilder.AppendLine("")

foreach ($session in $sessions) {
    $rel = $session.Meta.file.Replace("$env:USERPROFILE\", "~\")
    [void]$archiveBuilder.AppendLine("## Session: $rel")
    [void]$archiveBuilder.AppendLine("- id: $($session.Meta.id)")
    [void]$archiveBuilder.AppendLine("- timestamp: $($session.Meta.timestamp)")
    [void]$archiveBuilder.AppendLine("- cwd: $($session.Meta.cwd)")
    [void]$archiveBuilder.AppendLine("- message_count: $($session.Messages.Count)")
    [void]$archiveBuilder.AppendLine("")

    foreach ($message in $session.Messages) {
        $text = $message.text
        if ($text.Length -gt 1800) { $text = $text.Substring(0, 1800) + " ...[truncated]" }
        [void]$archiveBuilder.AppendLine("- [$($message.timestamp)] $($message.role.ToUpper()): $text")
    }
    [void]$archiveBuilder.AppendLine("")
}

$archiveParts = Write-SplitMarkdown `
    -Directory $logsDir `
    -Prefix "conversation_archive_current.md_" `
    -Content $archiveBuilder.ToString()

$topics = @(
    "web pos", "mobil", "lisans", "faz", "patch", "apk", "senkron",
    "sync", "barkod", "ocr", "stok", "rapor", "ticket", "yazdir", "admin", "pin", "compan"
)

$compactBuilder = New-Object System.Text.StringBuilder
[void]$compactBuilder.AppendLine("# Context History (Compact)")
[void]$compactBuilder.AppendLine("")
[void]$compactBuilder.AppendLine("Generated: $(Get-Date -Format o)")
[void]$compactBuilder.AppendLine("Session count: $($sessions.Count)")
[void]$compactBuilder.AppendLine("")

foreach ($session in $sessions) {
    $messages = $session.Messages
    $userMessages = $messages | Where-Object { $_.role -eq "user" }
    $assistantMessages = $messages | Where-Object { $_.role -eq "assistant" }
    $allText = ($messages | ForEach-Object { $_.text }) -join "`n"
    $lc = $allText.ToLowerInvariant()

    $detected = New-Object System.Collections.Generic.List[string]
    foreach ($topic in $topics) {
        if ($lc.Contains($topic)) { $detected.Add($topic) }
    }

    $directives = $userMessages |
        Where-Object { $_.text -match "(faz|kural|surum|sürüm|patch|web pos|mobil|duzelt|düzelt|ekle|yap|istiyorum|zorunlu)" } |
        Select-Object -ExpandProperty text

    $seen = New-Object System.Collections.Generic.HashSet[string]
    $picked = New-Object System.Collections.Generic.List[string]
    foreach ($directive in $directives) {
        $d = Normalize-Text $directive
        if ($d.Length -gt 220) { $d = $d.Substring(0, 220) + " ..." }
        if ($d.Length -gt 0 -and $seen.Add($d)) {
            $picked.Add($d)
        }
        if ($picked.Count -ge 12) { break }
    }

    $firstUser = $userMessages | Select-Object -First 1
    $lastUser = $userMessages | Select-Object -Last 1
    $rel = $session.Meta.file.Replace("$env:USERPROFILE\", "~\")

    [void]$compactBuilder.AppendLine("## Session: $rel")
    [void]$compactBuilder.AppendLine("- message_count: $($messages.Count) (user: $($userMessages.Count), assistant: $($assistantMessages.Count))")
    [void]$compactBuilder.AppendLine("- topic_tags: $([string]::Join(', ', $detected))")

    if ($firstUser) {
        $txt = $firstUser.text
        if ($txt.Length -gt 280) { $txt = $txt.Substring(0, 280) + " ..." }
        [void]$compactBuilder.AppendLine("- first_user_summary: $txt")
    }
    if ($lastUser) {
        $txt = $lastUser.text
        if ($txt.Length -gt 280) { $txt = $txt.Substring(0, 280) + " ..." }
        [void]$compactBuilder.AppendLine("- last_user_summary: $txt")
    }
    if ($picked.Count -gt 0) {
        [void]$compactBuilder.AppendLine("- notable_directives:")
        foreach ($item in $picked) {
            [void]$compactBuilder.AppendLine("  - $item")
        }
    }
    [void]$compactBuilder.AppendLine("")
}

$compactParts = Write-SplitMarkdown `
    -Directory $ctxDir `
    -Prefix "context_history_current.md_" `
    -Content $compactBuilder.ToString()

$runtimeRoots = @(
    (Join-Path $env:USERPROFILE "Desktop\Codex.log"),
    (Join-Path $projectRoot "dokumanlar\oturum-kayitlari")
)

$runtimeFiles = @()
foreach ($root in $runtimeRoots) {
    if (Test-Path $root) {
        if ((Get-Item $root).PSIsContainer) {
            $runtimeFiles += Get-ChildItem -Path $root -Recurse -File -Include "*.log" -ErrorAction SilentlyContinue
        } else {
            $runtimeFiles += Get-Item $root
        }
    }
}
$runtimeFiles = $runtimeFiles | Sort-Object FullName -Unique

$runtimeBuilder = New-Object System.Text.StringBuilder
[void]$runtimeBuilder.AppendLine("# Runtime Logs Summary")
[void]$runtimeBuilder.AppendLine("")
[void]$runtimeBuilder.AppendLine("Generated: $(Get-Date -Format o)")
[void]$runtimeBuilder.AppendLine("Log file count: $($runtimeFiles.Count)")
[void]$runtimeBuilder.AppendLine("")

$allErrors = New-Object System.Collections.Generic.List[string]
$allWarnings = New-Object System.Collections.Generic.List[string]

foreach ($file in $runtimeFiles) {
    $lines = Get-Content -Path $file.FullName -Encoding UTF8 -ErrorAction SilentlyContinue
    $errors = @($lines | Where-Object { $_ -match "\[error\]" })
    $warnings = @($lines | Where-Object { $_ -match "\[warning\]" })
    foreach ($e in $errors) { $allErrors.Add($e) }
    foreach ($w in $warnings) { $allWarnings.Add($w) }

    $rel = $file.FullName.Replace("$env:USERPROFILE\", "~\")
    [void]$runtimeBuilder.AppendLine("## $rel")
    [void]$runtimeBuilder.AppendLine("- error_count: $($errors.Count)")
    [void]$runtimeBuilder.AppendLine("- warning_count: $($warnings.Count)")

    if ($errors.Count -gt 0) {
        [void]$runtimeBuilder.AppendLine("- last_error_samples:")
        foreach ($sample in ($errors | Select-Object -Last 2)) {
            $txt = Normalize-Text $sample
            if ($txt.Length -gt 300) { $txt = $txt.Substring(0, 300) + " ..." }
            [void]$runtimeBuilder.AppendLine("  - $txt")
        }
    }
    if ($warnings.Count -gt 0) {
        [void]$runtimeBuilder.AppendLine("- last_warning_samples:")
        foreach ($sample in ($warnings | Select-Object -Last 2)) {
            $txt = Normalize-Text $sample
            if ($txt.Length -gt 300) { $txt = $txt.Substring(0, 300) + " ..." }
            [void]$runtimeBuilder.AppendLine("  - $txt")
        }
    }
    [void]$runtimeBuilder.AppendLine("")
}

if ($allErrors.Count -gt 0) {
    [void]$runtimeBuilder.AppendLine("## Top Error Signatures (Top 10)")
    $groups = $allErrors | Group-Object | Sort-Object Count -Descending | Select-Object -First 10
    foreach ($group in $groups) {
        $txt = Normalize-Text $group.Name
        if ($txt.Length -gt 220) { $txt = $txt.Substring(0, 220) + " ..." }
        [void]$runtimeBuilder.AppendLine("- [$($group.Count)] $txt")
    }
    [void]$runtimeBuilder.AppendLine("")
}

if ($allWarnings.Count -gt 0) {
    [void]$runtimeBuilder.AppendLine("## Top Warning Signatures (Top 10)")
    $groups = $allWarnings | Group-Object | Sort-Object Count -Descending | Select-Object -First 10
    foreach ($group in $groups) {
        $txt = Normalize-Text $group.Name
        if ($txt.Length -gt 220) { $txt = $txt.Substring(0, 220) + " ..." }
        [void]$runtimeBuilder.AppendLine("- [$($group.Count)] $txt")
    }
}

$runtimeParts = Write-SplitMarkdown `
    -Directory $logsDir `
    -Prefix "runtime_logs_summary_current.md_" `
    -Content $runtimeBuilder.ToString()

$sourcesPath = Join-Path $aiDocs "sources.md"
if (Test-Path $sourcesPath) {
    Add-Content -Path $sourcesPath -Encoding UTF8 -Value @"

## Historical Session and Log Ingestion ($(Get-Date -Format yyyy-MM-dd))
- processed_session_jsonl: $($sessionFiles.Count)
- processed_runtime_logs: $($runtimeFiles.Count)
- conversation_archive_parts: $archiveParts
- compact_context_parts: $compactParts
- runtime_log_summary_parts: $runtimeParts
"@
}

$ctxCurrent = Join-Path $ctxDir "context_current.md_001"
if (Test-Path $ctxCurrent) {
    Add-Content -Path $ctxCurrent -Encoding UTF8 -Value @"

## Entry 0002
- timestamp: $(Get-Date -Format o)
- request_classification: docs_only
- user_request_summary: Process historical logs, conversation records, and compact context into ai_docs.
- read_documents:
  - $env:USERPROFILE\.codex\sessions\**\*.jsonl
  - $projectRoot\dokumanlar\oturum-kayitlari\**
  - $env:USERPROFILE\Desktop\Codex.log
- constraints:
  - no app code changes
  - keep updates inside ai_docs only
- decisions:
  - generated aggregated conversation archive
  - generated compact context history
  - generated runtime log summary
  - applied 1024KB split policy
- result:
  - historical memory artifacts are now persisted under ai_docs
"@
}

$changeCurrent = Join-Path $logsDir "changelog_current.md_001"
if (Test-Path $changeCurrent) {
    Add-Content -Path $changeCurrent -Encoding UTF8 -Value @"

## $(Get-Date -Format yyyy-MM-dd) - Historical Memory Ingestion

### Change Type
- docs_only

### Added/Updated
- ai_docs/logs/conversation_archive_current.md_00X
- ai_docs/context/context_history_current.md_00X
- ai_docs/logs/runtime_logs_summary_current.md_00X
- ai_docs/sources.md
- ai_docs/context/context_current.md_001 (Entry 0002)
"@
}

$convCurrent = Join-Path $logsDir "conversation_current.md_001"
if (Test-Path $convCurrent) {
    Add-Content -Path $convCurrent -Encoding UTF8 -Value @"

### User Request
- process historical logs, conversation records and compact context into ai_docs

### Assistant Action Summary
- parsed JSONL sessions from codex session folders
- generated split markdown conversation archive files
- generated compact context history files
- generated runtime error/warning summary files
"@
}

Write-Output ("session_files={0}; archive_parts={1}; compact_parts={2}; runtime_files={3}; runtime_parts={4}" -f $sessionFiles.Count, $archiveParts, $compactParts, $runtimeFiles.Count, $runtimeParts)
