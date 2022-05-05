import {DeviceNotification} from '../schemas/deviceNotification'
import {Device} from '../schemas/device'
import {SubscribedChannel} from '../schemas/subscribedChannel'
import {ifAnnouncements} from '../types/announcementType'
import WebSocket from 'ws'

const updateMongoDevice = (message: ifAnnouncements, attestStatus: number,
  wss) => {
  try {
    DeviceNotification.create({
      deviceId: message._id,
      deviceName: message.deviceName,
      deviceChannels: message.channels,
      timestamp: message.timestamp,
      status: attestStatus,
    })
    Device.findOneAndUpdate(
      {_id: message._id},
      {
        trustedState: attestStatus,
        $push: {
          history: {
            name: message.deviceName,
            timestamp: message.timestamp,
            trustedState: attestStatus,
          },
        },
      },
      (err, docs) => {
        wss.clients.forEach((client) => {
          if (client.readyState === WebSocket.OPEN) {
            console.log('SENT MESSAGE')
            client.send(JSON.stringify(docs))
          }
        })
      }
    )
  } catch (error) {
    console.log('mongodb error', error)
  }
}

const createNewMongoDevice = (
  message: ifAnnouncements,
  attestStatus: number,
  wss
) => {
  try {
    Device.create(
      {
        name: message.deviceName,
        _id: message._id,
        trustedState: attestStatus,
        channels: message.channels,
        history: [
          {
            name: message.deviceName,
            timestamp: message.timestamp,
            trustedState: attestStatus,
          },
        ],
      },
      (err, docs) => {
        wss.clients.forEach((client) => {
          if (client.readyState === WebSocket.OPEN) {
            console.log('SENT MESSAGE')
            client.send(JSON.stringify(docs))
          }
        })
      }
    )
    DeviceNotification.create({
      deviceId: message._id,
      deviceName: message.deviceName,
      deviceChannels: message.channels,
      timestamp: message.timestamp,
      status: attestStatus,
    })
  } catch (error) {
    console.log('mongo error', error)
  }
}

const updateSubscribedChannels = (
  message: ifAnnouncements,
  channel: string
) => {
  try {
    SubscribedChannel.findOneAndUpdate(
      {name: channel},
      {$push: {devices: message._id}},
      (err, docs) => {
        if (!docs) {
          SubscribedChannel.create({
            name: channel,
            devices: message._id,
          })
        }
      }
    )
  } catch (error) {
    console.log('mongo error', error)
  }
}

const updateAttestation = async (attestStatus: number, id: string):Promise<any> => {
  try {
    try {
      Device.findOneAndUpdate(
        {_id: id},
        {
          trustedState: attestStatus,
          $push: {
            history: {
              timestamp: Date.now()/1000,
              trustedState: attestStatus,
            },
          },
        },
        (err, docs) => {
          return docs.history
        }
      )
    } catch (error) {
      console.log('mongodb error', error)
    }
  } catch (error) {

  }
}

export {updateMongoDevice, createNewMongoDevice, updateSubscribedChannels, updateAttestation}
