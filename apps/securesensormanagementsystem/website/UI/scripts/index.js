let host = "127.0.0.1";
let port = 5000;

let websocket = new WebSocket("ws://127.0.0.1:5000", "protocol");

websocket.onopen = (event) => {
  websocket.send("onopen is successful");
};

websocket.onmessage = (event) => {
  console.log(event.data.includes("god"));
  if (event.data.includes("god")) {
    addElement(event.data);
  } else {
    console.log("got message", event.data);
  }
};

/**
 * Creates a div with paragraphs to main_content div with data from the server.
 * @param {{topic: string; message: string}} data
 */

let history = [];
let devices = [];
let sensors = [];
let count = 0;
const addElement = async (data) => {
  console.log(data);
  let o = JSON.parse(data);
  //let test = o.god.data
  // let i = JSON.parse(o)
  let { hostname, sensor, message, timestamp, deviceObject, valid } =
    o.god.data;
  let { valdate } = o.god;
  const newDiv = document.createElement("tr");
  const newTimestamp = document.createElement("td");
  const newHost = document.createElement("td");
  const newSensor = document.createElement("td");
  const newMessage = document.createElement("td");
  console.log(deviceObject);

  if (!devices.find((item) => item.hostname === deviceObject)) {
    let x = {
      hostname: deviceObject,
      valid: valid,

      valdate: "",
      sensor: "",
    };

    let y = {
      hostname: deviceObject,
      sensor: sensor,
    };
    sensors.push(y);
    //if (m.event==="validation ok"){
    //  x.valdate=m.timestamp
    //}
    devices.push(x);
    s = devices[devices.length];
  }
  let k = devices.find((item) => item.hostname === deviceObject);
  if (devices.find((item) => item.hostname === deviceObject)) {
    console.log("komlmas iffi" + valdate);
    let y = {
      hostname: deviceObject,
      sensor: sensor,
    };
    s = devices.indexOf(k);
    devices[s].valdate = valdate;
    devices[s].valid = valid;
    if (sensors.find((item) => item.hostname === deviceObject)) {
      if (!sensors.find((item) => item.sensor === sensor)) {
        sensors.push(y);
      }
    }
    console.log("valdate: " + devices[s].valdate);
  }

  newTimestamp.appendChild(document.createTextNode(`${timestamp}`));
  newHost.appendChild(document.createTextNode(`${hostname}`));
  newSensor.appendChild(document.createTextNode(`${sensor}`));
  newMessage.appendChild(document.createTextNode(`${message}`));

  newDiv.appendChild(newHost);
  newDiv.appendChild(newSensor);
  newDiv.appendChild(newMessage);
  newDiv.appendChild(newTimestamp);

  newDiv.classList.add("active-box", "new-box");
  count++;
  if (count % 2 === 0) newDiv.classList.add("new-new-box");
  document.getElementById("main_content").prepend(newDiv);

  document.getElementById("main_content-devices").innerHTML = "";
  const tes = JSON.stringify(newDiv);
  console.log("copytest = ", tes, " div ", newDiv);
  history.push({
    timestamp,
    hostname,
    sensor,
    message,
  });

  for (let i = 0; i < devices.length; i++) {
    if (devices[i].hostname != "") {
      let elem = document.createElement("div");
      let elem2 = document.createElement("div");
      console.log(devices[s].valid);
      if (devices[i].valid === "true") {
        elem2.classList.add("valid");
      } else {
        elem2.classList.add("notvalid");
      }
      let elem3 = document.createElement("div");
      let elem4 = document.createElement("div");
      let node = document.createTextNode("Name: " + devices[i].hostname);
      let elleem = document.createElement("div");
      let noode;
      let count = 0;
      for (let d = 0; d < sensors.length; d++) {
        if (sensors[d].hostname === devices[i].hostname) {
          if (sensors[d].sensor != "") {
            count++;
            if (count === 1) {
              noode = document.createTextNode(
                "Sensor(s) running: " + sensors[d].sensor
              );
              elleem.appendChild(noode);
            } else {
              let noode = document.createTextNode(" & " + sensors[d].sensor);
              elleem.appendChild(noode);
            }
          }
        }
      }
      let node2 = document.createTextNode("Valid: " + devices[i].valid);
      let node3 = document.createTextNode(
        "Last validated on: " + devices[i].valdate
      );
      let node4 = document.createTextNode("----- ");
      elem.appendChild(node);
      elem2.appendChild(node2);
      elem3.appendChild(node3);
      elem4.appendChild(node4);
      document.getElementById("main_content-devices").appendChild(elem);
      document.getElementById("main_content-devices").appendChild(elem2);
      document.getElementById("main_content-devices").appendChild(elem3);
      document.getElementById("main_content-devices").appendChild(elleem);
      document.getElementById("main_content-devices").appendChild(elem4);
    }
  }
  const mainContent = document.getElementById("main_content");

  console.log(mainContent.lastChild.innerHTML);
  if (mainContent.childElementCount > 50) {
    while (mainContent.childElementCount > 30) {
      mainContent.removeChild(mainContent.lastChild);
    }
  }
};

const hideButton = document.getElementById("hide-button");
const logViewerHideButton = document.getElementById("log-viewer-hide-button");

const logViewerContainer = document.getElementById("log-viewer-container");
const hideDiv = document.getElementById("hide-div");
const hideLogViewer = async () => {
  if (!logViewerContainer.classList.contains("hidden")) {
    logViewerContainer.classList.add("hidden");
    hideDiv.classList.add("hidden");
    console.log("test", logElement);
    while (logElement.childElementCount) {
      console.log("logElement");
      logElement.removeChild(logElement.lastChild);
    }
    search.value = "";
  } else {
    logViewerContainer.classList.remove("hidden");
    hideDiv.classList.remove("hidden");
    logViewer();
  }
};
hideDiv.addEventListener("click", hideLogViewer);

const logElement = document.getElementById("log-viewer");
const logViewer = async () => {
  console.log(history);
  const ultdiv = document.createElement("div");

  history.forEach((item) => {
    const cont = document.createElement("p");
    cont.innerText = `Hostname: ${item.hostname}, Sensor: ${item.sensor}, Message: ${item.message}, Timestamp: ${item.timestamp}`;
    ultdiv.appendChild(cont);
    logElement.prepend(ultdiv);
  });
};

const hideViewButton = async () => {
  console.log(hideButton.innerText);

  hideLogViewer();
};

hideButton.addEventListener("click", hideViewButton);
logViewerHideButton.addEventListener("click", hideViewButton);

const filterfunc = (checkValue, searchValue) => {
  const { value } = checkValue;
  switch (value.toLowerCase()) {
    case "sensor":
      console.log("sensor");
      return history.filter((node) => {
        let lowercasedNodeSensor = node.sensor.toLowerCase();
        return lowercasedNodeSensor.includes(searchValue.toLowerCase());
      });
    case "hostname":
      console.log("hostname");
      return history.filter((node) => {
        let lowercasedNodeHostname = node.hostname.toLowerCase();
        return lowercasedNodeHostname.includes(searchValue.toLowerCase());
      });
    case "message":
      console.log("message");
      return history.filter((node) => {
        let lowercasedNodeMessage = node.message.toLowerCase();
        return lowercasedNodeMessage.includes(searchValue.toLowerCase());
      });
    case "timestamp":
      console.log("timestamp");
      return history.filter((node) => {
        let lowercasedNodeTimestamp = node.timestamp.toLowerCase();
        return lowercasedNodeTimestamp.includes(searchValue.toLowerCase());
      });
    default:
      console.log("default case");
      return history;
  }
};

const search = document.getElementById("log-viewer-search");

search.addEventListener("input", async (event) => {
  let test = [];
  let radio = document.getElementsByName("filter");
  let foundChecked;
  radio.forEach((item) => {
    if (item.checked) {
      foundChecked = item;
    }
  });
  try {
    test = filterfunc(foundChecked, search.value);
    console.log("test = ", test);
  } catch (error) {
    test = history;
    console.log("incorrect format");
  }

  const ultdiv = document.createElement("div");
  while (logElement.childElementCount) {
    console.log("logElement");
    logElement.removeChild(logElement.lastChild);
  }
  test.forEach((item) => {
    const cont = document.createElement("p");
    cont.innerText = `Hostname: ${item.hostname}, Sensor: ${item.sensor}, Message: ${item.message}, Timestamp: ${item.timestamp}`;
    ultdiv.appendChild(cont);
    logElement.prepend(ultdiv);
  });
  console.log(" ", test);
});

const logViewerSortButton = document.getElementById("log-viewer-sort-button");
let asc = true;
// logViewerSortButton.addEventListener("click", () => {
//   console.log("asc ", asc);
//   let sorted = [];
//   if (asc) {
//     sorted = history.sort((a, b) => {
//       let reversedA = a.timestamp.split(",")[0].split(".").reverse();
//       let reversedB = b.timestamp.split(",")[0].split(".").reverse();
//       let astr =
//         reversedA.toString().split(",").join("-") +
//         "T" +
//         a.timestamp.split(",")[1].replace(" ", "");
//       // a.timestamp = a.timestamp.split(".").join("-").replace(", ", "T");
//       // b.timestamp = b.timestamp.split(".").join("-").replace(", ", "T");
//       console.log(astr);
//       console.log(
//         "timestamp",
//         Date.parse(a.timestamp),
//         " b = ",
//         Date.parse(b.timestamp)
//       );
//       console.log("a-b = ", a.timestamp - b.timestamp);
//       if (a.timestamp) return a.timestamp - b.timestamp;
//     });
//     asc = false;
//   } else {
//     sorted = history.sort((a, b) => {
//       if (a.timestamp) return b.timestamp - a.timestamp;
//     });
//     asc = true;
//   }

//   const ultdiv = document.createElement("div");
//   while (logElement.childElementCount) {
//     console.log("logElement");
//     logElement.removeChild(logElement.lastChild);
//   }
//   sorted.forEach((item) => {
//     const cont = document.createElement("p");
//     cont.innerText = `Hostname: ${item.hostname}, Sensor: ${item.sensor}, Message: ${item.message}, Timestamp: ${item.timestamp}`;
//     ultdiv.appendChild(document.createElement("p").appendChild(cont));
//     logElement.prepend(ultdiv);
//   });
// });
