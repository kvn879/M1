import {Router, type Request, type Response} from 'express';
import os from 'os';
import axios from 'axios';
import { env } from '../config/env';

const router = Router()

router.get('/server-ip', async (_req: Request, res: Response) => {
    let serverIp = env.serverPublicIp;

    if (serverIp === 'unknown') {
        try {
            const response = await axios.get(
              'http://metadata.google.internal/computeMetadata/v1/instance/network-interfaces/0/access-configs/0/external-ip',
              {
                headers: { 'Metadata-Flavor': 'Google' },
                timeout: 1000,
              }
            );
            serverIp = response.data;
          } catch (error) {
            // Silently fail metadata check
          }
    }

    const clientIp = _req.ip || 'unknown';

    res.json({
        ip: serverIp,
        clientIp: clientIp
    });
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