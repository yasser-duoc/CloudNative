import axios from 'axios';
import { msalInstance } from '../msal';
import { apiConfig, loginRequest } from '../config/authConfig';

const httpClient = axios.create({
  baseURL: apiConfig.baseUrl,
});

httpClient.interceptors.request.use(
  async (config) => {
    const account = msalInstance.getActiveAccount();

    if (!account) {
      await msalInstance.loginRedirect(loginRequest);
      return Promise.reject(new Error('No autenticado'));
    }

    const response = await msalInstance.acquireTokenSilent({
      account,
      scopes: apiConfig.scopes,
    });

    config.headers.Authorization = `Bearer ${response.accessToken}`;
    return config;
  },
  (error) => Promise.reject(error)
);

export default httpClient;
