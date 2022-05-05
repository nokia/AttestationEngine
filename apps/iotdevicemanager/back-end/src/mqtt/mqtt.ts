import {ifAnnouncements} from '../types/announcementType'
import mqtt from 'mqtt'
import {connectUrl, clientId, originalTopic} from '../utils/GlobalVariables'
import {SubscribedChannel} from '../schemas/subscribedChannel'
import {ISensorData} from '../types/sensorDataType'
import { announcementService, sensorService } from './mqttServices'
import WebSocketClient from '../websocket/websocket'

const mqttClient = () => {
  const {wss} = WebSocketClient()
  const topics = [originalTopic]
  const dbTopics = async () => {
    try{
      const fetchedTopics = await SubscribedChannel.find({})
      fetchedTopics.forEach((topic) => {
        topics.push(topic.name)
      })
    }catch (error) {
      console.log('error occurred fetching topics', error)
    }
  }
  const client = mqtt.connect(connectUrl, {
    clientId,
    clean: true,
    connectTimeout: 4000,
    username: 'emqx',
    password: 'public',
    reconnectPeriod: 1000,
  })

  try {
    client.on('connect', () => {
      console.log('connected')
      dbTopics().then(() => {
        topics.forEach((topic) => {
          client.subscribe(topic, () => {
            console.log(`Subscribed to topic ${topic}`)
          })
        })
      })
    })
  } catch (error) {
    console.log('error occurred trying to connect to mqtt', error)
  }


  client.on('message', (topic, payload) => {
    if (topic == originalTopic) {
      try {
        const message: ifAnnouncements = JSON.parse(payload.toString())
        announcementService(message, topics, client, wss)
        console.log(
          `Received message from topic: ${topic} reading out: ${payload.toString()}`
        )
      } catch (error) {
        console.error('error occurred trying to read announcements', error)
      }
    } else {
      try {
        const message: ISensorData = JSON.parse(payload.toString())
        sensorService(message)
        console.log(
          `Received message from topic: ${topic} reading out: ${payload.toString()}`
        )
      } catch (error) {
        console.error('error occurred trying to read device info', error)
      }
    }
  })
}

export default mqttClient
