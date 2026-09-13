import {Router, type Request, type Response} from 'express';
import os from 'os';

const router = Router()

router.get('/server-ip', (_req: Request, res: Response) => {
    const interfaces = os.networkInterfaces
    let ipAddress = ''

    for (const name of Object.keys(interfaces)) {
        for (const net of interfaces[name]) {

            if (!net.internal && net.family === 'IPv4') {
                ipAddress = net.address
                }
            }
        }

    res.json({ipAddress})
    })

router.get("/server-time", (_req: Request, res: Response) => {
    const now = new Date()
    const hr = String(now.getUTCHours()).padStart(2, '0')
    const min = String(now.getUTCHours()).padStart(2, '0')
    const sec = String(now.getUTCHours()).padStart(2, '0')

    res.json({time: `${hr}:${min}:${sec} GMT`})
    })

router.get('/name', (_req: Request, res: Response) => {
    res.json({firstname: 'Place', lastname: 'Holder'})
    })

export default router;