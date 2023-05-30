#!/bin/bash +x

echo "Making distribution binaries"

DISTDIR=`pwd`
GA10_BINARY_NAME=ga10
TA10_BINARY_NAME=ta10

DATE=`date +%s%3N`
LD_RELEASE_FLAGS="-X main.BUILD=$DATE"

echo "Binary release code is " $DATE

echo "Making ga10"

cd ../ga10
go get -u
go mod tidy

GOOS=linux    GOARCH=amd64 go build -ldflags="${LD_RELEASE_FLAGS}" -o $DISTDIR/${GA10_BINARY_NAME}_linuxamd64   ga10.go
GOOS=linux    GOARCH=arm64 go build -ldflags="${LD_RELEASE_FLAGS}" -o $DISTDIR/${GA10_BINARY_NAME}_linuxarm64   ga10.go
GOOS=linux    GOARCH=arm   go build -ldflags="${LD_RELEASE_FLAGS}" -o $DISTDIR/${GA10_BINARY_NAME}_linuxarm     ga10.go

GOOS=illumos  GOARCH=amd64 go build -ldflags="${LD_RELEASE_FLAGS}" -o $DISTDIR/${GA10_BINARY_NAME}_illumosamd64 ga10.go
GOOS=windows  GOARCH=amd64 go build -ldflags="${LD_RELEASE_FLAGS}" -o $DISTDIR/${GA10_BINARY_NAME}_windowsamd64 ga10.go
GOOS=openbsd  GOARCH=amd64 go build -ldflags="${LD_RELEASE_FLAGS}" -o $DISTDIR/${GA10_BINARY_NAME}_openBSDamd64 ga10.go
GOOS=freebsd  GOARCH=amd64 go build -ldflags="${LD_RELEASE_FLAGS}" -o $DISTDIR/${GA10_BINARY_NAME}_freeBSDamd64 ga10.go

echo "Making ta10"

cd ../ta10
go get -u
go mod tidy

GOOS=linux    GOARCH=amd64 go build -ldflags="${LD_RELEASE_FLAGS}" -o $DISTDIR/${TA10_BINARY_NAME}_linuxamd64   ta10.go
GOOS=linux    GOARCH=arm64 go build -ldflags="${LD_RELEASE_FLAGS}" -o $DISTDIR/${TA10_BINARY_NAME}_linuxarm64   ta10.go
GOOS=linux    GOARCH=arm   go build -ldflags="${LD_RELEASE_FLAGS}" -o $DISTDIR/${TA10_BINARY_NAME}_linuxarm     ta10.go

GOOS=illumos  GOARCH=amd64 go build -ldflags="${LD_RELEASE_FLAGS}" -o $DISTDIR/${TA10_BINARY_NAME}_illumosamd64 ta10.go
GOOS=windows  GOARCH=amd64 go build -ldflags="${LD_RELEASE_FLAGS}" -o $DISTDIR/${TA10_BINARY_NAME}_windowsamd64 ta10.go
GOOS=openbsd  GOARCH=amd64 go build -ldflags="${LD_RELEASE_FLAGS}" -o $DISTDIR/${TA10_BINARY_NAME}_openBSDamd64 ta10.go
GOOS=freebsd  GOARCH=amd64 go build -ldflags="${LD_RELEASE_FLAGS}" -o $DISTDIR/${TA10_BINARY_NAME}_freeBSDamd64 ta10.go

echo "Listing binaries"

cd $DISTDIR

ls -l ga10* ta10*

echo "Done."