import { msalInstance } from '../msal';
import { loginRequest, apiConfig } from '../config/authConfig';

function decodeJwtPayload(token) {
  if (!token) return {};
  const base64Url = token.split('.')[1];
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  const jsonPayload = decodeURIComponent(
    window
      .atob(base64)
      .split('')
      .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
      .join('')
  );
  return JSON.parse(jsonPayload);
}

export function getRoles(account) {
  const acc = account || msalInstance.getActiveAccount();
  const roles = acc?.idTokenClaims?.roles;
  return Array.isArray(roles) ? roles : [];
}

export function hasRole(role) {
  return getRoles().some((r) => r.toLowerCase() === role.toLowerCase());
}

export function hasAnyRole(roles) {
  return roles.some((role) => hasRole(role));
}

export async function getAccessToken() {
  const account = msalInstance.getActiveAccount();
  if (!account) return null;
  const response = await msalInstance.acquireTokenSilent({
    account,
    scopes: apiConfig.scopes,
  });
  return response.accessToken;
}

export async function getScopes() {
  const token = await getAccessToken();
  const claims = decodeJwtPayload(token);
  const scp = claims.scp ?? claims.scopes;
  if (typeof scp === 'string') return scp.split(' ');
  if (Array.isArray(scp)) return scp;
  return [];
}

export function login() {
  return msalInstance.loginPopup(loginRequest);
}

export function logout() {
  return msalInstance.logoutRedirect({
    postLogoutRedirectUri: process.env.REACT_APP_AZURE_REDIRECT_URI,
  });
}
