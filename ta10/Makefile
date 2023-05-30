BINARY_NAME=ta10
DATE=`date +%s%3N`
LD_RELEASE_FLAGS=-X 'main.BUILD=${DATE}'

build:
	go build -o dist/${BINARY_NAME} ta10.go

clean:
	go clean
	rm dist/${BINARY_NAME}*

# Illumos and Windows commented out - I don't know of any Illumnos nor Windows that support /dev/tpm0

distribution:
	GOOS=linux GOARCH=amd64 go build   -ldflags="${LD_RELEASE_FLAGS}" -o dist/${BINARY_NAME}_linuxamd64 ta10.go
	GOOS=linux GOARCH=arm64 go build   -ldflags="${LD_RELEASE_FLAGS}" -o dist/${BINARY_NAME}_linuxarm64 ta10.go
	GOOS=linux GOARCH=arm   go build   -ldflags="${LD_RELEASE_FLAGS}" -o dist/${BINARY_NAME}_linuxarm ta10.go
