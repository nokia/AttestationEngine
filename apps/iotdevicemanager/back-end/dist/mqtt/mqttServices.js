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
Object.defineProperty(exports, "__esModule", { value: true });
exports.sensorService = exports.announcementService = void 0;
const device_1 = require("../schemas/device");
const sensorData_1 = require("../schemas/sensorData");
const mongoServices_1 = require("../mongo/mongoServices");
const announcementService = (message, topics, client, wss) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        const attestStatus = 0;
        //await startAttestation(message._id)
        device_1.Device.find({ _id: message._id }, (err, docs) => {
            if (message.disconnect) {
                (0, mongoServices_1.updateMongoDevice)(message, 2);
                return;
            }
            if (docs.length > 0) {
                (0, mongoServices_1.updateMongoDevice)(message, attestStatus);
            }
            if (docs.length == 0) {
                (0, mongoServices_1.createNewMongoDevice)(message, attestStatus, wss);
            }
        });
        message.channels.forEach((channel) => {
            if (!topics.includes(channel)) {
                topics.push(channel);
                client.subscribe([channel], () => {
                    console.log(`Subscribe to topic ${channel}`);
                });
                (0, mongoServices_1.updateSubscribedChannels)(message, channel);
            }
        });
    }
    catch (error) {
        console.log('error occurred trying to handle announcements ', error);
    }
});
exports.announcementService = announcementService;
const sensorService = (message) => {
    sensorData_1.SensorData.create({
        deviceId: message._id,
        sensorValue: message.sensorValue,
        timestamp: message.timestamp,
        sensorType: message.sensorType,
    });
};
exports.sensorService = sensorService;
//# sourceMappingURL=mqttServices.js.map