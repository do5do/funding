#!/bin/bash

APP_NAME='funding-app'
VERSION='1.0.0'

set -a
source docker/.env
set +a

echo "> app clean build"
./gradlew --build-cache clean build

echo "> app docker image build"
docker build -f docker/app/Dockerfile -t $DOCKER_REPO/$APP_NAME:$VERSION .

echo "> push to hub docker image"
docker push $DOCKER_REPO/$APP_NAME:$VERSION
