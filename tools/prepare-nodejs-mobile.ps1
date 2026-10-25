param(
    [string]$Archive = (Join-Path $PSScriptRoot "..\.codex-tmp\nodejs-mobile-v18.20.4-16k-android.zip")
)

$ErrorActionPreference = "Stop"
$expectedHash = "83F3F6C284800FC830767B43B963E38FDECF94AC46A35DA8CBE05EF7C29E59D2"
$repoRoot = Split-Path -Parent $PSScriptRoot

if (-not (Test-Path -LiteralPath $Archive)) {
    throw "The 16 KB runtime archive is missing: $Archive. Build it with tools/build-nodejs-mobile-16k.sh and copy the resulting ZIP here. The upstream v18.20.4 ZIP is not 16 KB compatible."
}

$actualHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $Archive).Hash
if ($actualHash -ne $expectedHash) {
    throw "Node.js Mobile archive checksum mismatch. Expected $expectedHash, received $actualHash."
}

$staging = Join-Path ([System.IO.Path]::GetTempPath()) ("pocketcoded-node-" + [guid]::NewGuid())
New-Item -ItemType Directory -Path $staging | Out-Null
try {
    Expand-Archive -LiteralPath $Archive -DestinationPath $staging
    $abis = @("arm64-v8a", "armeabi-v7a", "x86_64")
    foreach ($abi in $abis) {
        if (-not (Test-Path -LiteralPath (Join-Path $staging "bin\$abi\libnode.so"))) {
            throw "Runtime archive is missing $abi/libnode.so."
        }
    }
    if (-not (Test-Path -LiteralPath (Join-Path $staging "include\node\node.h")) -or
        -not (Test-Path -LiteralPath (Join-Path $staging "LICENSE"))) {
        throw "Runtime archive is missing matching headers or its license."
    }
    & python (Join-Path $PSScriptRoot "check-native-alignment.py") (Join-Path $staging "bin")
    if ($LASTEXITCODE -ne 0) { throw "Runtime archive failed 16 KB ELF validation; nothing was installed." }
    foreach ($abi in $abis) {
        $source = Join-Path $staging "bin\$abi\libnode.so"
        $destination = Join-Path $repoRoot "app\src\main\jniLibs\$abi"
        New-Item -ItemType Directory -Force -Path $destination | Out-Null
        Copy-Item -LiteralPath $source -Destination (Join-Path $destination "libnode.so") -Force
    }

    $includeDestination = Join-Path $repoRoot "app\src\main\nodejs-mobile\include"
    New-Item -ItemType Directory -Force -Path $includeDestination | Out-Null
    Copy-Item -LiteralPath (Join-Path $staging "include\node") -Destination $includeDestination -Recurse -Force
    $licenseDestination = Join-Path $repoRoot "app\src\main\assets\licenses\nodejs-mobile-LICENSE.txt"
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $licenseDestination) | Out-Null
    Copy-Item -LiteralPath (Join-Path $staging "LICENSE") -Destination $licenseDestination -Force
    Write-Host "Prepared Node.js Mobile v18.20.4 with 16 KB support for $($abis -join ', ')."
} finally {
    $resolvedStaging = [System.IO.Path]::GetFullPath($staging)
    $tempRoot = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath()).TrimEnd('\') + '\'
    if (-not $resolvedStaging.StartsWith($tempRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to clean a staging directory outside the temporary directory."
    }
    Remove-Item -LiteralPath $resolvedStaging -Recurse -Force -ErrorAction SilentlyContinue
}
