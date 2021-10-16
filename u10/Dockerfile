# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

FROM python:3.8-slim

WORKDIR /nae/u10

RUN pip3 install flask pymongo pyserial paho-mqtt requests --proxy=http://10.144.1.10:8080

RUN pip3 install --index-url https://artifactory-espoo2.int.net.nokia.com/artifactory/api/pypi/nbl-nae-pypi-local/simple a10 -v --trusted-host https://artifactory-espoo2.int.net.nokia.com/artifactory/api/pypi/nbl-nae-pypi-local/simple  --proxy=http://10.144.1.10:8080

ADD blueprints/ ./blueprints
ADD u10.conf .
ADD static ./static
ADD templates ./templates
ADD u10.py .

# Disable the creation of Python bytecode creation and the creations of `__pycache__` directories
ENV PYTHONDONTWRITEBYTECODE=1

EXPOSE 8540

CMD [ "python3", "u10.py" ]
