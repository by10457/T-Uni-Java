param(
    [string]$Config,
    [string]$DisplayName,
    [string]$RepoSlug,
    [string]$GroupId,
    [string]$RootPackage,
    [string]$ConfigPrefix,
    [string]$EnvPrefix,
    [string]$ServerDbName,
    [string]$AdminDbName,
    [string]$CommonModule,
    [string]$ServerModule,
    [string]$AdminModule,
    [ValidateSet('true','false')]
    [string]$RenameAdmin = 'false',
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path (Join-Path $ScriptDir '..\..')
Set-Location $ProjectRoot

function Load-ConfigFile {
    param([string]$Path)
    if (-not (Test-Path $Path)) {
        throw "配置文件不存在: $Path"
    }

    Get-Content $Path | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith('#')) { return }
        $index = $line.IndexOf('=')
        if ($index -lt 1) { return }
        $key = $line.Substring(0, $index).Trim()
        $value = $line.Substring($index + 1).Trim().Trim('"')
        switch ($key) {
            'DISPLAY_NAME' { if (-not $DisplayName) { $script:DisplayName = $value } }
            'REPO_SLUG' { if (-not $RepoSlug) { $script:RepoSlug = $value } }
            'GROUP_ID' { if (-not $GroupId) { $script:GroupId = $value } }
            'ROOT_PACKAGE' { if (-not $RootPackage) { $script:RootPackage = $value } }
            'CONFIG_PREFIX' { if (-not $ConfigPrefix) { $script:ConfigPrefix = $value } }
            'ENV_PREFIX' { if (-not $EnvPrefix) { $script:EnvPrefix = $value } }
            'SERVER_DB_NAME' { if (-not $ServerDbName) { $script:ServerDbName = $value } }
            'ADMIN_DB_NAME' { if (-not $AdminDbName) { $script:AdminDbName = $value } }
            'COMMON_MODULE' { if (-not $CommonModule) { $script:CommonModule = $value } }
            'SERVER_MODULE' { if (-not $ServerModule) { $script:ServerModule = $value } }
            'ADMIN_MODULE' { if (-not $AdminModule) { $script:AdminModule = $value } }
            'RENAME_ADMIN' { if ($RenameAdmin -eq 'false') { $script:RenameAdmin = $value.ToLowerInvariant() } }
        }
    }
}

if ($Config) {
    Load-ConfigFile $Config
}

function Require-Value {
    param([string]$Name, [string]$Value)
    if ([string]::IsNullOrWhiteSpace($Value)) {
        throw "缺少必填参数 $Name"
    }
}

Require-Value '-DisplayName' $DisplayName
Require-Value '-RepoSlug' $RepoSlug
Require-Value '-GroupId' $GroupId
Require-Value '-RootPackage' $RootPackage
Require-Value '-ConfigPrefix' $ConfigPrefix
Require-Value '-EnvPrefix' $EnvPrefix
Require-Value '-ServerDbName' $ServerDbName
Require-Value '-AdminDbName' $AdminDbName
Require-Value '-CommonModule' $CommonModule
Require-Value '-ServerModule' $ServerModule
if ($RenameAdmin -eq 'true') {
    Require-Value '-AdminModule' $AdminModule
}

if (-not (Test-Path 'pom.xml')) {
    throw "未找到仓库根目录 pom.xml: $ProjectRoot"
}

$RootPackagePath = $RootPackage.Replace('.', [IO.Path]::DirectorySeparatorChar)
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$UpdatedFiles = 0
$RenamedPaths = 0
$SkippedFiles = 0
$ManualItems = 0

Write-Host '========================================='
Write-Host '  T-Uni Java 项目改名脚本'
Write-Host '========================================='
Write-Host "仓库根目录: $ProjectRoot"
Write-Host "模式: $(if ($DryRun) { 'dry-run' } else { 'apply' })"
Write-Host "rename_admin: $RenameAdmin"
Write-Host "新项目名: $DisplayName"
Write-Host "新仓库名: $RepoSlug"
Write-Host "新包名: $RootPackage"
Write-Host "新配置前缀: $ConfigPrefix"
Write-Host "新环境变量前缀: $EnvPrefix"

function Should-SkipPath {
    param([string]$Path, [bool]$IsFile = $true)
    $normalized = $Path.Replace('\', '/')
    if ($normalized -match '(^|/)\.git(/|$)' -or
        $normalized -match '(^|/)target(/|$)' -or
        $normalized -match '(^|/)node_modules(/|$)' -or
        $normalized -match '(^|/)\.idea(/|$)' -or
        $normalized -match '(^|/)\.claude(/|$)' -or
        $normalized -match '(^|/)scripts/rename-project(/|$)') {
        return $true
    }
    if ($RenameAdmin -eq 'false' -and $normalized -match '(^|/)t-uni-admin(/|$)') {
        return $true
    }
    if ($IsFile) {
        $name = Split-Path -Leaf $Path
        if ($name -eq '.env') { return $true }
        if ($name.StartsWith('.env.') -and $name -ne '.env.example') { return $true }
    }
    return $false
}

function Test-TextFile {
    param([string]$Path)
    $bytes = [IO.File]::ReadAllBytes($Path)
    if ($bytes.Length -eq 0) { return $true }
    return -not ($bytes[0..([Math]::Min($bytes.Length - 1, 4095))] -contains 0)
}

function Move-PathIfExists {
    param([string]$From, [string]$To)
    if (-not (Test-Path $From)) { return }
    if ($From -eq $To) { return }
    if (Test-Path $To) {
        Write-Host "跳过重命名，目标已存在: $From -> $To"
        $script:ManualItems++
        return
    }
    if ($DryRun) {
        Write-Host "[dry-run] 重命名: $From -> $To"
    } else {
        $parent = Split-Path -Parent $To
        if ($parent -and -not (Test-Path $parent)) {
            New-Item -ItemType Directory -Path $parent | Out-Null
        }
        Move-Item $From $To
        Write-Host "已重命名: $From -> $To"
    }
    $script:RenamedPaths++
}

function Convert-Text {
    param([string]$Text)
    $keep = @{}
    function Protect([string]$Needle, [string]$Token) {
        $script:workingText = $script:workingText.Replace($Needle, $Token)
        $keep[$Token] = $Needle
    }

    $script:workingText = $Text
    if ($RenameAdmin -eq 'false') {
        Protect 't-uni-admin' '__KEEP_ADMIN_DASH_MODULE__'
        Protect 'tuni-admin' '__KEEP_ADMIN_DASH_RESOURCE__'
        Protect 'tuni_admin' '__KEEP_ADMIN_UNDERSCORE_DB__'
        Protect 't_uni_admin' '__KEEP_ADMIN_SPLIT_DB__'
        Protect 'T_UNI_ADMIN' '__KEEP_ADMIN_ENV_PREFIX__'
    }

    $script:workingText = $script:workingText.Replace('<groupId>t.uni</groupId>', "<groupId>$GroupId</groupId>")
    $script:workingText = $script:workingText.Replace('<groupId>t.uni', "<groupId>$GroupId")
    $script:workingText = $script:workingText.Replace('prefix = "t.uni.', "prefix = `"$ConfigPrefix.")
    $script:workingText = $script:workingText.Replace('prefix="t.uni.', "prefix=`"$ConfigPrefix.")
    $script:workingText = $script:workingText.Replace('prefix = "t.uni"', "prefix = `"$ConfigPrefix`")
    $script:workingText = $script:workingText.Replace('prefix="t.uni"', "prefix=`"$ConfigPrefix`")
    $script:workingText = [regex]::Replace($script:workingText, '\bt\.uni\b(?=\s*:)', $ConfigPrefix)
    $script:workingText = [regex]::Replace($script:workingText, '\bt\.uni\.(jwt|master|redis|qiniu|wx|payment)\b', "$ConfigPrefix.`$1")
    $script:workingText = [regex]::Replace($script:workingText, '\bt\.uni\b', $RootPackage)
    $script:workingText = [regex]::Replace($script:workingText, '\bt/uni\b', $RootPackage.Replace('.', '/'))

    $script:workingText = $script:workingText.Replace('t-uni-common', $CommonModule)
    $script:workingText = $script:workingText.Replace('t-uni-server', $ServerModule)
    if ($RenameAdmin -eq 'true') {
        $script:workingText = $script:workingText.Replace('t-uni-admin', $AdminModule)
        $script:workingText = $script:workingText.Replace('tuni-admin', $AdminModule)
        $script:workingText = $script:workingText.Replace('tuni_admin', $AdminDbName)
        $script:workingText = $script:workingText.Replace('t_uni_admin', $AdminDbName)
        $script:workingText = $script:workingText.Replace('T_UNI_ADMIN', "${EnvPrefix}_ADMIN")
    }

    $script:workingText = $script:workingText.Replace('T-Uni-Java', $DisplayName)
    $script:workingText = $script:workingText.Replace('T-Uni', $DisplayName)
    $script:workingText = $script:workingText.Replace('t-uni', $RepoSlug)
    $script:workingText = $script:workingText.Replace('tuni', $ServerDbName)
    $script:workingText = $script:workingText.Replace('t_uni', $ServerDbName)
    $script:workingText = $script:workingText.Replace('T_UNI_DOCKER', "${EnvPrefix}_DOCKER")
    $script:workingText = $script:workingText.Replace('T_UNI_', "${EnvPrefix}_")
    $script:workingText = $script:workingText.Replace('T_UNI', $EnvPrefix)

    if ($RenameAdmin -eq 'false') {
        foreach ($token in $keep.Keys) {
            $script:workingText = $script:workingText.Replace($token, $keep[$token])
        }
    }

    return $script:workingText
}

Write-Host ''
Write-Host '[1/3] 规划目录重命名...'
$packageDirs = Get-ChildItem -Recurse -Directory -Force |
    Where-Object { -not (Should-SkipPath $_.FullName $false) } |
    Where-Object { $_.FullName.Replace('\', '/') -match '/src/(main|test)/java/t/uni$' } |
    Sort-Object { $_.FullName.Length } -Descending
foreach ($dir in $packageDirs) {
    $javaRoot = Split-Path -Parent (Split-Path -Parent $dir.FullName)
    $newDir = Join-Path $javaRoot $RootPackagePath
    Move-PathIfExists $dir.FullName $newDir
    if (-not $DryRun) {
        $parent = Split-Path -Parent $dir.FullName
        while ($parent -and $parent -ne $javaRoot -and (Test-Path $parent)) {
            try { Remove-Item $parent -ErrorAction Stop } catch { break }
            $parent = Split-Path -Parent $parent
        }
    }
}
Move-PathIfExists './t-uni-common' "./$CommonModule"
Move-PathIfExists './t-uni-server' "./$ServerModule"
if ($RenameAdmin -eq 'true') {
    Move-PathIfExists './t-uni-admin' "./$AdminModule"
}

Write-Host ''
Write-Host '[2/3] 扫描并替换文本...'
$files = Get-ChildItem -Recurse -File -Force | Where-Object { -not (Should-SkipPath $_.FullName $true) }
foreach ($file in $files) {
    if (-not (Test-TextFile $file.FullName)) {
        $SkippedFiles++
        continue
    }
    $old = [IO.File]::ReadAllText($file.FullName, [Text.Encoding]::UTF8)
    $new = Convert-Text $old
    if ($old -eq $new) { continue }
    if ($DryRun) {
        Write-Host "[dry-run] 修改文本: $($file.FullName)"
    } else {
        [IO.File]::WriteAllText($file.FullName, $new, $Utf8NoBom)
        Write-Host "已修改文本: $($file.FullName)"
    }
    $UpdatedFiles++
}

Write-Host ''
Write-Host '[3/3] 人工确认提示...'
Write-Host '- 真实 .env / .env.* 不会自动修改，请按新的环境变量前缀人工同步。'
Write-Host '- 已有 Docker volume 不会迁移，如需保留数据请手工规划迁移。'
Write-Host '- OpenIM userID / MinIO bucket / 线上回调域名不会迁移，请按部署环境人工确认。'
Write-Host '- LICENSE holder 是否改名需要人工确认。'
$ManualItems += 4

Write-Host ''
Write-Host '========================================='
Write-Host '  改名脚本摘要'
Write-Host '========================================='
Write-Host "文本文件变更数: $UpdatedFiles"
Write-Host "路径重命名数: $RenamedPaths"
Write-Host "跳过文件数: $SkippedFiles"
Write-Host "人工确认项: $ManualItems"
if ($DryRun) {
    Write-Host '当前是 dry-run，未写入任何文件。确认摘要后去掉 -DryRun 再执行。'
} else {
    Write-Host '已写入文件。请继续运行 scripts/rename-project/verify-rename.ps1。'
}
