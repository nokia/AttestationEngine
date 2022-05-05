import {INotification} from './../types/notificationType'
import {IChannel} from './../types/channelType'
import {IDevice} from '../types/deviceType'
import {useState, useEffect, useContext} from 'react'
import {
  wsLocalHostUrl,
  apiUrl,
  channelUrl,
  notificationUrl,
} from '../globals/globals'
import {doFetch} from '../utils/http'
import {replaceElement} from '../utils/utilFunctions'
import {
  NotificationContext,
  INotifications,
} from '../contexts/NotificationContext'

export const useDevices = () => {
  const [devices, setDevices] = useState<IDevice[]>([])
  const [connection, setConnection] = useState<WebSocket>()
  const {setUnreadNotification} = useContext(
    NotificationContext
  ) as INotifications

  useEffect(() => {
    const server = new WebSocket(wsLocalHostUrl)
    setConnection(server)
  }, [])

  if (connection) {
    connection.onerror = (error) => {
      console.log(`WebSocket error: ${error}`)
    }

    connection.onmessage = () => {
      setUnreadNotification(true)
      fetchDevices()
    }
  }
  useEffect(() => {
    fetchDevices()
  }, [])

  const fetchDevices = async () => {
    try {
      const response = await doFetch(apiUrl)
      setDevices(response)
    } catch (error) {
      console.log('error occurred fetching', error)
    }
  }

  const fetchDevice = async (id: string) => {
    try {
      const response = await doFetch(`${apiUrl}/${id}`)
      return await response
    } catch (error) {
      console.log('error occurred fetching', error)
    }
  }

  const fetchDeviceData = async (id: string) => {
    try {
      const entity = 'sensordata'
      const response = await doFetch(`${apiUrl}/${entity}/${id}`)
      return await response
    } catch (error) {
      console.log('error occurred fetching', error)
    }
  }

  const attestDevice = async (id: string) => {
    try {
      const response: IDevice = await doFetch(`${apiUrl}/attest/${id}`)
      const newDevices = replaceElement(response, devices)
      setDevices(newDevices)
      return await response
    } catch (error) {
      console.log('error occurred fetching', error)
    }
  }
  return {devices, fetchDevice, fetchDeviceData, attestDevice}
}

export const useChannels = () => {
  const [channels, setChannels] = useState<string[]>([])

  useEffect(() => {
    fetchChannels()
  }, [])

  const fetchChannels = async () => {
    try {
      const response = await doFetch(channelUrl)
      mapChannelNames(response)
    } catch (error) {
      console.log('error fetching', error)
    }
  }

  const mapChannelNames = (data: IChannel[]) => {
    const channelNames = data.map((channel) => channel.name)
    setChannels(channelNames)
  }
  return {channels}
}

export const useNotifications = () => {
  const [notifications, setNotifications] = useState<INotification[]>([])
  const {setUnreadNotification} = useContext(
    NotificationContext
  ) as INotifications

  useEffect(() => {
    fetchNotifications()
  }, [])

  useEffect(() => {
    setUnreadNotification(false)
  }, [notifications])

  const fetchNotifications = async () => {
    try {
      const response = await doFetch(notificationUrl)
      setNotifications(response)
    } catch (error) {
      console.log('error fetching', error)
    }
  }

  const deleteNotifications = async () => {
    try {
      const response = await doFetch(notificationUrl, {method: 'DELETE'})
      if (response.acknowledged) {
        fetchNotifications()
      }
    } catch (error) {
      console.log('error fetching', error)
    }
  }

  const deleteSingleNotification = async (id: string) => {
    try {
      const response = await doFetch(`${notificationUrl}${id}`, {
        method: 'DELETE',
      })
      if (response) {
        fetchNotifications()
      }
    } catch (error) {
      console.log('error fetching', error)
    }
  }

  return {notifications, deleteNotifications, deleteSingleNotification}
}
