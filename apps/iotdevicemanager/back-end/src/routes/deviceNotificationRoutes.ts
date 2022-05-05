import {
  deleteAllNotifications,
  deleteDeviceNotification,
  getDeviceNotifications,
  setDeviceNotification,
} from '../controllers/deviceNotificationController'
import express from 'express'

export const router = express.Router()

router
  .route('/')
  .get(getDeviceNotifications)
  .post(setDeviceNotification)
  .delete(deleteAllNotifications)

router.route('/:id').delete(deleteDeviceNotification)
