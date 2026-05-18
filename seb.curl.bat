@echo off
REM --------------------------------------------------
REM Sports Exercise Battle (SEB)
REM --------------------------------------------------
title Sports Exercise Battle (SEB)
echo CURL Testing for Sports Exercise Battle (SEB)
echo.

REM --------------------------------------------------
echo 1) Create Users (Registration)
echo --- EXPECTED: 201 - User registered successfully ---
curl -X POST http://localhost:10001/users --header "Content-Type: application/json" -d "{\"username\":\"kienboec\", \"password\":\"daniel\"}"
echo.
echo --- EXPECTED: 201 - User registered successfully ---
curl -X POST http://localhost:10001/users --header "Content-Type: application/json" -d "{\"username\":\"altenhof\", \"password\":\"markus\"}"
echo.

echo should fail:
echo --- EXPECTED: 409 - Username already taken ---
curl -X POST http://localhost:10001/users --header "Content-Type: application/json" -d "{\"username\":\"kienboec\", \"password\":\"daniel\"}"
echo.
echo --- EXPECTED: 409 - Username already taken ---
curl -X POST http://localhost:10001/users --header "Content-Type: application/json" -d "{\"username\":\"kienboec\", \"password\":\"different\"}"
echo.
echo --- EXPECTED: 400 - Username must not be blank ---
curl -X POST http://localhost:10001/users --header "Content-Type: application/json" -d "{\"username\":\"\", \"password\":\"daniel\"}"
echo.
echo.

REM --------------------------------------------------
echo 2) Login Users
echo --- EXPECTED: 200 - token kienboec-sebToken ---
curl -X POST http://localhost:10001/sessions --header "Content-Type: application/json" -d "{\"username\":\"kienboec\", \"password\":\"daniel\"}"
echo.
echo --- EXPECTED: 200 - token altenhof-sebToken ---
curl -X POST http://localhost:10001/sessions --header "Content-Type: application/json" -d "{\"username\":\"altenhof\", \"password\":\"markus\"}"
echo.

echo should fail:
echo --- EXPECTED: 401 - Invalid credentials ---
curl -X POST http://localhost:10001/sessions --header "Content-Type: application/json" -d "{\"username\":\"kienboec\", \"password\":\"different\"}"
echo.
echo --- EXPECTED: 401 - Invalid credentials ---
curl -X POST http://localhost:10001/sessions --header "Content-Type: application/json" -d "{\"username\":\"nobody\", \"password\":\"pass\"}"
echo.
echo.

REM --------------------------------------------------
echo 3) edit user data
echo.
echo --- EXPECTED: 200 - kienboec profile (name/bio/image null, elo 100) ---
curl -X GET http://localhost:10001/users/kienboec --header "Authorization: Bearer kienboec-sebToken"
echo.
echo --- EXPECTED: 200 - altenhof profile (name/bio/image null, elo 100) ---
curl -X GET http://localhost:10001/users/altenhof --header "Authorization: Bearer altenhof-sebToken"
echo.
echo --- EXPECTED: 200 - Profile updated for kienboec ---
curl -X PUT http://localhost:10001/users/kienboec --header "Content-Type: application/json" --header "Authorization: Bearer kienboec-sebToken" -d "{\"name\": \"Kienboeck\",  \"bio\": \"me playin...\", \"image\": \":-)\"}"
echo.
echo --- EXPECTED: 200 - Profile updated for altenhof ---
curl -X PUT http://localhost:10001/users/altenhof --header "Content-Type: application/json" --header "Authorization: Bearer altenhof-sebToken" -d "{\"name\": \"Altenhofer\", \"bio\": \"me codin...\",  \"image\": \":-D\"}"
echo.
echo --- EXPECTED: 200 - kienboec profile (name Kienboeck, bio me playin...) ---
curl -X GET http://localhost:10001/users/kienboec --header "Authorization: Bearer kienboec-sebToken"
echo.
echo --- EXPECTED: 200 - altenhof profile (name Altenhofer, bio me codin...) ---
curl -X GET http://localhost:10001/users/altenhof --header "Authorization: Bearer altenhof-sebToken"
echo.
echo.

echo should fail:
echo --- EXPECTED: 403 - Not allowed to view this profile ---
curl -X GET http://localhost:10001/users/altenhof --header "Authorization: Bearer kienboec-sebToken"
echo.
echo --- EXPECTED: 403 - Not allowed to view this profile ---
curl -X GET http://localhost:10001/users/kienboec --header "Authorization: Bearer altenhof-sebToken"
echo.
echo --- EXPECTED: 403 - Not allowed to edit ---
curl -X PUT http://localhost:10001/users/kienboec --header "Content-Type: application/json" --header "Authorization: Bearer altenhof-sebToken" -d "{\"name\": \"Hoax\",  \"bio\": \"me playin...\", \"image\": \":-)\"}"
echo.
echo --- EXPECTED: 403 - Not allowed to edit ---
curl -X PUT http://localhost:10001/users/altenhof --header "Content-Type: application/json" --header "Authorization: Bearer kienboec-sebToken" -d "{\"name\": \"Hoax\", \"bio\": \"me codin...\",  \"image\": \":-D\"}"
echo.
echo --- EXPECTED: 403 - Not allowed to view this profile ---
curl -X GET http://localhost:10001/users/someGuy  --header "Authorization: Bearer kienboec-sebToken"
echo.
echo --- EXPECTED: 401 - Invalid or missing token ---
curl -X GET http://localhost:10001/users/kienboec --header "Authorization: Bearer invalidToken"
echo.
echo.

REM --------------------------------------------------
echo 4) stats (get my elo value and count of pushups overall; startup value e.g. 100)
echo --- EXPECTED: 200 - kienboec elo 100, totalPushUps 0, totalEntries 0 ---
curl -X GET http://localhost:10001/stats --header "Authorization: Bearer kienboec-sebToken"
echo.
echo --- EXPECTED: 200 - altenhof elo 100, totalPushUps 0, totalEntries 0 ---
curl -X GET http://localhost:10001/stats --header "Authorization: Bearer altenhof-sebToken"
echo.
echo --- EXPECTED: 401 - Invalid or missing token ---
curl -X GET http://localhost:10001/stats --header "Authorization: Bearer invalidToken"
echo.
echo.

REM --------------------------------------------------
echo 5) scoreboard (compare elo values and count of pushups accross all users)
echo --- EXPECTED: 200 - both users elo 100, pushups 0 ---
curl -X GET http://localhost:10001/score --header "Authorization: Bearer kienboec-sebToken"
echo.
echo.

REM --------------------------------------------------
echo 6) history (count and duration; currently empty)
echo --- EXPECTED: 200 - empty list [] ---
curl -X GET http://localhost:10001/history --header "Authorization: Bearer kienboec-sebToken"
echo.
echo --- EXPECTED: 200 - empty list [] ---
curl -X GET http://localhost:10001/history --header "Authorization: Bearer altenhof-sebToken"
echo.
echo.

REM --------------------------------------------------
echo 7) list current tournament info/state (currently none)
echo --- EXPECTED: 200 - No tournaments yet ---
curl -X GET http://localhost:10001/tournament --header "Authorization: Bearer kienboec-sebToken"
echo.
echo --- EXPECTED: 200 - No tournaments yet ---
curl -X GET http://localhost:10001/tournament --header "Authorization: Bearer altenhof-sebToken"
echo.
echo.

REM --------------------------------------------------
echo 8) add entry to history / starts a tournament
echo --- EXPECTED: 201 - entry saved, tournamentId set, count 40 ---
curl -X POST http://localhost:10001/history --header "Content-Type: application/json" --header "Authorization: Bearer kienboec-sebToken" -d "{\"name\": \"PushUps\",  \"count\": 40, \"duration\": 60}"
echo.
echo --- EXPECTED: 201 - entry saved, same tournamentId, count 50 ---
curl -X POST http://localhost:10001/history --header "Content-Type: application/json" --header "Authorization: Bearer altenhof-sebToken" -d "{\"name\": \"PushUps\",  \"count\": 50, \"duration\": 70}"
echo.
echo.

REM --------------------------------------------------
echo 9) list current tournament info/state (tournament started; 2 participants; altenhof in front; write start-time)
echo --- EXPECTED: 200 - tournament state RUNNING ---
curl -X GET http://localhost:10001/tournament --header "Authorization: Bearer kienboec-sebToken"
echo.
echo --- EXPECTED: 200 - tournament state RUNNING ---
curl -X GET http://localhost:10001/tournament --header "Authorization: Bearer altenhof-sebToken"
echo.
echo.

REM --------------------------------------------------
echo 10) stats (get my elo value and count of pushups overall; startup value like 100 - no tournament should be finished here)
echo --- EXPECTED: 200 - kienboec elo 100 (unchanged), totalPushUps 40 ---
curl -X GET http://localhost:10001/stats --header "Authorization: Bearer kienboec-sebToken"
echo.
echo --- EXPECTED: 200 - altenhof elo 100 (unchanged), totalPushUps 50 ---
curl -X GET http://localhost:10001/stats --header "Authorization: Bearer altenhof-sebToken"
echo.
echo.

REM --------------------------------------------------
echo 11) scoreboard (compare elo values and count of pushups accross all users; still startup values)
echo --- EXPECTED: 200 - both elo 100, kienboec 40 pushups, altenhof 50 pushups ---
curl -X GET http://localhost:10001/score --header "Authorization: Bearer kienboec-sebToken"
echo.
echo.

REM --------------------------------------------------
echo 12) history (count and duration; 1 entry each)
echo --- EXPECTED: 200 - 1 entry, count 40 ---
curl -X GET http://localhost:10001/history --header "Authorization: Bearer kienboec-sebToken"
echo.
echo --- EXPECTED: 200 - 1 entry, count 50 ---
curl -X GET http://localhost:10001/history --header "Authorization: Bearer altenhof-sebToken"
echo.
echo.

REM --------------------------------------------------
echo 13) add entry to history / continues in tournament
echo --- EXPECTED: 201 - entry saved, same tournamentId as before, count 11 ---
curl -X POST http://localhost:10001/history --header "Content-Type: application/json" --header "Authorization: Bearer kienboec-sebToken" -d "{\"name\": \"PushUps\",  \"count\": 11, \"duration\": 25}"
echo.
echo.

REM --------------------------------------------------
echo 14) list current tournament info/state
echo --- EXPECTED: 200 - tournament state still RUNNING ---
curl -X GET http://localhost:10001/tournament --header "Authorization: Bearer kienboec-sebToken"
echo.
echo --- EXPECTED: 200 - tournament state still RUNNING ---
curl -X GET http://localhost:10001/tournament --header "Authorization: Bearer altenhof-sebToken"
echo.
echo.

REM --------------------------------------------------
echo 15) sleep of 2min (afterwards the tournament should be over and elo values need to be updated)
ping localhost -n 120 >NUL 2>NUL
echo.
echo.

REM --------------------------------------------------
echo 16) list current tournament info/state (1 tournament with state ended)
echo --- EXPECTED: 200 - tournament state ENDED ---
curl -X GET http://localhost:10001/tournament --header "Authorization: Bearer kienboec-sebToken"
echo.
echo --- EXPECTED: 200 - tournament state ENDED ---
curl -X GET http://localhost:10001/tournament --header "Authorization: Bearer altenhof-sebToken"
echo.
echo.

REM --------------------------------------------------
echo 17) stats
echo --- EXPECTED: 200 - kienboec elo 102 (+2 winner, sum 51), totalPushUps 51 ---
curl -X GET http://localhost:10001/stats --header "Authorization: Bearer kienboec-sebToken"
echo.
echo --- EXPECTED: 200 - altenhof elo 99 (-1 loser, sum 50), totalPushUps 50 ---
curl -X GET http://localhost:10001/stats --header "Authorization: Bearer altenhof-sebToken"
echo.
echo.

REM --------------------------------------------------
echo 18) scoreboard
echo --- EXPECTED: 200 - kienboec first (elo 102), altenhof second (elo 99) ---
curl -X GET http://localhost:10001/score --header "Authorization: Bearer kienboec-sebToken"
echo.
echo.

REM --------------------------------------------------
echo 19) history
echo --- EXPECTED: 200 - 2 entries for kienboec (count 40 and 11) ---
curl -X GET http://localhost:10001/history --header "Authorization: Bearer kienboec-sebToken"
echo.
echo --- EXPECTED: 200 - 1 entry for altenhof (count 50) ---
curl -X GET http://localhost:10001/history --header "Authorization: Bearer altenhof-sebToken"
echo.
echo.

REM --------------------------------------------------
echo 20) logout
echo --- EXPECTED: 200 - logout successful ---
curl -X DELETE http://localhost:10001/sessions --header "Authorization: Bearer kienboec-sebToken"
echo.
echo --- EXPECTED: 401 - token no longer valid after logout ---
curl -X GET http://localhost:10001/stats --header "Authorization: Bearer kienboec-sebToken"
echo.
echo.

REM --------------------------------------------------
echo 21) unknown route
echo --- EXPECTED: 404 - Route not found ---
curl -X GET http://localhost:10001/unknown --header "Authorization: Bearer altenhof-sebToken"
echo.
echo.

REM --------------------------------------------------
echo end...

@echo on

pause