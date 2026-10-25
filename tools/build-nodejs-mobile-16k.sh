#!/usr/bin/env bash
# Run on Linux/WSL with gcc, g++, make, curl, unzip, rsync and Python 3.11 on PATH.
# Builds the same Node.js version as the app, with upstream PR #154's linker flag.
set -euo pipefail
tools_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
build_root="${1:?Usage: build-nodejs-mobile-16k.sh BUILD_DIRECTORY [JOBS]}"
jobs="${2:-4}"
mkdir -p "$build_root"
cd "$build_root"
build_root="$PWD"

download() {
    local name="$1" url="$2" hash="$3"
    if [[ ! -f "$name" ]]; then curl -fL --retry 3 "$url" -o "$name"; fi
    printf '%s  %s\n' "$hash" "$name" | sha256sum -c -
}
download source.tar.gz \
    https://codeload.github.com/nodejs-mobile/nodejs-mobile/tar.gz/refs/tags/v18.20.4 \
    62a2d3cca8c5ee89b6c40b52d8189dc915c8cae6e48dda2c1ad9e5f9d5f668f3
download ndk-r24.zip https://dl.google.com/android/repository/android-ndk-r24-linux.zip \
    caac638f060347c9aae994e718ba00bb18413498d8e0ad4e12e1482964032997
if [[ ! -d android-ndk-r24 ]]; then unzip -q ndk-r24.zip; fi
download upstream-android.zip \
    https://github.com/nodejs-mobile/nodejs-mobile/releases/download/v18.20.4/nodejs-mobile-v18.20.4-android.zip \
    bd7321eaa1a7602fbe0bb87302df2d79d87835cf4363fbdd17c350dbb485c2af
# The 32-bit runtime is unchanged; the 16 KB requirement concerns 64-bit ABIs.
mkdir -p dist/bin/armeabi-v7a
unzip -p upstream-android.zip bin/armeabi-v7a/libnode.so > dist/bin/armeabi-v7a/libnode.so
export LDFLAGS='-Wl,-z,max-page-size=16384'
for arch in x86_64 arm64; do
    abi="$arch"
    [[ "$arch" != arm64 ]] || abi=arm64-v8a
    source_dir="$build_root/source-$arch"
    if [[ ! -d "$source_dir" ]]; then
        mkdir "$source_dir"
        tar -xzf source.tar.gz --strip-components=1 -C "$source_dir"
    fi
    (
        cd "$source_dir"
        ./android-configure "$build_root/android-ndk-r24" 24 "$arch"
        make -j"$jobs"
        mkdir -p "$build_root/dist/bin/$abi"
        cp out/Release/obj.target/libnode.so "$build_root/dist/bin/$abi/libnode.so"
        "$build_root/android-ndk-r24/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-strip" \
            --strip-unneeded "$build_root/dist/bin/$abi/libnode.so"
        # Export matching generated headers as well as upstream notices.
        bash tools/copy_libnode_headers.sh android
        mkdir -p "$build_root/dist/include"
        cp -R out_android/libnode/include/node "$build_root/dist/include/"
        cp LICENSE "$build_root/dist/LICENSE"
    )
done
python3 "$tools_dir/check-native-alignment.py" "$build_root/dist"
(
    cd "$build_root/dist"
    python3 -m zipfile -c ../nodejs-mobile-v18.20.4-16k-android.zip bin include LICENSE
)
sha256sum "$build_root/nodejs-mobile-v18.20.4-16k-android.zip"
echo "Validate and pin the archive checksum before importing with prepare-nodejs-mobile.ps1."
