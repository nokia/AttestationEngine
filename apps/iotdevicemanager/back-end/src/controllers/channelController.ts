import { SubscribedChannel } from "../schemas/subscribedChannel"
import { ISubscribedChannel } from "../types/subscribedChannelType"

/** Return list of all the subscribed channels from MongoDB */
export const getChannels = async (req, res) => {
  res.set('Access-Control-Allow-Origin', '*')
    try {
        const channels = await SubscribedChannel.find({})
        res.status(200).json(channels)
    } catch (error) {
        console.log(error)
        res.status(400)
    }
}

/** Create a new channel to MongoDB */
export const setChannel = async (newChannel: ISubscribedChannel, req, res) => {
    if (!newChannel) {
        res.status(400)
        throw new Error('No channel!')
    }
    try {
        const device = await SubscribedChannel.create({
            name: newChannel.name,
            devices: newChannel.devices
        })
        res.status(200).json(device)
    } catch (error) {
        console.log(error)
        res.status(400)
    }
}

/** Update a channel in MongoDB by given id */
export const updateChannel = async (updatedChannel: ISubscribedChannel, req, res) => {
    try {
        const channel = await SubscribedChannel.findByIdAndUpdate(req.params.id, updatedChannel, {
            new: true,
        })
        res.status(200).JSON(channel)
    } catch (error) {
        console.log(error)
        res.status(400)
    }
}

/** Delete a channel in MongoDB by given id */
export const deleteChannel = async (req, res) => {
    try {
        const channel = await SubscribedChannel.findByIdAndDelete(req.params.id)
        res.status(200).JSON(channel)
    } catch (error) {
        console.log(error)
        res.status(400)
    }
}
