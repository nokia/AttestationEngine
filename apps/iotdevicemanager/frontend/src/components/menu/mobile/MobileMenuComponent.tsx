import React from 'react'
import './MobileMenuComponent.css'
import NotificationCenterComponent from '../../NotificationCenterComponent/NotificationCenterComponent'

const MobileMenuComponent: React.FC = () => {
  return (
    <div>
      <div className="mobile-menu-container">
        <NotificationCenterComponent />
      </div>
    </div>
  )
}

export default MobileMenuComponent
