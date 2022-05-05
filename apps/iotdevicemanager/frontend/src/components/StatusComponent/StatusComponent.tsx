import React from 'react'
import {Chart as ChartJS, ArcElement, Tooltip, Legend} from 'chart.js'
import {Doughnut} from 'react-chartjs-2'
import './StatusComponent.css'
import {IDevice} from '../../types/deviceType'
import StatusRedIcon from '../../assets/icons/status_red.svg'
import StatusGreenIcon from '../../assets/icons/status_green.svg'
import StatusYellowIcon from '../../assets/icons/status_yellow.svg'

ChartJS.register(ArcElement, Tooltip, Legend)

interface StatusComponentProps {
  devices: IDevice[]
}

const StatusComponent: React.FC<StatusComponentProps> = ({devices}) => {
  const calculateDeviceStatuses = (status: number) =>
    devices.filter((device) => device.trustedState === status).length

  const calculateUntrustedDevices = () =>
    devices.filter(
      (device) => device.trustedState != 0 && device.trustedState != 2
    ).length

  const statusData = {
    totalDeviceAmount: devices.length,
    trustedDeviceAmount: calculateDeviceStatuses(0),
    offlineDeviceAmount: calculateDeviceStatuses(2),
    untrustedDeviceAmount: calculateUntrustedDevices(),
  }

  const statusComponentData = {
    datasets: [
      {
        data: [
          statusData.trustedDeviceAmount,
          statusData.offlineDeviceAmount,
          statusData.untrustedDeviceAmount,
        ],
        backgroundColor: ['#48C864', '#FA4C5C', '#FFCD05'],
        borderWidth: 0,
        cutout: '70%',
      },
    ],
  }
  return (
    <div className="status-container">
      <div className="status-component">
        <Doughnut data={statusComponentData} />
        <div className="status-description-container">
          <div className="status-description">
            <p>
              {statusData.trustedDeviceAmount}/{statusData.totalDeviceAmount}
            </p>
            <p>Trusted</p>
          </div>
        </div>
      </div>
      <div className="status-calculator-container">
        <div className="status-calculator">
          <div className="status-calculator-upper">
            <img src={StatusGreenIcon} />
            <p className="status-data">{statusData.trustedDeviceAmount}</p>
          </div>
          <p>trusted</p>
        </div>
        <div className="status-calculator">
          <div className="status-calculator-upper">
            <img src={StatusYellowIcon} />
            <p className="status-data">{statusData.untrustedDeviceAmount}</p>
          </div>
          <p>untrusted</p>
        </div>
        <div className="status-calculator">
          <div className="status-calculator-upper">
            <img src={StatusRedIcon} />
            <p className="status-data">{statusData.offlineDeviceAmount}</p>
          </div>
          <p>offline</p>
        </div>
      </div>
    </div>
  )
}

export default StatusComponent
