import {QdAppSetup, QdAuthConfigServerSide, QdLogLevel} from '@quadrel-enterprise-ui/auth';
import {QdAppEnvironment} from '@quadrel-enterprise-ui/framework';

export const appSetup: QdAppSetup = {
  production: false,
  serviceEndpoint: 'http://localhost:8080/jme-server-sent-events-scs/'
};

export const authConfig: QdAuthConfigServerSide = {
  configPathSegment: 'ui-api/configuration/auth',
  clientId: 'jme-server-sent-events-scs',
  systemName: 'jme',
  logLevel: QdLogLevel.Debug,
  renewUserInfoAfterTokenRenew: true,
  silentRenew: true,
  silentRenewUrl: `${window.location.origin}/assets/auth/silent-renew.html`,
  useAutoLogin: true
};

export const appEnvironment: QdAppEnvironment = {
  production: appSetup.production,
  BACKEND_SERVICE_API: appSetup.serviceEndpoint,
  CONFIGURATION_PATH: 'ui-api/configuration/auth'
};
