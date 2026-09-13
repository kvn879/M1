import express, { type Express } from 'express';
import info from './routes/info'

export function createApp(): Express {
  const app = express();
  app.use(cors())
  app.use(express.json())

  app.get('/health', (_req, res) => {
    res.json({ status: 'ok' });
  });

  app.use("/api", info)

  app.use((_req, res) => {
    res.status(404).json({ error: 'Not Found' });
  });

  return app;
}
