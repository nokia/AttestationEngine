# Automatic Startup at Boot

In this section we present an example distribution for use in a systemd environment. We utilise the file layout given in the follow section.

## Example File Layout (Linux/BSD)

One possible layout is to put everything in `/opt`.  Note, `ga10` and `ta10` are put together just for convenience. Set permissions accordingly.

```bash
$ pwd
/opt/nae
$ ls -l
total 27364
-rw-rw-r-- 1 ian ian      706 tammi  21 13:01 config.yaml
-rwxrwxr-x 1 ian ian 19448208 tammi  21 13:00 ga10
-rwxrwxr-x 1 ian ian  8554460 tammi  21 13:02 ta10
-rw-rw-r-- 1 ian ian     1440 tammi  21 13:01 temporary.crt
-rw-rw-r-- 1 ian ian     1704 tammi  21 13:01 temporary.key
```

## GA10 and Systemd on Linux

Place the following systemd configuration in `/etc/systemd/system`  as `ga10.service`

```
[Unit]
Description=GA10 Attestation Engine
After=network.target
StartLimitIntervalSec=0

[Service]
Type=simple
Restart=always
RestartSec=1
User=ian
ExecStart=/opt/nae/ga10 -config=/opt/nae/config.yaml

[Install]
WantedBy=multi-user.target
```

Ensure the `config.yaml` is properly configured for your system and installation.

Start with `systemctl start ga10.service` and enable with `systemctl enable ga10.service`. Use `journalctl -xe` to check startup and possible errors.


## TA10 and Systemd on Linux

Place the following systemd configuration in `/etc/systemd/system`  as `ta10.service`

Note ta10 may require root to run. Take note of any security aspects.

```
[Unit]
Description=TA10 Trust Agent
After=network.target
StartLimitIntervalSec=0

[Service]
Type=simple
Restart=always
RestartSec=1
User=root
ExecStart=/opt/nae/ta10

[Install]
WantedBy=multi-user.target
```

Start with `systemctl start ta10.service` and enable with `systemctl enable ta10.service`. Use `journalctl -xe` to check startup and possible errors.

## Windows

This is possible. In respositor in `dist` is a file `ta10TrustAgent.xml` which provides some hints on this.

## BSD (rc.d)

Yes too. This script placed in `/etc/rc.d` called `ta10` works for startup, at least on my OpenBSD VM:

```
#!/bin/sh
#
# $OpenBSD: ta10

daemon="/opt/nae/ta10"

. /etc/rc.d/rc.subr

rc_cmd $1
```