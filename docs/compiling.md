# Compiling

Ensure that Go is installed and correctly configured. You will also need the Intel SGX SDK and Edgeless libraries.

The instructions presented here have been tested in Ubuntu 22.04 om AMD64.

## Install SGX SDK and Edgeless

Intel and Edgeless supply releases for Ubuntu and other operating systems. Here we should for Ubuntu Jammy (22.04). These commands might need to be run as `sudo`. Modify as appropriate for your operating system.

```bash
mkdir -p /etc/apt/keyrings 
wget -q https://download.01.org/intel-sgx/sgx_repo/ubuntu/intel-sgx-deb.key -O /etc/apt/keyrings/intel-sgx-keyring.asc 
echo "deb [signed-by=/etc/apt/keyrings/intel-sgx-keyring.asc arch=amd64] https://download.01.org/intel-sgx/sgx_repo/ubuntu jammy main" > /etc/apt/sources.list.d/intel-sgx.list 
apt update  
wget https://github.com/edgelesssys/edgelessrt/releases/download/v0.4.1/edgelessrt_0.4.1_amd64_ubuntu-22.04.deb 
apt-get install -y ./$ERT_DEB build-essential cmake libssl-dev libsgx-dcap-default-qpl libsgx-dcap-ql libsgx-dcap-quote-verify
```

## Compiling GA10

Compilation requires an up-to-date mod file and the Edgeless environment variables - which should be installed if the above went correctly. *MAKE SURE* you are in the `ga10` directory when you run these commands:

```bash
go get -u
go mod tidy
. /opt/edgelessrt/share/openenclave/openenclaverc && GOOS=linux GOARCH=amd64 go build -o ga10
```

You will now get a file called `ga10` which is your executable.

If you wish to reduce the size of the binary, run `strip ga10`

## Compiling TA10

*MAKE SURE* you are in the `ta10` directory.  TA10 is much simpler than ga10 and requires just compilation. For your local operating system and architecture you can remove the `GOOS` and `GOARCH` variables, for example as shown below. The `strip` command is optional but it does reduce the binary size a little.

```bash
go get -u
go mod tidy
go build -o ta10
strip ta10
```

For other architectures, use `go tool dist list` for a list of operating system and architecture options. Listed below are a few common options - and we like to append this to the binary name when we're generating a few of these for the devices we have (remeber amd64 is 64-bit Intel/AMD x86 based chips, eg: Xeons, i9's, i7's, Threaripper etc etc)

```bash
GOOS=linux GOARCH=arm go build -o ta10_arm                 # eg: Pi 3s
GOOS=linux GOARCH=arm64 go build -o ta10_arm64             # eg: Pi 4, 5s in 64-bit mode (also 3's I think)
GOOS=windows GOARCH=amd64 go build -o ta10_win             # eg: Pretty much every Win10, Win11 machine
GOOS=plan9 GOARCH=386 go build -o ta10_belllabs            # Because we're Bell Labs....
GOOS=linux GOARCH=s390x go build -o ta10_mainframe         # Because you either have an z-Series in the basement or Hercules
GOOS=solaris GOARCH=amd64 go build -o ta10_solaris         # I still mourn the lost of the SparcStation and UltraSparcs, RIP Sun.
GOOS=opebsd GOARCH=amd64 go build -o ta10_openbsd          # BSD for security
GOOS=freebsd GOARCH=amd64 go build -o ta10_freeebsd        # More BSD fun
GOOS=darmin GOARCH=arm64 go build -o ta10_mac              # For the Apple people out there...no TPM, but if you figure out attesting a T2 let me know
GOOS=aix GOARCH=ppc64 go build -o ta10_aix                 # If you have an AIX box, let me know...DRTM is supported during boot and a TPM too?
```

