import { bootstrapApplication } from '@angular/platform-browser';
import { MsalRedirectComponent } from '@azure/msal-angular';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error('Error al arrancar AppComponent', err));

bootstrapApplication(MsalRedirectComponent, appConfig)
  .catch((err) => console.error('Error al arrancar MsalRedirectComponent', err));
