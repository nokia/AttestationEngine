# Building for docker compose

```bash
cd ../..
sudo docker build -t u10 -f u10/Dockerfile.local .
sudo docker build -t a10rest -f a10rest/Dockerfile.local .
cd utilities/dockercomposedeployment
```

Running

```bash
sudo docker-compose up
```