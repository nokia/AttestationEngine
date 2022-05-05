# Iot-Device-Manager

Team project for Metropolia UAS in collaboration with Nokia Bell Labs.

Authors Jaani Kaukonen, Aleksi Kosonen, Niko Lindborg and Aleksi Kytö. 

# About Iot-Device-Manager

Web application for showcasing the possibilities Nokia
Bell Lab’s Attestation Engine provides. Main focus being
the integrity of a given device while providing critical
data in altering scenarios.

With our everyday life increasingly relying on IoT-based
systems - quarateeing safe usage of such is crucial. The
core purpose of IoT Device Manager aims to provide
security for critical infrastructure such as railway
systems and medical devices, without forgetting the
rapidly growing demand for smart homes.

# Installation 

In order to run the application you will need Mongo Database, MQTT-server and an Attestation Server.

Clone repository

```
git clone https://github.com/NikoLindborg/Iot-Device-Manager
```

### To run production web application

1. Move to back-end directory

```
cd back-end
```

2. Install npm packages

```
npm i
```

3. Change url related variables in `back-end/src/utils/GlobalVariables.ts`

```
const host = 'your-mqtt-server'
const mqttPort = '1883'
const clientId = `your-mqtt-client-id`
const originalTopic = 'ANNOUNCEMENTS'
const connectUrl = `mqtt://${host}:${mqttPort}`

const a10RestApi = 'your-attestation-engine-url'
```

4. Configure your Mongo Database url

```
Either with `.env` file with MONGODB_URL = your-url

Or change url variable in `back-end/src/config/db.ts` to

const url = `${process.env.MONGODB_URL}` || '' //your url here

```

5. Start application 

```
Either start the application with nodemon by running `npm start`

Or `npm run start-production`
```

## To run front-end locally

1. Move to front-end directory
```
cd front-end
```

2. Install npm packages 
```
npm i
```

3. Change variables in `front-end/src/globals/globals.ts`
```
const wsLocalHostUrl = `ws://${your-backend-url}:8080`
const apiUrl = `your-backend-url/api/devices`
const channelUrl = `your-backend-url/api/channels`
const notificationUrl = `your-backend-url/api/notifications/`
```

4. Run the application
```
npm start
```

## To develop front-end locally and move it to back-end build file

1. Change variables back to original in `front-end/src/globals/globals.ts`
```
const wsLocalHostUrl = `ws://${hostname}:8080`
const apiUrl = `/api/devices`
const channelUrl = `/api/channels`
const notificationUrl = `/api/notifications/`
```

2. Make a build folder
```
npm run build
```

3. Move the build folder to back-end directory replacing the existing one
