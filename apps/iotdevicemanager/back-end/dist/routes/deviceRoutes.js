"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.router = void 0;
const express_1 = __importDefault(require("express"));
const deviceController_1 = require("../controllers/deviceController");
exports.router = express_1.default.Router();
exports.router.route('/').get(deviceController_1.getDevices).post(deviceController_1.setDevice);
exports.router.route('/:id').delete(deviceController_1.deleteDevice).put(deviceController_1.updateDevice).get(deviceController_1.getDevice);
exports.router.route('/sensordata/:id').get(deviceController_1.getDeviceSensorData);
//# sourceMappingURL=deviceRoutes.js.map