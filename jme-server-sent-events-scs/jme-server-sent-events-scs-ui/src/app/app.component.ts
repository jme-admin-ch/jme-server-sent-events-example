import {Component, OnDestroy, OnInit} from '@angular/core';
import {QdDialogAuthSessionEndService, QdShellConfig} from '@quadrel-enterprise-ui/framework';
import {QdAuthenticationService, QdConfigService} from '@quadrel-enterprise-ui/auth';
import {VersionService} from './shared/services/version.service';
import {PushEventService} from "./shared/services/pushevent.service";

@Component({
  standalone: false,
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {
  constructor(
    private readonly qdAuthenticationService: QdAuthenticationService,
    private readonly authSupport: QdDialogAuthSessionEndService,
    private readonly qdConfigService: QdConfigService,
    private readonly versionService: VersionService,
    private readonly pushEventService: PushEventService
  ) {}

  qdShellConfig: QdShellConfig = {
    title: {
      i18n: 'i18n.application.title'
    },
    hasSearch: false,
    isInternal: true,
    headerWidget: {
      isDisabled: true
    },
    serviceNavigation: {
      pamsAppId: 'notSet',
      languageList: ['de', 'fr', 'it', 'en'],
      infoLinks: [
        {
          i18n: 'i18n.error.success',
          href: '/success'
        },
        {
          i18n: 'i18n.error.info',
          hrefs: {
            de: '/info/de',
            fr: '/info/fr',
            it: '/info/it',
            en: '/info/en'
          }
        }
      ],
      isInfoActive: true,
      showLanguages: true,
      showNotifications: true,
      showEportalServices: true,
      showProfile: true
    }
  };

  ngOnInit(): void {
    // Subscribe to version changes and update copyright info when the version is fetched
    this.versionService.version$.subscribe(version => {
      if (version) {
        // Only update when version is available
        this.qdShellConfig = {
          ...this.qdShellConfig,
          copyrightInfo: {
            i18n: 'Powered by jEAP and Quadrel - Generated with jEAP Initializer. Version: ' + version,
            showYear: true
          }
        };
      }
    });

    // Trigger the version fetching from the backend
    this.versionService.fetchVersion();

    // Subscribes to the configuration observable and updates the `pamsAppId`
    // in the `qdShellConfig.serviceNavigation` object when a new configuration is received.
    this.qdConfigService.config$.subscribe(config => {
      // @ts-ignore
      this.qdShellConfig.serviceNavigation.pamsAppId = config.pamsAppId;
    });

    // Register the logout handler for the authentication service
    this.qdAuthenticationService.registerBeforeSessionLogoutHandler(this.authSupport.getLogoutHandler());
  }

  ngOnDestroy() {
    this.pushEventService.disconnect();
  }
}
