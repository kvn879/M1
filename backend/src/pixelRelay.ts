import WebSocket from 'ws';
import type { Server as SocketIOServer } from 'socket.io';

const SOCKET_URL_321 = 'wss://8.229.22.124';

export function startPixelRelay(io: SocketIOServer) {
    connect(io)
}

function connect(io: SocketIOServer) {
    const ws = new WebSocket(SOCKET_URL_321,{rejectUnauthorized: false})

    ws.on('open', () =>  {
        console.log("connected to 321 websocket server")
    })

    ws.on("message", (data) => {
        try {
            const pixel = JSON.parse(data.toString())
            io.emit('pixel', pixel)
        } catch (e) {
            console.error('Failed to parse pixel data: ', e)
        }
    })

    ws.on("error", (error) => {
        console.error('WebSocket error: ', error)
    })

    ws.on('close', () => {
        console.log('WebSocket closed - trying to reconnect (2 seconds)')
        setTimeout(() => connect(io), 2000)
    })
}