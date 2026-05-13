#!/usr/bin/env bash
# build_chouten_ipa.sh
# Builds Chouten.ipa for ChoutenKMP without opening Xcode IDE.
# Run from the ChoutenKMP project root.
#
# Usage:
#   ./build_chouten_ipa.sh               # full build + package
#   ./build_chouten_ipa.sh --skip-gradle # skip Gradle (reuse last framework build)
#   ./build_chouten_ipa.sh --sign        # also sign + install via xtool after packaging
set -euo pipefail

# ── Project config ────────────────────────────────────────────────────────────
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
XCODEPROJ="$PROJECT_ROOT/iosApp/iosApp.xcodeproj"
SCHEME="iosApp"
PRODUCT_NAME="Chouten"
BUNDLE_ID="com.inumaki.chouten.Chouten"
CONFIGURATION="Release"
GRADLE_LINK_TASK="linkReleaseFrameworkIosArm64"
OUTPUT_DIR="$PROJECT_ROOT/ipa_out"
BUILD_DIR="$PROJECT_ROOT/build/xcodebuild"
# ─────────────────────────────────────────────────────────────────────────────

SKIP_GRADLE=false
DO_SIGN=false

while [[ $# -gt 0 ]]; do
    case "$1" in
        --skip-gradle) SKIP_GRADLE=true; shift ;;
        --sign)        DO_SIGN=true; shift ;;
        --help|-h)
            echo "Usage: $0 [--skip-gradle] [--sign]"
            echo "  --skip-gradle  Skip ./gradlew $GRADLE_LINK_TASK (reuse existing framework)"
            echo "  --sign         After packaging, run: xtool install $OUTPUT_DIR/${PRODUCT_NAME}.ipa"
            exit 0 ;;
        *) echo "Unknown argument: $1"; exit 1 ;;
    esac
done

IPA_PATH="$OUTPUT_DIR/${PRODUCT_NAME}.ipa"

# ── Helpers ───────────────────────────────────────────────────────────────────
info()    { printf '\033[34m▶\033[0m %s\n' "$*"; }
success() { printf '\033[32m✓\033[0m %s\n' "$*"; }
fail()    { printf '\033[31m✗\033[0m %s\n' "$*" >&2; exit 1; }
step()    { printf '\n\033[1m── %s\033[0m\n' "$*"; }

# ── Sanity checks ─────────────────────────────────────────────────────────────
[[ -f "$PROJECT_ROOT/gradlew" ]]  || fail "gradlew not found. Run from ChoutenKMP project root."
[[ -d "$XCODEPROJ" ]]             || fail "Xcode project not found: $XCODEPROJ"
command -v xcodebuild &>/dev/null || fail "xcodebuild not found. Install Xcode Command Line Tools: xcode-select --install"

mkdir -p "$OUTPUT_DIR" "$BUILD_DIR"

# ── Step 1: Gradle — build Kotlin/Native framework ───────────────────────────
if [[ "$SKIP_GRADLE" == false ]]; then
    step "Step 1/3: Gradle — $GRADLE_LINK_TASK"
    info "Running: ./gradlew $GRADLE_LINK_TASK"
    cd "$PROJECT_ROOT"
    ./gradlew "$GRADLE_LINK_TASK" --no-daemon \
        || fail "Gradle build failed. Check output above."
    success "Kotlin/Native framework built."
else
    step "Step 1/3: Gradle — skipped (--skip-gradle)"
fi

# ── Step 2: xcodebuild — build the iOS app shell ─────────────────────────────
step "Step 2/3: xcodebuild — building $SCHEME ($CONFIGURATION)"

# xcodebuild without the Xcode IDE: no indexing, no UI, no simulator overhead.
# CODE_SIGNING_ALLOWED=NO — skip signing here; xtool handles that separately.
# generic/platform=iOS — builds for arm64 device, not simulator.

XCODEBUILD_ARGS=(
    -project "$XCODEPROJ"
    -scheme "$SCHEME"
    -configuration "$CONFIGURATION"
    -destination "generic/platform=iOS"
    -derivedDataPath "$BUILD_DIR"
    CODE_SIGNING_ALLOWED=NO
    CODE_SIGNING_REQUIRED=NO
    CODE_SIGN_IDENTITY=""
    ONLY_ACTIVE_ARCH=NO
)

# Use xcpretty for cleaner output if available, otherwise raw xcodebuild
if command -v xcpretty &>/dev/null; then
    info "Running xcodebuild (output via xcpretty)..."
    xcodebuild "${XCODEBUILD_ARGS[@]}" | xcpretty
else
    info "Running xcodebuild (install xcpretty for cleaner output: gem install xcpretty)..."
    xcodebuild "${XCODEBUILD_ARGS[@]}"
fi

# Locate the built .app (exclude PlugIns subdirs and Simulator builds)
APP_PATH=$(find "$BUILD_DIR" \
    -name "*.app" \
    -not -path "*/PlugIns/*" \
    -not -path "*Simulator*" \
    -not -path "*iphonesimulator*" \
    | head -1)

[[ -n "$APP_PATH" && -d "$APP_PATH" ]] \
    || fail "Could not find .app under $BUILD_DIR. Check xcodebuild output above."

success "Built .app: $(basename "$APP_PATH")"

# ── Step 3: Package as IPA ────────────────────────────────────────────────────
step "Step 3/3: Packaging → ${PRODUCT_NAME}.ipa"

WORK_DIR=$(mktemp -d)
trap 'rm -rf "$WORK_DIR"' EXIT

PAYLOAD_DIR="$WORK_DIR/Payload"
mkdir -p "$PAYLOAD_DIR"

APP_BASENAME=$(basename "$APP_PATH")
info "Copying $APP_BASENAME → Payload/..."
cp -R "$APP_PATH" "$PAYLOAD_DIR/$APP_BASENAME"

info "Zipping Payload/..."
(cd "$WORK_DIR" && zip -qr "${PRODUCT_NAME}.zip" Payload/)
mv "$WORK_DIR/${PRODUCT_NAME}.zip" "$IPA_PATH"

success "IPA ready: $IPA_PATH"

# ── Optional: sign + install via xtool ───────────────────────────────────────
if [[ "$DO_SIGN" == true ]]; then
    step "Signing & installing via xtool"
    command -v xtool &>/dev/null || fail "xtool not found. Install from https://xtool.sh"
    xtool install "$IPA_PATH"
    success "Installed to device."
else
    echo ""
    info "To sign and sideload, run:"
    info "  xtool install \"$IPA_PATH\""
fi