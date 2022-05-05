let hostname = window.location.hostname
if (hostname.charAt(hostname.length - 1) == '/') {
  hostname = hostname.substring(0, hostname.length - 1)
}
const wsLocalHostUrl = `ws://${hostname}:8080`
const apiUrl = `/api/devices`
const channelUrl = `/api/channels`
const notificationUrl = `/api/notifications/`

export {wsLocalHostUrl, apiUrl, channelUrl, notificationUrl}
