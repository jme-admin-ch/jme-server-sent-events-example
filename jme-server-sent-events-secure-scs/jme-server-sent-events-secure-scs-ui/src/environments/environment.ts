import {QdAppSetup, QdAuthConfigServerSide, QdLogLevel} from '@quadrel-enterprise-ui/auth';
import {QdAppEnvironment} from '@quadrel-enterprise-ui/framework';

export const appSetup: QdAppSetup = {
  production: false,
  serviceEndpoint: 'http://localhost:8081/jme-server-sent-events-secure-scs/'
};

// Let the backend configuration control auto-login; a frontend value takes precedence in Quadrel Auth 21.
export const authConfig: QdAuthConfigServerSide = {
  configPathSegment: 'ui-api/configuration/auth',
  clientId: 'jme-server-sent-events-secure-scs',
  systemName: 'jme',
  logLevel: QdLogLevel.Debug,
  renewUserInfoAfterTokenRenew: true,
  silentRenew: true,
  silentRenewUrl: `${window.location.origin}/assets/auth/silent-renew.html`
};

export const appEnvironment: QdAppEnvironment = {
  production: appSetup.production,
  BACKEND_SERVICE_API: appSetup.serviceEndpoint,
  CONFIGURATION_PATH: 'ui-api/configuration/auth'
};
