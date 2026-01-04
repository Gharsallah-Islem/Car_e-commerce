// Polyfill for sockjs-client compatibility with Vite/esbuild
// sockjs-client expects 'global' to exist (Node.js environment)
(window as any).global = window;

import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
