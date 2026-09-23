import { LogLevel } from '@azure/msal-browser';

const runtimeConfig = window.__env || {};
const config = {
  clientId: runtimeConfig.clientId || process.env.REACT_APP_AZURE_CLIENT_ID || '',
  tenantId: runtimeConfig.tenantId || process.env.REACT_APP_AZURE_TENANT_ID || '',
  redirectUri:
    runtimeConfig.redirectUri ||
    process.env.REACT_APP_AZURE_REDIRECT_URI ||
    window.location.origin,
  apiScope: runtimeConfig.apiScope || process.env.REACT_APP_API_SCOPE || '',
  apiBaseUrl: runtimeConfig.apiBaseUrl || process.env.REACT_APP_API_BASE_URL || '',
};

export const msalConfig = {
  auth: {
    clientId: config.clientId,
    authority: `https://login.microsoftonline.com/${config.tenantId}`,
    redirectUri: config.redirectUri,
    postLogoutRedirectUri: config.redirectUri,
  },
  cache: {
    cacheLocation: 'localStorage',
    storeAuthStateInCookie: false,
  },
  system: {
    loggerOptions: {
      loggerCallback: (level, message, containsPii) => {
        if (containsPii) return;
        console.log(`[MSAL] ${message}`);
      },
      piiLoggingEnabled: false,
      logLevel: LogLevel.Verbose,
    },
  },
};

export const loginRequest = {
  scopes: [config.apiScope],
};

export const apiConfig = {
  baseUrl: config.apiBaseUrl,
  scopes: [config.apiScope],
};
