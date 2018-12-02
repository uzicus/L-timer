#!/bin/bash

FLAVOR_NAME=$1;
OPTIONS=$2;

if [[ -z ${TELEGRAM_BOT_ID} ]]
then
    echo "TELEGRAM_BOT_ID empty!"
    exit 1;
fi

if [[ -z ${TELEGRAM_CHAT_ID} ]]
then
    echo "TELEGRAM_CHAT_ID empty!"
    exit 1;
fi

#Build project
echo "Clean gradle"
./gradlew clean

buildDescription="
Version name: $(grep "versionName " ./app/build.gradle | awk '{print $2}' | sed -e 's/^"//' -e 's/"$//')
Build Number: ${TRAVIS_BUILD_NUMBER}
Branch: ${TRAVIS_BRANCH}
Flavor: ${FLAVOR_NAME}
Commit hash: ${TRAVIS_COMMIT:0:7}
Commit title: ${TRAVIS_COMMIT_MESSAGE}
Log is available at the link https://api.travis-ci.com/v3/job/${TRAVIS_JOB_ID}/log.txt"

flavorName=""
if [[ "$OPTIONS" == "gradle=v3" ]]
then
    if [[ -n ${FLAVOR_NAME} ]]
    then
        flavorName=`perl -e 'my ($fn) = @ARGV; $fn =~ s/^([a-z])/\u$1/g; print "$fn\n";' ${FLAVOR_NAME}`
        flavorName=`perl -e 'my ($fn) = @ARGV; $fn =~ s/-([a-z])/\u$1/g; print "$fn\n";' ${flavorName}`
    fi
fi

if [[ "$TRAVIS_BRANCH" == "master" || "$TRAVIS_BRANCH" == "release" ]]
then
    echo "Build release app"
    BUILD_NUMBER=${TRAVIS_BUILD_NUMBER} ./gradlew assemble${flavorName}Release
    apkType="release"

    if [[ -z ${FLAVOR_NAME} ]]
    then
        apkName="app-release.apk"
    else
        apkName="app-${FLAVOR_NAME}-release.apk"
    fi
else
    echo "Build debug app"
    BUILD_NUMBER=${TRAVIS_BUILD_NUMBER} ./gradlew assemble${flavorName}Debug
    apkType="debug"

    if [[ -z ${FLAVOR_NAME} ]]
    then
        apkName="app-debug.apk"
    else
        apkName="app-${FLAVOR_NAME}-debug.apk"
    fi
fi
rc=$?

if [[ ${rc} != 0 ]]
then
    echo "Failed to compile the build with ret code $rc"

    # Notify the channel
    message=
"FAIL
$buildDescription"

    curl \
        -F text="$message" \
        -F chat_id=${TELEGRAM_CHAT_ID} \
        https://api.telegram.org/bot${TELEGRAM_BOT_ID}/sendMessage

    exit 3;
fi

apkPath="app/build/outputs/apk"
if [[ "$OPTIONS" == "gradle=v3" ]]
then
    if [[ -z ${FLAVOR_NAME} ]]
    then
        apkPath="app/build/outputs/apk/${apkType}"
    else
        flavorDir=`perl -e 'my ($fn) = @ARGV; $fn =~ s/-([a-z])/\u$1/g; print "$fn\n";' ${FLAVOR_NAME}`
        apkPath="app/build/outputs/apk/${flavorDir}/${apkType}"
    fi
fi

if [[ $rc != 0 ]]
then
    echo "Failed to distribute the build with ret code $rc"

    # Notify the channel
    message=
"FAIL
$buildDescription"

    curl \
        -F text="$message" \
        -F chat_id=${TELEGRAM_CHAT_ID} \
        https://api.telegram.org/bot${TELEGRAM_BOT_ID}/sendMessage

    exit 4;
fi

echo "Notify the channel"

curl \
    -F document=@"${apkPath}/${apkName}" \
    -F caption="${buildDescription}" \
    -F chat_id=${TELEGRAM_CHAT_ID} \
    https://api.telegram.org/bot${TELEGRAM_BOT_ID}/sendDocument

#exit $?
