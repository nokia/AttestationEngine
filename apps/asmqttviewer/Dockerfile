# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

FROM python:3.8-slim

WORKDIR /opt/asmqttviewer

ADD asmqttviewer.py .
ADD a10.conf /etc/a10.conf

RUN pip3 install ansicolors --proxy=http://10.144.1.10:8080
RUN pip3 install -i http://10.144.176.154:8080 a10 --trusted-host 10.144.176.154 --proxy=http://10.144.1.10:8080
#RUN pip3 install -i http://127.0.0.1:8080 a10 --trusted-host 127.0.0.1 
#RUN pip3 install flask --proxy=http://10.144.1.10:8080 

#EXPOSE 8540

CMD [ "python3", "/opt/asmqttviewer/asmqttviewer.py"]
