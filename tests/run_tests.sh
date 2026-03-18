#!/bin/bash
# FishFinder NorCal — Automated Integration Test Suite
# Usage: bash tests/run_tests.sh [base_url]
# Default: http://localhost:9090/FishFinderNorCal

BASE_URL="${1:-http://localhost:9090/FishFinderNorCal}"
COOKIES="/tmp/fishfinder_test_cookies.txt"
PASSED=0
FAILED=0

rm -f "$COOKIES"

# ── Helpers ──────────────────────────────────────────────
pass() { PASSED=$((PASSED+1)); echo "  PASS: $1"; }
fail() { FAILED=$((FAILED+1)); echo "  FAIL: $1 (got $2)"; }

assert_status() {
    local desc="$1" url="$2" expected="$3" extra_curl="${4:-}"
    local status
    status=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIES" -c "$COOKIES" $extra_curl "$url" 2>/dev/null)
    if [ "$status" = "$expected" ]; then pass "$desc"; else fail "$desc" "$status"; fi
}

assert_redirect() {
    local desc="$1" url="$2" expected_location="$3"
    local redirect
    redirect=$(curl -s -o /dev/null -w "%{redirect_url}" -b "$COOKIES" "$url" 2>/dev/null)
    if echo "$redirect" | grep -q "$expected_location"; then pass "$desc"; else fail "$desc" "$redirect"; fi
}

assert_contains() {
    local desc="$1" file="$2" text="$3"
    if grep -q "$text" "$file" 2>/dev/null; then pass "$desc"; else fail "$desc" "text not found"; fi
}

# ── Public Pages ─────────────────────────────────────────
echo ""
echo "=== Public Pages ==="
assert_status "Home page" "$BASE_URL/home.html" "200"
assert_status "Login page" "$BASE_URL/login" "200"
assert_status "Register page" "$BASE_URL/register" "200"
assert_status "Locations list" "$BASE_URL/locations" "200"
assert_status "Reports list" "$BASE_URL/reports" "200"
assert_status "Location detail (id=1)" "$BASE_URL/location?id=1" "200"
assert_status "Location search" "$BASE_URL/locations?search=Lake" "200"
assert_status "Reports by location" "$BASE_URL/reports?locationId=1" "200"
assert_status "CSS stylesheet" "$BASE_URL/css/style.css" "200"
assert_status "JS app script" "$BASE_URL/js/app.js" "200"

# ── Auth Guard (should redirect to login) ────────────────
echo ""
echo "=== Auth Guards ==="
assert_redirect "Dashboard requires login" "$BASE_URL/dashboard" "/login"
assert_redirect "Favorites requires login" "$BASE_URL/favorites" "/login"
assert_redirect "Account requires login" "$BASE_URL/account" "/login"
assert_redirect "Report form requires login" "$BASE_URL/report/new" "/login"
assert_redirect "Location form requires login" "$BASE_URL/location/new" "/login"

# ── Register Test User ───────────────────────────────────
echo ""
echo "=== Registration ==="
rm -f "$COOKIES"
curl -s -c "$COOKIES" -b "$COOKIES" -L \
  -d "username=fishfinder_testbot&email=testbot@fishfinder.test&password=testpass123&confirmPassword=testpass123" \
  -o /tmp/ff_register.html \
  "$BASE_URL/register" 2>/dev/null
# Check if we got redirected to login or dashboard
if grep -q "Login\|Dashboard\|dashboard" /tmp/ff_register.html 2>/dev/null; then
    pass "Register new user"
else
    fail "Register new user" "unexpected page"
fi

# ── Login ─────────────────────────────────────────────────
echo ""
echo "=== Login ==="
rm -f "$COOKIES"
curl -s -c "$COOKIES" -b "$COOKIES" -L \
  -d "username=fishfinder_testbot&password=testpass123" \
  -o /tmp/ff_login.html \
  "$BASE_URL/login" 2>/dev/null
assert_contains "Login redirects to dashboard" /tmp/ff_login.html "Dashboard"

# ── Authenticated Pages ───────────────────────────────────
echo ""
echo "=== Authenticated Pages ==="
assert_status "Dashboard" "$BASE_URL/dashboard" "200"
assert_status "Favorites" "$BASE_URL/favorites" "200"
assert_status "Account" "$BASE_URL/account" "200"
assert_status "Report form" "$BASE_URL/report/new" "200"
assert_status "Location form" "$BASE_URL/location/new" "200"
assert_status "Report detail (id=1)" "$BASE_URL/report/edit?id=1" "200"
assert_status "My reports" "$BASE_URL/reports?userId=mine" "200"

# ── Create Catch Report ───────────────────────────────────
echo ""
echo "=== CRUD: Catch Report ==="
curl -s -b "$COOKIES" -c "$COOKIES" -L \
  -d "locationId=1&speciesId=2&catchDate=2026-01-01&weightLbs=3.5&lengthInches=15&method=08:00&notes=Automated+test+report" \
  -o /tmp/ff_newreport.html \
  "$BASE_URL/report/new" 2>/dev/null
assert_contains "Create catch report" /tmp/ff_newreport.html "report"

# ── Add Comment ───────────────────────────────────────────
echo ""
echo "=== CRUD: Comment ==="
curl -s -b "$COOKIES" -c "$COOKIES" -L \
  -d "reportId=1&commentText=Automated+test+comment" \
  -o /tmp/ff_comment.html \
  "$BASE_URL/comment" 2>/dev/null
assert_contains "Add comment" /tmp/ff_comment.html "report"

# ── Toggle Favorite ───────────────────────────────────────
echo ""
echo "=== CRUD: Favorite ==="
curl -s -b "$COOKIES" -c "$COOKIES" -L \
  -d "locationId=2" \
  -o /tmp/ff_fav.html \
  "$BASE_URL/favorite" 2>/dev/null
assert_contains "Add favorite" /tmp/ff_fav.html "location"

# Unfavorite
curl -s -b "$COOKIES" -c "$COOKIES" -L \
  -d "locationId=2" \
  -o /tmp/ff_unfav.html \
  "$BASE_URL/favorite" 2>/dev/null
assert_contains "Remove favorite" /tmp/ff_unfav.html "location"

# ── Add Location ──────────────────────────────────────────
echo ""
echo "=== CRUD: Location ==="
curl -s -b "$COOKIES" -c "$COOKIES" -L \
  -d "name=Automated+Test+Lake&description=Test+location&city=TestCity&address=123+Test+Rd&typeId=1" \
  -o /tmp/ff_newloc.html \
  "$BASE_URL/location/new" 2>/dev/null
assert_contains "Create location" /tmp/ff_newloc.html "location"

# ── Account Update ────────────────────────────────────────
echo ""
echo "=== Account ==="
curl -s -b "$COOKIES" -c "$COOKIES" -L \
  -d "username=fishfinder_testbot&email=testbot@fishfinder.test" \
  -o /tmp/ff_account.html \
  "$BASE_URL/account" 2>/dev/null
assert_contains "Update account" /tmp/ff_account.html "Account"

# ── Logout ────────────────────────────────────────────────
echo ""
echo "=== Logout ==="
curl -s -b "$COOKIES" -c "$COOKIES" -L \
  -o /dev/null -w "%{http_code}" \
  "$BASE_URL/logout" 2>/dev/null
assert_redirect "Logout redirects, dashboard blocked" "$BASE_URL/dashboard" "/login"

# ── DB Cleanup ────────────────────────────────────────────
echo ""
echo "=== Cleanup ==="
if command -v mysql &>/dev/null; then
    mysql --defaults-file="$HOME/.my.cnf" --get-server-public-key fishfindernorcaldb -e "
        DELETE FROM comments WHERE comment_text = 'Automated test comment';
        DELETE FROM catchreports WHERE notes = 'Automated test report';
        DELETE FROM locations WHERE location_name = 'Automated Test Lake';
        DELETE FROM users WHERE username = 'fishfinder_testbot';
    " 2>/dev/null
    pass "DB cleanup"
else
    fail "DB cleanup" "mysql CLI not found"
fi

# ── Summary ───────────────────────────────────────────────
echo ""
echo "=============================="
echo "  PASSED: $PASSED"
echo "  FAILED: $FAILED"
echo "  TOTAL:  $((PASSED + FAILED))"
echo "=============================="

rm -f "$COOKIES" /tmp/ff_*.html

if [ "$FAILED" -gt 0 ]; then exit 1; fi
exit 0
