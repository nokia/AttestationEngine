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
exports.deleteDeviceNotification = exports.deleteAllNotifications = exports.setDeviceNotification = exports.getDeviceNotifications = void 0;
const deviceNotification_1 = require("../schemas/deviceNotification");
/** Return list of all the notifications from MongoDB */
const getDeviceNotifications = (req, res) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        const notifications = yield deviceNotification_1.DeviceNotification.find({}).sort({ timestamp: -1 });
        res.status(200).json(notifications);
    }
    catch (error) {
        console.log(error);
        res.status(400);
    }
});
exports.getDeviceNotifications = getDeviceNotifications;
/** Create a new notification to MongoDB */
const setDeviceNotification = (newNotification, req, res) => __awaiter(void 0, void 0, void 0, function* () {
    if (!newNotification) {
        res.status(400);
        throw new Error('No notifications');
    }
    try {
        const notification = yield deviceNotification_1.DeviceNotification.create({
            deviceId: newNotification.deviceId,
            deviceName: newNotification.deviceName,
            deviceChannels: newNotification.deviceChannels,
            timestamp: newNotification.timestamp,
        });
        res.status(200).json(notification);
    }
    catch (error) {
        console.log(error);
        res.status(400);
    }
});
exports.setDeviceNotification = setDeviceNotification;
/** Delete a notification in MongoDB by given id */
const deleteAllNotifications = (req, res) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        const notification = yield deviceNotification_1.DeviceNotification.deleteMany({});
        res.status(200).json(notification);
    }
    catch (error) {
        console.log(error);
        res.status(400);
    }
});
exports.deleteAllNotifications = deleteAllNotifications;
const deleteDeviceNotification = (req, res) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        const notification = yield deviceNotification_1.DeviceNotification.findByIdAndDelete(req.params.id);
        res.status(200).json(notification);
    }
    catch (error) {
        console.log(error);
        res.status(400);
    }
});
exports.deleteDeviceNotification = deleteDeviceNotification;
//# sourceMappingURL=deviceNotificationController.js.map