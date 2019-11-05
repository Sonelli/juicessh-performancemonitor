#!/bin/bash

. release-common.sh

##
## Now test distrib

cd ../ceylon

log "Testing distrib"
# For _some_ reason, at least compiler-js does not work with "clean dist test-quick", so
# call regular test entry
ant test  2>&1 >> $LOG_FILE || fail "Testing distrib"

log "Testing SDK"
cd ../ceylon-sdk
ant -Dceylon.home=../ceylon/dist/dist test-quick  2>&1 >> $LOG_FILE || fail "Testing SDK"
cd ../ceylon

