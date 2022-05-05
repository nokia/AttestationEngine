"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.router = void 0;
const deviceNotificationController_1 = require("./../controllers/deviceNotificationController");
const express_1 = __importDefault(require("express"));
exports.router = express_1.default.Router();
exports.router
    .route('/')
    .get(deviceNotificationController_1.getDeviceNotifications)
    .post(deviceNotificationController_1.setDeviceNotification)
    .delete(deviceNotificationController_1.deleteAllNotifications);
exports.router.route('/:id').delete(deviceNotificationController_1.deleteDeviceNotification);
//# sourceMappingURL=deviceNotificationRoutes.js.map