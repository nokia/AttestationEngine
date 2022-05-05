import React, {useEffect, useState} from 'react'
import MenuComponent from '../../components/menu/MenuComponent'
import MobileMenuComponent from '../../components/menu/mobile/MobileMenuComponent'
import MobileNavigationComponent from '../../components/navigation/mobile/MobileNavigationComponent'
import NavigationComponent from '../../components/navigation/NavigationComponent'
import StatusComponent from '../../components/StatusComponent/StatusComponent'
import './BaseView.css'
import {useDevices} from '../../hooks/ApiHooks'
import NotificationCenterComponent from '../../components/NotificationCenterComponent/NotificationCenterComponent'
import {Route, Routes} from 'react-router-dom'
import DeviceView from '../devices/DevicesView'
import DetailsView from '../details/DetailsView'

const BaseView: React.FC = () => {
  const [isMobile, setIsMobile] = useState(() => {
    return window.innerWidth > 1000 ? false : true
  })
  const {devices} = useDevices()
  const [open, setOpen] = useState(false)
  const [openNotificationCenter, setOpenNotificationCenter] = useState(false)

  const handleResize = () => {
    window.innerWidth > 1000 ? setIsMobile(false) : setIsMobile(true)
  }

  useEffect(() => {
    window.addEventListener('resize', handleResize)
  }, [])

  const toggleMenu = () => {
    setOpen(!open)
  }

  const toggleNotificationCenter = () => {
    setOpenNotificationCenter(!openNotificationCenter)
  }

  return (
    <div className="base-view-container">
      <div>
        {isMobile ? (
          <>
            <MobileNavigationComponent toggleMenu={toggleMenu} />
            {open && <MobileMenuComponent />}
            <div className="mobile-status-container">
              <StatusComponent devices={devices} />
            </div>
          </>
        ) : (
          <>
            <NavigationComponent
              toggleNotificationCenter={toggleNotificationCenter}
            />
            <MenuComponent devices={devices} />
          </>
        )}
        {openNotificationCenter && <NotificationCenterComponent />}
      </div>
      <div className="base-view-content-container">
        <Routes>
          <Route path="/" element={<DeviceView />}></Route>
          <Route path=":deviceId" element={<DetailsView />} />
        </Routes>
      </div>
    </div>
  )
}

export default BaseView
