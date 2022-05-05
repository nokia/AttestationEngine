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
exports.deleteDevice = exports.updateDevice = exports.setDevice = exports.getDevice = exports.getDevices = void 0;
const Device_1 = require("../schemas/Device");
/** Return list of all the devices from MongoDB */
const getDevices = (req, res) => __awaiter(void 0, void 0, void 0, function* () {
    res.set('Access-Control-Allow-Origin', '*');
    try {
        const devices = yield Device_1.Device.find({});
        res.status(200).json(devices);
    }
    catch (error) {
        console.log(error);
        res.status(400);
    }
});
exports.getDevices = getDevices;
const getDevice = (req, res) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        const device = yield Device_1.Device.findById(req.params.id);
        res.status(200).json(device);
    }
    catch (error) {
        res.status(404);
        throw new Error('Device not found!');
    }
});
exports.getDevice = getDevice;
/** Create a new device to MongoDB */
const setDevice = (newDevice, req, res) => __awaiter(void 0, void 0, void 0, function* () {
    if (!newDevice) {
        res.status(400);
        throw new Error('No device!');
    }
    try {
        const device = yield Device_1.Device.create({
            name: newDevice.name,
            trustedState: newDevice.trustedState,
            channels: newDevice.channels,
            history: newDevice.history,
            sensors: newDevice.sensors,
        });
        res.status(200).json(device);
    }
    catch (error) {
        console.log(error);
        res.status(400);
    }
});
exports.setDevice = setDevice;
/** Update a device in MongoDB by given id */
const updateDevice = (updatedDevice, req, res) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        const device = yield Device_1.Device.findByIdAndUpdate(req.params.id, updatedDevice, {
            new: true,
        });
        res.status(200).json(device);
    }
    catch (error) {
        console.log(error);
        res.status(400);
    }
});
exports.updateDevice = updateDevice;
/** Delete a device in MongoDB by given id */
const deleteDevice = (req, res) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        const device = yield Device_1.Device.findByIdAndDelete(req.params.id);
        res.status(200).json(device);
    }
    catch (error) {
        console.log(error);
        res.status(400);
    }
});
exports.deleteDevice = deleteDevice;
//# sourceMappingURL=deviceController.js.map