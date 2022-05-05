import {Schema, model} from "mongoose"
import {ISubscribedChannel} from "../types/subscribedChannelType"

const subscribedChannelSchema = new Schema<ISubscribedChannel>({
    name: String,
    devices: [{}]
})

export const SubscribedChannel = model<ISubscribedChannel>("Subscribed Channel", subscribedChannelSchema)
