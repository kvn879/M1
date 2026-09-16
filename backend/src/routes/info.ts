import {Router, type Request, type Response} from 'express';
import os from 'os';

const router = Router()

router.get('/server-ip', (_req: Request, res: Response) => {
    const interfaces = os.networkInterfaces
    let ip = 'Unknown'

    for (const [, addresses] of Object.entries(interfaces)) {
        for (const net of addresses) {
            if (!net.internal && net.family === 'IPv4') {
                ip = net.address
                }
            }
        }

    res.json({ip: ip})
})

router.get("/server-time", (_req: Request, res: Response) => {
    const now = new Date()
    const timeStr = now.toUTCString().split(' ')[4]
    const offsetMinutes = now.getTimezoneOffset();
    const absOffset = Math.abs(offsetMinutes);
    const offsetHours = Math.floor(absOffset / 60);
    const offsetMins = absOffset % 60;
    const sign = offsetMinutes <= 0 ? "+" : "-";

    res.json({time: `${timeStr} GMT${sign}${String(offsetHours).padStart(2, '0')}:${String(offsetMins).padStart(2, '0')}`
    })

router.get('/name', (_req: Request, res: Response) => {
    res.json({firstName: 'Place', lastName: 'Holder'})
    })
})
export default router;