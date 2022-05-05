import React, {useContext} from 'react'
import NokiaBellLabsLogo from '../../../assets/NokiaBellLabsLogo'
import './MobileNavigationComponent.css'
import {Icon} from '@iconify/react'
import {
  INotifications,
  NotificationContext,
} from '../../../contexts/NotificationContext'

type ChildProps = {
  toggleMenu?: () => void
}

const MobileNavigationComponent: React.FC<ChildProps> = ({toggleMenu}) => {
  const {unreadNotification} = useContext(NotificationContext) as INotifications
  return (
    <div className="nav-bar">
      <NokiaBellLabsLogo />
      <div className="icon-container" onClick={toggleMenu}>
        <Icon
          className="icon"
          icon="ic:outline-notifications"
          color="white"
          width="22"
        />
        {unreadNotification ? <div className="red-dot"></div> : <></>}
      </div>
    </div>
  )
}

export default MobileNavigationComponent
