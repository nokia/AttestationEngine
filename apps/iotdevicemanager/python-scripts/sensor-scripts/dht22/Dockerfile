FROM python:3.8-slim
ADD servicebase.py /
ADD dht22temphumidity.py /
ADD temphum.py /
#RUN pip3 --proxy=http://10.144.1.10:8080 --trusted-host pypi.python.org --force-pi install Adafruit_DHT
#RUN pip3 --proxy=http://10.144.1.10:8080  --trusted-host pypi.python.org install paho-mqtt
#RUN pip3  --trusted-host pypi.python.org --force-pi install Adafruit_DHT
#RUN pip3   --trusted-host pypi.python.org install paho-mqtt
EXPOSE 3333
CMD [ "python", "./temphum.py"]
