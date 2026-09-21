import {Router, type Request, type Response} from 'express';
import os from 'os';
import axios from 'axios';
import { env } from '../config/env';

const router = Router()

router.get('/server-ip', async (req: Request, res: Response) => {
    let serverIp = env.serverPublicIp;

    // Try Google Metadata Server first
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
        } catch (e) {
            // Not on Google Cloud or metadata unreachable
        }
    }

    // Try ipify as a backup (user requested)
    if (serverIp === 'unknown') {
        try {
            const response = await axios.get('https://api.ipify.org', { timeout: 2000 });
            serverIp = response.data;
        } catch (e) {
            // All lookups failed
        }
    }

    // Capture Client IP
    let clientIp = req.ip || 'unknown';
    if (clientIp.startsWith('::ffff:')) {
        clientIp = clientIp.substring(7);
    }

    res.json({
        ip: serverIp,
        clientIp: clientIp
    });
});

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