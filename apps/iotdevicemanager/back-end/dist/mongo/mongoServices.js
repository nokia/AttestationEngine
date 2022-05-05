"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.updateSubscribedChannels = exports.createNewMongoDevice = exports.updateMongoDevice = void 0;
const deviceNotification_1 = require("./../schemas/deviceNotification");
const device_1 = require("../schemas/device");
const subscribedChannel_1 = require("../schemas/subscribedChannel");
const ws_1 = __importDefault(require("ws"));
const updateMongoDevice = (message, attestStatus) => {
    try {
        deviceNotification_1.DeviceNotification.create({
            deviceId: message._id,
            deviceName: message.deviceName,
            deviceChannels: message.channels,
            timestamp: message.timestamp,
            status: attestStatus,
        });
        device_1.Device.findOneAndUpdate({ _id: message._id }, {
            trustedState: attestStatus,
            $push: {
                history: {
                    name: message.deviceName,
                    timestamp: message.timestamp,
                    trustedState: attestStatus,
                },
            },
        }, () => { });
    }
    catch (error) {
        console.log('mongodb error', error);
    }
};
exports.updateMongoDevice = updateMongoDevice;
const createNewMongoDevice = (message, attestStatus, wss) => {
    try {
        device_1.Device.create({
            name: message.deviceName,
            _id: message._id,
            trustedState: attestStatus,
            channels: message.channels,
            history: [
                {
                    name: message.deviceName,
                    timestamp: message.timestamp,
                    trustedState: attestStatus,
                },
            ],
        }, (err, docs) => {
            wss.clients.forEach((client) => {
                if (client.readyState === ws_1.default.OPEN) {
                    console.log('SENT MESSAGE');
                    client.send(JSON.stringify(docs));
                }
            });
        });
        deviceNotification_1.DeviceNotification.create({
            deviceId: message._id,
            deviceName: message.deviceName,
            deviceChannels: message.channels,
            timestamp: message.timestamp,
            status: attestStatus,
        });
    }
    catch (error) {
        console.log('mongo error', error);
    }
};
exports.createNewMongoDevice = createNewMongoDevice;
const updateSubscribedChannels = (message, channel) => {
    try {
        subscribedChannel_1.SubscribedChannel.findOneAndUpdate({ name: channel }, { $push: { devices: message._id } }, (err, docs) => {
            if (!docs) {
                subscribedChannel_1.SubscribedChannel.create({
                    name: channel,
                    devices: message._id,
                });
            }
            else {
                console.log(docs);
            }
        });
    }
    catch (error) {
        console.log('mongo error', error);
    }
};
exports.updateSubscribedChannels = updateSubscribedChannels;
//# sourceMappingURL=mongoServices.js.map