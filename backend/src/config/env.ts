import 'dotenv/config';
import path from 'path';

const rawPort = process.env.PORT;
const port =
  rawPort === undefined || rawPort === ''
    ? 3000
    : Number.parseInt(rawPort, 10);

if (Number.isNaN(port) || port < 1 || port > 65535) {
  throw new Error(`Invalid PORT: ${rawPort}`);
}

export const env = {
  port,
  sslKeyPath: process.env.SSL_KEY_PATH || path.join(__dirname, '../../certs/key.pem'),
  sslCertPath: process.env.SSL_CERT_PATH || path.join(__dirname, '../../certs/cert.pem'),
} as const;
