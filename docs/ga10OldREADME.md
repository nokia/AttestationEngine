To build

make build
make distribution
make

go build .
./a10 --config=config.yaml --runtests=True


To run (without build)

go run . --config=config.yaml --runtests=True


To set build version numbers for releases

go build -ldflags="-X 'main.BUILD=123'"

with a datetime

go build -ldflags="-X 'main.BUILD=`date`'"



To generate HTTPS keys

openssl genrsa 2048 > temporary.key
chmod 400 temporary.key 
openssl req -new -x509 -nodes -sha256 -days 365 -key temporary.key -out temporary.crt




To reduce binary size

ian@ian-virtual-machine:~/ga10$ go build -ldflags "-w" .
ian@ian-virtual-machine:~/ga10$ ls -l a10 
-rwxrwxr-x 1 ian ian 13545955 huhti  25 15:37 a10
ian@ian-virtual-machine:~/ga10$ go build -ldflags "-s -w" .
ian@ian-virtual-machine:~/ga10$ ls -l a10 
-rwxrwxr-x 1 ian ian 12517376 huhti  25 15:38 a10
ian@ian-virtual-machine:~/ga10$ 


Installation

NB: choose the correct operating system and architecture

make distribution
cp dist/a10_linuxamd64 /usr/local/bin/a10
cp config.yaml /etc/a10config.yaml
cp a10.service to /etc/systemd/system

Modify the config.yaml file

sudo systemctl daemon-reload
sudo systemctl start a10.service
journalctl -xe

sudo systemctl stop a10.service


sudo systemctl enable a10.service

sudo systemctl disable a10.service




PKCS#11

Find the yubihsm.so file
$ ls /usr/lib/x86_64-linux-gnu/pkcs11/
gnome-keyring-pkcs11.so  p11-kit-client.so  p11-kit-trust.so  yubihsm_pkcs11.so

github.com/miekg/pkcs11

Or which ever user can see the YubiHSM (sudo is probably not good)
sudo yubihsm-connector -d

specify where the yubihsm conf file is
export YUBIHSM_PKCS11_CONF=/home/ian/pkcs11test/yubihsm_pkcs1.conf
