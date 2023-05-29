BINARY_NAME=ga10
DATE=`date +%s%3N`
LD_RELEASE_FLAGS=-X 'main.BUILD=${DATE}'

build:
	go build -o dist/${BINARY_NAME} ga10.go

clean:
	go clean
	rm dist/${BINARY_NAME}*

update:
	go get -u
	go mod tidy

distribution: update
	GOOS=linux GOARCH=amd64 go build   -ldflags="${LD_RELEASE_FLAGS}" -o dist/${BINARY_NAME}_linuxamd64 ga10.go
	GOOS=illumos GOARCH=amd64 go build -ldflags="${LD_RELEASE_FLAGS}" -o dist/${BINARY_NAME}_illumosamd64 ga10.go
	GOOS=windows GOARCH=amd64 go build -ldflags="${LD_RELEASE_FLAGS}" -o dist/${BINARY_NAME}_windowsamd64 ga10.go
	GOOS=linux GOARCH=arm64 go build   -ldflags="${LD_RELEASE_FLAGS}" -o dist/${BINARY_NAME}_linuxarm64 ga10.go
	GOOS=linux GOARCH=arm go build     -ldflags="${LD_RELEASE_FLAGS}" -o dist/${BINARY_NAME}_linuxarm ga10.go
	GOOS=openbsd GOARCH=amd64 go build -ldflags="${LD_RELEASE_FLAGS}" -o dist/${BINARY_NAME}_openBSDamd64 ga10.go
