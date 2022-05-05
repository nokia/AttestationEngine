import { IDeviceNotification } from '../types/deviceNotificationType';
import { DeviceNotification } from '../schemas/deviceNotification';

/** Return list of all the notifications from MongoDB */
export const getDeviceNotifications = async (req, res) => {
    try {
        const notifications = await DeviceNotification.find({}).sort({timestamp: -1})
        res.status(200).json(notifications)
    } catch (error) {
        console.log(error)
        res.status(400)
    }
}

/** Create a new notification to MongoDB */
export const setDeviceNotification = async (newNotification: IDeviceNotification, req, res) => {
  if (!newNotification) {
    res.status(400)
    throw new Error('No notifications')
  }
  try {
    const notification = await DeviceNotification.create({
      deviceId: newNotification.deviceId,
      deviceName: newNotification.deviceName,
      deviceChannels: newNotification.deviceChannels,
      timestamp: newNotification.timestamp,
    })
    res.status(200).json(notification)
  } catch (error) {
    console.log(error)
    res.status(400)
  }
}

/** Delete a notification in MongoDB by given id */
export const deleteAllNotifications = async (req, res) => {
    try {
        const notification = await DeviceNotification.deleteMany({})
        res.status(200).json(notification)
    } catch (error) {
        console.log(error)
        res.status(400)
    }
}

export const deleteDeviceNotification = async (req, res) => {
    try {
        const notification = await DeviceNotification.findByIdAndDelete(req.params.id)
        res.status(200).json(notification)
    } catch (error) {
        console.log(error)
        res.status(400)
    }
}
