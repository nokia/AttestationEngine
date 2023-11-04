FROM golang:1.21-bookworm AS build


RUN mkdir -p /etc/apt/keyrings && \
    wget -q https://download.01.org/intel-sgx/sgx_repo/ubuntu/intel-sgx-deb.key -O /etc/apt/keyrings/intel-sgx-keyring.asc && \
    echo "deb [signed-by=/etc/apt/keyrings/intel-sgx-keyring.asc arch=amd64] https://download.01.org/intel-sgx/sgx_repo/ubuntu jammy main" > /etc/apt/sources.list.d/intel-sgx.list && \
    apt update && \ 
    ERT_DEB=edgelessrt_0.4.1_amd64_ubuntu-22.04.deb && \
    wget https://github.com/edgelesssys/edgelessrt/releases/download/v0.4.1/$ERT_DEB && \
    apt-get install -y ./$ERT_DEB build-essential cmake libssl-dev libsgx-dcap-default-qpl libsgx-dcap-ql libsgx-dcap-quote-verify

# Set destination for COPY
WORKDIR /app

# Download Go modules
COPY ./go.mod ./go.sum ./
RUN go mod download

COPY . ./

RUN go get -u
RUN go mod tidy
RUN . /opt/edgelessrt/share/openenclave/openenclaverc && GOOS=linux GOARCH=amd64 go build  -o /ga10   ga10.go

CMD ["/ga10"]