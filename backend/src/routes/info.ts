import {Router, type Request, type Response} from 'express';
import os from 'os';

const router = Router()

router.get('/server-ip', (_req: Request, res: Response) => {
    res.json({ip: process.env.SERVER_PUBLIC_IP || 'unknown'})
    /* const interfaces = os.networkInterfaces()
    let ip = 'Unknown'

    for (const [, addresses] of Object.entries(interfaces)) {
        for (const net of addresses || []) {
            if (!net.internal && net.family === 'IPv4') {
                ip = net.address
                }
            }
        }

    res.json({ip: ip})*/
})

router.get("/server-time", (_req: Request, res: Response) => {
    const now = new Date()
    const hr = String(now.getUTCHours()).padStart(2, '0')
    const min = String(now.getUTCMinutes()).padStart(2, '0')
    const sec = String(now.getUTCSeconds()).padStart(2, '0')

    res.json({time: `${hr}:${min}:${sec} GMT+00:00`})
 })

router.get('/name', (_req: Request, res: Response) => {
    res.json({firstName: 'Kevin', lastName: 'Zhu'})
})


export default router;