const mqtt = require("mqtt");
WebSocket = require("ws");
const path = require("path");
const express = require("express");
const bodyParser = require("body-parser");
const app = express();
const fs = require("fs");
const jsStringify = require("js-stringify");

const datae = fs.readFileSync(
  path.resolve(__dirname, "server_config.json"),
  "utf8"
);

let obj = JSON.parse(datae);
console.log(obj);

const restPort = obj.RESTport;
app.set("view engine", "pug");
app.set("views", "views");
app.use(bodyParser.urlencoded({ extended: false }));
app.use("/static", express.static("public"));
const wshost = obj.WEBSOCKEThost;
const wsport = obj.WEBSOCKETport;

app.get("/", (req, res) => {
  res.render("index", {
    jsStringify,
    wshost: wshost,
    wsport: wsport,
    pageTitle: "System log info",
  });
});

app.listen(restPort, () => {
  console.log(`Rest api listening on port ${restPort}`);
});

const options = {
  // clean: true, // retain session
  // Authentication information
  clientId: "test",
  // keepalive: 60,
  protocolId: "MQTT",
  // protocolVersion: 4,
  // reconnectPeriod: 1000,
  connectTimeout: 1000,
};

// Connect string, and specify the connection method by the protocol
// ws Unencrypted WebSocket connection
// wss Encrypted WebSocket connection
// mqtt Unencrypted TCP connection
// mqtts Encrypted TCP connection
// wxs WeChat applet connection
// alis Alipay applet connection
//const connectUrl = 'mqtt://test.mosquitto.org'

const connectUrl = "mqtt://" + obj.MQTThost + ":" + obj.MQTTport;

const client = mqtt.connect(connectUrl);

const WEBSOCKETport = obj.WEBSOCKETport;

const wsServer = new WebSocket.Server({
  host: obj.WEBSOCKEThost,
  port: WEBSOCKETport,
});
console.log("ws ip address", wsServer.options.host);
wsServer.on("connection", function (socket) {
  console.log("A client just connected");

  socket.on("message", function (msg) {
    console.log("Received message from client: " + msg);

    wsServer.clients.forEach(function (item) {
      item.send(`{"topic": "connection", "message": "${msg}"}`);
    });
  });
});

console.log(
  new Date() +
    " Server is listening for websocket clients on port " +
    WEBSOCKETport
);

client.on("connect", function () {
  client.subscribe("management", function (err) {
    if (!err) {
      console.log("Successfully subscribed to management channel.");
    } else {
      console.log("Failed to subscribe to the management channel.", err);
    }
  });
});

client.on("reconnect", (error) => {
  console.log("MQTT reconnecting:", error);
});

client.on("error", (error) => {
  console.log("MQTT Connection failed:", error);
});

let devices = [];

client.on("message", function (topic, message) {
  // message is Buffer
  try {
    console.log(message.toString(), topic);
  } catch (error) {
    console.log("Message in wrong format at topic: ", topic);
  }
  let m = JSON.parse(message);
  let s;
  if (!devices.find((item) => item.device.hostname === m.device.hostname)) {
    try {
      let x = {
        event: m.event,
        message: m.message,
        messagetimestamp: m.messagetimestamp,
        device: {
          itemid: m.device.itemid,
          hostname: m.device.hostname,
          address: m.device.address,
          starttimestamp: m.device.starttimestamp,
          valid: m.device.valid,
          validtimestamp: m.device.validtimestamp,
        },
        sensor: {
          name: m.sensor.name,
          starttimestamp: m.sensor.starttimestamp,
          valid: m.sensor.valid,
          validtimestamp: m.sensor.validtimestamp,
        },
      };

      devices.push(x);
      s = devices[devices.length];
    } catch {
      console.log("Something went wrong with putting data into array");
    }
  }
  let k = devices.find((item) => item.device.hostname === m.device.hostname);
  if (devices.find((item) => item.device.hostname === m.device.hostname)) {
    //s = devices[devices.length]
    s = devices.indexOf(k);
    devices[s].device.valid = m.device.valid;
    if (m.event === "device validation ok") {
      console.log("Device validation ok");
      devices[s].device.valid = m.device.valid;
      devices[s].device.validtimestamp = m.messagetimestamp;
    }
  }

  wsServer.clients.forEach(function (item) {
    console.log(topic, "message:", m);

    let iitemmm = `{"god": {"data": { "hostname": "${m.device.hostname}", "timestamp": "${m.messagetimestamp}", "sensor": "${m.sensor.name}", "message": "${m.message}", "deviceObject": "${devices[s].device.hostname}", "valid": "${devices[s].device.valid}"}, "valdate": "${devices[s].device.validtimestamp}"}}`;

    console.log(iitemmm);

    item.send(iitemmm);
  });
});
