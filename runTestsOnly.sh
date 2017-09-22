#!/bin/bash

set -o errexit

# ======================================= FUNCTIONS START =======================================

CLOUD_DOMAIN=${DOMAIN:-system.pcfeu.dev.dynatracelabs.com}
CLOUD_TARGET=api.${DOMAIN}
CLOUD_PREFIX="docssleuth"

function app_domain(){
    D=`cf apps | grep $1 | tr -s ' ' | cut -d' ' -f 6 | cut -d, -f1`
    echo $D
}


# ====================================================

SERVICE1_HOST=`app_domain ${CLOUD_PREFIX}-service1`
ZIPKIN_SERVER_HOST=`app_domain ${CLOUD_PREFIX}-zipkin-server`
echo -e "Service1 host is [${SERVICE1_HOST}]"
echo -e "Zikpin server host is [${ZIPKIN_SERVER_HOST}]"


# ======================================= TEST START =======================================

echo -e "Running acceptance tests"

ACCEPTANCE_TEST_OPTS="-DLOCAL_URL=http://${ZIPKIN_SERVER_HOST} -DserviceUrl=http://${SERVICE1_HOST} -Dzipkin.query.port=80"
echo -e "\n\nSetting test opts for sleuth stream to call ${ACCEPTANCE_TEST_OPTS}"
./gradlew :acceptance-tests:acceptanceTests "-DLOCAL_URL=http://${ZIPKIN_SERVER_HOST}" "-DserviceUrl=http://${SERVICE1_HOST}" "-Dzipkin.query.port=80" --stacktrace --no-daemon --configure-on-demand

# ======================================= TEST END   =======================================
