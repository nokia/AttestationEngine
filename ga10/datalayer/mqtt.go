package datalayer

import(
	"fmt"

    "a10/configuration"
    
	mqtt "github.com/eclipse/paho.mqtt.golang"

)

var MESSAGING mqtt.Client

var connectHandler mqtt.OnConnectHandler = func(client mqtt.Client) {
	fmt.Println("GA10: MQTT connected")
}

var connectLostHandler mqtt.ConnectionLostHandler = func(client mqtt.Client, err error) {
	fmt.Println("GA10: MQTT connection lost. Error %v",err)
}

func initialiseMessaging() {
	fmt.Println("GA10: Initialising message infrastructure MQTT connection")

	var broker = configuration.ConfigData.Messaging.Broker
	var port = configuration.ConfigData.Messaging.Port

	opts := mqtt.NewClientOptions()
	opts.AddBroker(fmt.Sprintf("tcp://%s:%d", broker, port))
	opts.SetClientID(configuration.ConfigData.Messaging.ClientID)
	//opts.SetUsername("me")
	//opts.SetPassword("me2")
	//opts.SetDefaultPublishHandler(messagePubHandler)
	opts.OnConnect = connectHandler
	opts.OnConnectionLost = connectLostHandler

	client := mqtt.NewClient(opts)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
		fmt.Println("GA10: Failed to initialise MQTT connection")
		panic(token.Error())
	}

	fmt.Println("GA10: Message infrastructure MQTT is running")

	MESSAGING = client
}