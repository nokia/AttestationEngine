#!/bin/sh -x

echo "Installing Fake Measured Boot Files"

mkdir /boot/measuredboot
cp -r * /boot/measuredboot
cp measure.service /etc/systemd/system

touch /var/log/measuredBootLog

systemctl enable measure.service

echo "Now select which version by copying either measure.start.tpm2tools or measure.start.eltt2 to measure.start in /boot/measuredboot."