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
const mqtt_1 = __importDefault(require("mqtt"));
const GlobalVariables_1 = require("../utils/GlobalVariables");
const subscribedChannel_1 = require("../schemas/subscribedChannel");
const mqttServices_1 = require("./mqttServices");
const websocket_1 = __importDefault(require("../websocket/websocket"));
const mqttClient = () => {
    const { wss } = (0, websocket_1.default)();
    const topics = [GlobalVariables_1.originalTopic];
    const dbTopics = () => __awaiter(void 0, void 0, void 0, function* () {
        try {
            const fetchedTopics = yield subscribedChannel_1.SubscribedChannel.find({});
            fetchedTopics.forEach((topic) => {
                topics.push(topic.name);
            });
        }
        catch (error) {
            console.log('error occurred fetching topics', error);
        }
    });
    const client = mqtt_1.default.connect(GlobalVariables_1.connectUrl, {
        clientId: GlobalVariables_1.clientId,
        clean: true,
        connectTimeout: 4000,
        username: 'emqx',
        password: 'public',
        reconnectPeriod: 1000,
    });
    try {
        client.on('connect', () => {
            console.log('connected');
            dbTopics().then(() => {
                topics.forEach((topic) => {
                    client.subscribe(topic, () => {
                        console.log(`Subscribed to topic ${topic}`);
                    });
                });
            });
        });
    }
    catch (error) {
        console.log('error occurred trying to connect to mqtt', error);
    }
    client.on('message', (topic, payload) => {
        if (topic == 'ANNOUNCEMENT') {
            try {
                const message = JSON.parse(payload.toString());
                (0, mqttServices_1.announcementService)(message, topics, client, wss);
                console.log(`Received message from topic: ${topic} reading out: ${message.channels[0]} ${message.channels[1]}`);
            }
            catch (error) {
                console.error('error occurred trying to read announcements', error);
            }
        }
        else {
            try {
                const message = JSON.parse(payload.toString());
                (0, mqttServices_1.sensorService)(message);
                console.log(`Received message from topic: ${topic} reading out: ${payload.toString()}`);
            }
            catch (error) {
                console.error('error occurred trying to read device info', error);
            }
        }
    });
};
exports.default = mqttClient;
//# sourceMappingURL=mqtt.js.map