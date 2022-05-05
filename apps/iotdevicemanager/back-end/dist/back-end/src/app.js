"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
require("dotenv/config");
const app = (0, express_1.default)();
const port = 3001;
const mqtt_1 = __importDefault(require("mqtt"));
const GlobalVariables_1 = require("./utils/GlobalVariables");
const ws_1 = __importDefault(require("ws"));
const Device_1 = require("./schemas/Device");
const SubscribedChannel_1 = require("./schemas/SubscribedChannel");
const db_1 = __importDefault(require("./config/db"));
const deviceRoutes_1 = require("./routes/deviceRoutes");
const channelRoutes_1 = require("./routes/channelRoutes");
const wss = new ws_1.default.Server({ port: 8080 });
wss.on('connection', (ws) => {
    console.log('new client connected');
    ws.on('message', (data) => {
        console.log(`Client has sent us: ${data}`);
    });
    ws.on('close', () => {
        console.log('the client has connected');
    });
    ws.onerror = function () {
        console.log('Some Error occurred');
    };
});
console.log('The WebSocket server is running on port 8080');
const topics = [GlobalVariables_1.originalTopic];
(0, db_1.default)();
app.use('/api/devices', deviceRoutes_1.router);
app.use('/api/channels', channelRoutes_1.router);
app.get('/', (req, res) => {
    res.send('Hello World!');
});
const client = mqtt_1.default.connect(GlobalVariables_1.connectUrl, {
    clientId: GlobalVariables_1.clientId,
    clean: true,
    connectTimeout: 4000,
    username: 'emqx',
    password: 'public',
    reconnectPeriod: 1000,
});
client.on('connect', () => {
    console.log('connected');
    client.subscribe([GlobalVariables_1.originalTopic], () => {
        console.log(`Subscribe to topic ${GlobalVariables_1.originalTopic}`);
    });
});
client.on('message', (topic, payload) => {
    console.log(topic, 'asdasdas');
    if (topic == 'ANNOUNCEMENTS') {
        const message = JSON.parse(payload.toString());
        console.log('saataana');
        Device_1.Device.findById(message._id, (err, docs) => {
            console.log('error', docs);
            if (err) {
                Device_1.Device.create({
                    name: message.deviceName,
                    _id: message._id,
                    trustedState: 1,
                    channels: message.channels,
                    history: [
                        {
                            name: message.deviceName,
                            timestamp: message.timestamp,
                            trustedState: 1,
                        },
                    ],
                });
            }
        });
        message.channels.forEach((channel) => {
            SubscribedChannel_1.SubscribedChannel.findOneAndUpdate({ name: channel }, { $push: { devices: message._id } }, (err, docs) => {
                if (err) {
                    SubscribedChannel_1.SubscribedChannel.create({
                        name: channel,
                        devices: message._id,
                    });
                }
                else {
                    console.log(docs);
                }
            });
        });
        console.log(`Received message from topic: asdasda ${topic} reading out: ${message.channels[0]} ${message.channels[1]}`);
    }
    else {
        const message = JSON.parse(payload.toString());
        Device_1.Device.findOneAndUpdate({ _id: message._id }, {
            $push: {
                sensors: [
                    {
                        name: message.sensorType,
                        sensorValue: message.sensorValue,
                        timestamp: message.timestamp,
                    },
                ],
            },
        });
        wss.clients.forEach((client) => {
            if (client.readyState === ws_1.default.OPEN) {
                client.send(payload.toString());
            }
        });
        console.log(`Received message from topic: ${topic} reading out: ${payload.toString()}`);
    }
});
app.listen(port, () => {
    return console.log(`Express is listening at http://localhost:${port}`);
});
//testDb()
function testDb() {
    return __awaiter(this, void 0, void 0, function* () {
        const testDevice = yield Device_1.Device.create({
            name: 'First Device',
            _id: '123daj9',
            trustedState: 1,
            channels: ['Test Channel'],
            history: [
                {
                    name: 'First Device',
                    timestamp: 1649248380,
                    trustedState: 1,
                },
            ],
            sensors: [
                {
                    name: 'Temperature',
                    sensorData: [
                        {
                            sensorValue: 25,
                            timestamp: 1649248380,
                        },
                    ],
                },
            ],
        });
        const testChannels = yield SubscribedChannel_1.SubscribedChannel.create({
            name: 'Test channel',
            devices: testDevice,
        });
    });
}
// Device.find({}).then(result => {
//   result.forEach(device => {
//     console.log(device as IDevice)
//   })
// })
//# sourceMappingURL=app.js.map