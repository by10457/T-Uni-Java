param(
    [ValidateSet('true','false')]
    [string]$RenameAdmin = 'true',
    [string]$OldRootPackage = 't.uni',
    [string]$OldEnvPrefix = 'T_UNI',
    [string]$OldSlug = 't-uni'
)

$ErrorActionPreference = 'Stop'

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path (Join-Path $ScriptDir '..\..')
Set-Location $ProjectRoot

$Failures = 0
$Warnings = 0

function Get-SearchFiles {
    Get-ChildItem -Recurse -File -Force |
        Where-Object {
            $path = $_.FullName.Replace('\', '/')
            $name = $_.Name
            -not ($path -match '(^|/)\.git(/|$)' -or
                $path -match '(^|/)target(/|$)' -or
                $path -match '(^|/)node_modules(/|$)' -or
                $path -match '(^|/)\.idea(/|$)' -or
                $path -match '(^|/)\.claude(/|$)' -or
                $path -match '(^|/)scripts/rename-project(/|$)' -or
                $name -eq '.env' -or
                ($name.StartsWith('.env.') -and $name -ne '.env.example'))
        }
}

function Test-TextFile {
    param([string]$Path)
    $bytes = [IO.File]::ReadAllBytes($Path)
    if ($bytes.Length -eq 0) { return $true }
    return -not ($bytes[0..([Math]::Min($bytes.Length - 1, 4095))] -contains 0)
}

function Run-Scan {
    param([string]$Title, [string]$Pattern, [bool]$Fail, [bool]$FilterAdmin = $false)
    Write-Host ''
    Write-Host "## $Title"

    $matches = @()
    if (Get-Command rg -ErrorAction SilentlyContinue) {
        $args = @('-n', '--hidden', '--glob', '!.git/**', '--glob', '!target/**', '--glob', '!**/target/**', '--glob', '!node_modules/**', '--glob', '!**/node_modules/**', '--glob', '!.idea/**', '--glob', '!**/.idea/**', '--glob', '!.claude/**', '--glob', '!**/.claude/**', '--glob', '!scripts/rename-project/**', '--glob', '!.env', '--glob', '!.env.*', '--glob', '.env.example', '--', $Pattern, '.')
        $output = & rg @args 2>$null
        if ($LASTEXITCODE -eq 0 -and $output) {
            $matches = @($output)
        }
    } else {
        foreach ($file in Get-SearchFiles) {
            if (-not (Test-TextFile $file.FullName)) { continue }
            $lineNumber = 0
            Get-Content $file.FullName | ForEach-Object {
                $lineNumber++
                if ($_ -match $Pattern) {
                    $relative = Resolve-Path -Relative $file.FullName
                    $matches += "${relative}:${lineNumber}:$_"
                }
            }
        }
    }

    if ($FilterAdmin -and $RenameAdmin -eq 'false' -and $matches.Count -gt 0) {
        $matches = @($matches | Where-Object { $_ -notmatch 't-uni-admin|tuni-admin|tuni_admin|t_uni_admin|T_UNI_ADMIN' })
    }

    if ($matches.Count -gt 0) {
        $matches | ForEach-Object { Write-Host $_ }
        if ($Fail) { $script:Failures++ } else { $script:Warnings++ }
    } else {
        Write-Host '未发现残留。'
    }
}

$EscapedRoot = [regex]::Escape($OldRootPackage)
$EscapedEnv = [regex]::Escape($OldEnvPrefix)
$EscapedSlug = [regex]::Escape($OldSlug)
$OldPath = $OldRootPackage.Replace('.', '/')
$AdminFail = $RenameAdmin -eq 'true'

Write-Host '========================================='
Write-Host '  项目改名残留验证'
Write-Host '========================================='
Write-Host "仓库根目录: $ProjectRoot"
Write-Host "rename_admin: $RenameAdmin"

Run-Scan 'Java/package 残留' "\b$EscapedRoot\b|$OldPath" $true $true
Run-Scan 'Maven/module 残留' "$EscapedSlug|t-uni-common|t-uni-server" $true $true
Run-Scan 'YAML/env/config 前缀残留' "$EscapedEnv(_|\b)|\bt\.uni\s*:|\bt\.uni\.(jwt|master|redis|qiniu|wx|payment)" $true $true
Run-Scan 'Docker/compose/script 残留' "tuni-mysql|tuni-redis|logs/t-uni|data/t-uni|image: t-uni|container_name: t-uni" $true $true
Run-Scan '.env.example 残留' "$EscapedEnv|$EscapedSlug|tuni|t_uni" $true $true
Run-Scan 'OpenIM 默认值残留' 'tuni_|tuni_system' $true $true
Run-Scan 'MinIO / admin 资源残留' "tuni-admin|tuni_admin|t_uni_admin|${EscapedEnv}_ADMIN" $AdminFail
Run-Scan 'README / docs / prompts / skills 展示文案残留' "T-Uni|T-Uni-Java|$EscapedSlug|$EscapedEnv" $true $true

if ($RenameAdmin -eq 'false') {
    Run-Scan 'admin 目录保留项，仅人工复核' "t-uni-admin|tuni_admin|t_uni_admin|${EscapedEnv}_ADMIN|\bt\.uni\b" $false
}

Write-Host ''
Write-Host '========================================='
Write-Host '  验证摘要'
Write-Host '========================================='
Write-Host "失败分类数: $Failures"
Write-Host "人工复核分类数: $Warnings"

if ($Failures -gt 0) {
    Write-Error '验证未通过：仍有旧命名残留需要处理。'
    exit 1
}

Write-Host '验证通过：未发现必须失败的旧命名残留。'
