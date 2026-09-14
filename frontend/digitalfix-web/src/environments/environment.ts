export const environment = {
  production: false,
  msal: {
    tenantId: '<TENANT_ID>',
    clientId: '<API_CLIENT_ID>',
    redirectUri: 'http://localhost:4200',
    postLogoutRedirectUri: 'http://localhost:4200',
    scopes: ['api://<API_CLIENT_ID>/access_as_user']
  },
  api: {
    baseUrl: 'http://localhost:8080'
  }
};
