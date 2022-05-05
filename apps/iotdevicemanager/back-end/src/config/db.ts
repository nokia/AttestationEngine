import mongoose from 'mongoose'

const url = `${process.env.MONGODB_URL}` || '' //your url here

const connectDB = async () => {
  try {
    const conn = await mongoose.connect(url)
    console.log(`MongoDB connected: ${conn.connection.host}`)
  } catch (error) {
    console.log(error)
    process.exit(1)
  }
}

export default connectDB
