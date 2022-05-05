/* eslint-disable react/prop-types */
import React, {useState} from 'react'

export type INotifications = {
  unreadNotification: boolean
  setUnreadNotification: (active: boolean) => void
}
// eslint-disable-next-line prettier/prettier
const NotificationContext = React.createContext<INotifications | null>(null)

const NotificationProvider: React.FC<React.ReactNode> = (props: any) => {
  // eslint-disable-next-line operator-linebreak
  const [unreadNotification, setUnreadNotification] = useState(false)

  return (
    <NotificationContext.Provider
      value={{unreadNotification, setUnreadNotification}}
    >
      {props.children}
    </NotificationContext.Provider>
  )
}

export {NotificationContext, NotificationProvider}
