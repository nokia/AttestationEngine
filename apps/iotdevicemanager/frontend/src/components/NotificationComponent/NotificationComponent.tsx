/* eslint-disable indent */
import {Icon} from '@iconify/react'
import React from 'react'
import {INotification} from '../../types/notificationType'
import './NotificationComponent.css'

interface NotificationProps {
  notification: INotification
  deleteNotification: (id: string) => Promise<void>
}

const NotificationComponent: React.FC<NotificationProps> = ({
  notification,
  deleteNotification,
}) => {
  const epoch = parseFloat(notification.timestamp)
  const date = new Date(epoch * 1000).toLocaleDateString()
  const time = new Date(epoch * 1000).toLocaleTimeString()

  return (
    <div className="notification-component">
      <div
        className="status-dot"
        style={
          // eslint-disable-next-line prettier/prettier
          notification.status == 0 ? {background: '#49C364'} : notification.status == 2 ? {background: '#FA4C5C'} : {background: '#FFCD05'}
        }
      ></div>
      <div className="notification-text-container">
        <div className="notification-component-header bold">
          {notification.status == 0
            ? `${notification.deviceName} is trusted`
            : notification.status == 2
            ? `${notification.deviceName} disconnected`
            : `${notification.deviceName} connected`}
        </div>
        <div className="notification-component-name description">
          Name: {notification.deviceName}
        </div>
        <div className="notification-component-channels description">
          Subscribed to:{' '}
          {notification.deviceChannels.map((channel) => `"${channel}" `)}
        </div>
        <div className="notification-component-timestamp description">
          {`${date} - ${time}`}
        </div>
      </div>
      <Icon
        icon="bx:x-circle"
        color="white"
        width="18"
        inline={true}
        className="delete-single-notification"
        onClick={() => deleteNotification(notification._id)}
      />
    </div>
  )
}

export default NotificationComponent
