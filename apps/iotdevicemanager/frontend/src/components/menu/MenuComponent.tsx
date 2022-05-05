import React from 'react'
import {IDevice} from '../../types/deviceType'
import StatusComponent from '../StatusComponent/StatusComponent'
import './MenuComponent.css'

interface MenuComponentProps {
  devices: IDevice[]
}

const MenuComponent: React.FC<MenuComponentProps> = ({devices}) => {
  return (
    <div className="side-menu-container">
      <div className="menu-container">
        <StatusComponent devices={devices} />
      </div>
      <div className="curve"></div>
    </div>
  )
}

export default MenuComponent
