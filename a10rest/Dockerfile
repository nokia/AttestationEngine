# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

FROM python:3.8-slim

WORKDIR /nae/a10rest

RUN pip3 install flask requests pymongo paho-mqtt pyserial --proxy=http://10.144.1.10:8080 

RUN pip3 install --index-url https://artifactory-espoo2.int.net.nokia.com/artifactory/api/pypi/nbl-nae-pypi-local/simple a10 -v --trusted-host https://artifactory-espoo2.int.net.nokia.com/artifactory/api/pypi/nbl-nae-pypi-local/simple  --proxy=http://10.144.1.10:8080


ADD a10rest.py .
ADD a10rest.conf .
ADD static/* ./static/

# Disable the creation of Python bytecode creation and the creations of `__pycache__` directories
ENV PYTHONDONTWRITEBYTECODE=1

EXPOSE 8520

CMD [ "python3", "a10rest.py"]
