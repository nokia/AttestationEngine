"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
require("dotenv/config");
const cors_1 = __importDefault(require("cors"));
const app = (0, express_1.default)();
const port = 3001;
const db_1 = __importDefault(require("./config/db"));
const deviceRoutes_1 = require("./routes/deviceRoutes");
const channelRoutes_1 = require("./routes/channelRoutes");
const deviceNotificationRoutes_1 = require("./routes/deviceNotificationRoutes");
const mqtt_1 = __importDefault(require("./mqtt/mqtt"));
(0, db_1.default)();
console.log('The WebSocket server is running on port 8080');
(0, mqtt_1.default)();
app.use((0, cors_1.default)());
app.use('/api/devices', deviceRoutes_1.router);
app.use('/api/channels', channelRoutes_1.router);
app.use('/api/notifications', deviceNotificationRoutes_1.router);
app.get('/', (req, res) => {
    res.send('Hello World!');
});
app.listen(port, () => {
    return console.log(`Express is listening at http://localhost:${port}`);
});
//# sourceMappingURL=app.js.map