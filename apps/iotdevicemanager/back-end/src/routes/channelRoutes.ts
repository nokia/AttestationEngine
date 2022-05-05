import express from "express"
import {getChannels, setChannel, updateChannel, deleteChannel} from "../controllers/channelController"

export const router = express.Router()

router.route('/')
.get(getChannels)
.post(setChannel)

router.route('/:id')
.delete(deleteChannel)
.put(updateChannel)
