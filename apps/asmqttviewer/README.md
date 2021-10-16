This builds nicely under docker

```bash
docker build -t asmqttviewer .
```

To run use the interactive option as this script currently waits for the user to press a key to stop it. If you do that the script terminates along wiht the docker container

```bash
docker run -ti asmqttviewer
```
