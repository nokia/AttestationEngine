import React from 'react'
import {useNotifications} from '../../hooks/ApiHooks'
import NotificationComponent from '../NotificationComponent/NotificationComponent'
import './NotificationCenterComponent.css'

const NotificationCenterComponent: React.FC = () => {
  // eslint-disable-next-line operator-linebreak
  const {notifications, deleteNotifications, deleteSingleNotification} =
    useNotifications()

  const mappedNotifications = notifications?.map((notification) => {
    return (
      <NotificationComponent
        notification={notification}
        deleteNotification={deleteSingleNotification}
        key={notification._id}
      />
    )
  })

  return (
    <div className="notification-center">
      <div className="notification-header">Notifications</div>
      {mappedNotifications?.length ? (
        <div className="device-notifications">{mappedNotifications}</div>
      ) : (
        <div className="no-notifications">No new notifications</div>
      )}
      <div
        className={
          mappedNotifications && mappedNotifications.length > 3
            ? 'notification-footer sticky'
            : 'notification-footer absolute'
        }
      >
        <button className="clear-notifications" onClick={deleteNotifications}>
          Clear all
        </button>
      </div>
    </div>
  )
}

export default NotificationCenterComponent
