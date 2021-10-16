# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

FROM python:3.8-slim


ENV PYTHONDONTWRITEBYTECODE=1


# Extra step to silence deps that don't obey simple "apt install -yq"
# instructions.
RUN DEBIAN_FRONTEND=noninteractive DEBCONF_NONINTERACTIVE_SEEN=true \
    apt update -yq && \
    DEBIAN_FRONTEND=noninteractive DEBCONF_NONINTERACTIVE_SEEN=true \
    apt install -y \
    -o Dpkg::Options::="--force-confdef" \
    -o Dpkg::Options::="--force-confold" \
    tzdata


################################################################################
#
# Build tpm2-tss/abrmd/tools
#
################################################################################

# Part 1: bring in the required packages
#         NB: the apt update statments here could be optimised 
# Each of these RUN statements corresponds to the tss, abrmd and tools respectively

RUN apt update -yq && \
    DEBIAN_FRONTEND=noninteractive DEBCONF_NONINTERACTIVE_SEEN=true \
  apt -y install \
  autoconf-archive \
  libcmocka0 \
  libcmocka-dev \
  procps \
  iproute2 \
  build-essential \
  git \
  pkg-config \
  gcc \
  libtool \
  automake \
  libssl-dev \
  uthash-dev \
  autoconf \
  doxygen \
  libjson-c-dev \
  libini-config-dev \
  libcurl4-openssl-dev \
  acl

RUN apt update -yq && \
    DEBIAN_FRONTEND=noninteractive DEBCONF_NONINTERACTIVE_SEEN=true \
  apt -y install \
    libglib2.0-dev

RUN apt update -yq && \
    DEBIAN_FRONTEND=noninteractive DEBCONF_NONINTERACTIVE_SEEN=true \
  apt -y install \
    autoconf automake libtool pkg-config gcc \
    libssl-dev libcurl4-gnutls-dev uuid-dev python-yaml


# Part 2: create a tss user for abrmd

RUN useradd --system --user-group tss


# Part 3: setup the working directory

RUN mkdir tpm2


# Part 4: buld tpm2-tss

WORKDIR /tpm2
RUN git clone https://github.com/tpm2-software/tpm2-tss.git && \
    git clone https://github.com/tpm2-software/tpm2-abrmd.git && \
    git clone https://github.com/tpm2-software/tpm2-tools.git

WORKDIR /tpm2/tpm2-tss
RUN ./bootstrap && \
    ./configure --with-udevrulesprefix && \
    make -j$(nproc) && \
    make install

RUN ldconfig


# Part 5: buld tpm2-abrmd

WORKDIR /tpm2/tpm2-abrmd
RUN ./bootstrap && \
    ./configure --with-dbuspolicydir=/etc/dbus-1/system.d  && \
    make -j$(nproc) && \
    make install

RUN ldconfig


# Part 6: buld tpm2-tools

WORKDIR /tpm2/tpm2-tools
RUN ./bootstrap && \
    ./configure && \
    make -j$(nproc) && \
    make install

RUN ldconfig



RUN pip3 install flask
RUN pip3 install pyyaml

ADD endpoints/ ./endpoints
ADD claims ./claims
ADD tpm ./tpm
ADD tpm2interface ./tpm2interface


ADD ta.py .
ADD ta_config.cfg .

EXPOSE 8530

CMD [ "python3", "ta.py"]
