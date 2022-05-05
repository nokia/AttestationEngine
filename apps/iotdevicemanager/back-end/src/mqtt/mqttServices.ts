import {Device} from '../schemas/device'
import {ISensorData} from '../types/sensorDataType'
import {ifAnnouncements} from '../types/announcementType'
import {SensorData} from '../schemas/sensorData'
import startAttestation from '../a10/a10services'
import { createNewMongoDevice, updateMongoDevice, updateSubscribedChannels } from '../mongo/mongoServices';

const announcementService = async (
  message: ifAnnouncements,
  topics: string[],
  client: any,
  wss: any
) => {
  try {
    const attestStatus = await startAttestation(message._id)
    Device.find({_id: message._id}, (err, docs) => {
      if (message.disconnect) {
        updateMongoDevice(message, 2, wss)
        return
      }
      if (docs.length > 0) {
        updateMongoDevice(message, attestStatus, wss)
      }
      if (docs.length == 0) {
        createNewMongoDevice(message, attestStatus, wss)
      }
    })

    message.channels.forEach((channel) => {
      if (!topics.includes(channel)) {
        topics.push(channel)
        client.subscribe([channel], () => {
          console.log(`Subscribe to topic ${channel}`)
        })
        updateSubscribedChannels(message, channel)
      }
    })
  } catch (error) {
    console.log('error occurred trying to handle announcements ', error)
  }
}

const sensorService = (message: ISensorData) => {
  SensorData.create({
    deviceId: message._id,
    sensorValue: message.sensorValue,
    timestamp: message.timestamp,
    sensorType: message.sensorType,
  })
}

export {announcementService, sensorService}
