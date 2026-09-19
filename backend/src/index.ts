import { createApp } from './app';
import { env } from './config/env';
import fs from 'fs';
import https from 'https';
import { startPixelRelay } from './pixelRelay';
import { Server as SocketIOServer, Socket } from 'socket.io';

const app = createApp();

const sslOptions = {
    key: fs.readFileSync(env.sslKeyPath),
    cert: fs.readFileSync(env.sslCertPath),
};

const server = https.createServer(sslOptions, app).listen(env.port, () => {
    console.log(`Server listening on port ${env.port}`)
    });

const io = new SocketIOServer(server, {
    cors: {origin: '*'}
})

startPixelRelay(io)

server.listen(env.port, () => {
    console.log(`Server listening on port ${env.port}`)
})

io.on('connection', (socket: Socket) => {
    console.log('Connected: ' + socket.id)
    socket.on('disconnect ', () => console.log('Disconnected: ' + socket.id))
})

for (const signal of ['SIGINT', 'SIGTERM'] as const) {
  process.on(signal, () => {
    server.close(() => {
      process.exit(0);
    });
  });
}
