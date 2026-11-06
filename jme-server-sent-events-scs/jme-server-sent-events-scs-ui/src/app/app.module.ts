import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {QdLanguageModule} from '@quadrel-enterprise-ui/language';
import {APP_ENVIRONMENT, QdFrameworkModule} from '@quadrel-enterprise-ui/framework';
import {StoreModule} from '@ngrx/store';
import {QdAuthModule} from '@quadrel-enterprise-ui/auth';
import {appEnvironment, appSetup, authConfig} from '../environments/environment';

import {registerLocaleData} from '@angular/common';
import localeDECH from '@angular/common/locales/de-CH';
import localeFRCH from '@angular/common/locales/fr-CH';
import localeITCH from '@angular/common/locales/it-CH';
import localeENCH from '@angular/common/locales/en-CH';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {ReactiveFormsModule} from '@angular/forms';
import {PersonModule} from './person/person.module';
import {CoreModule} from './core/core.module';

registerLocaleData(localeDECH);
registerLocaleData(localeENCH);
registerLocaleData(localeFRCH);
registerLocaleData(localeITCH);

@NgModule({
  bootstrap: [AppComponent],
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    StoreModule.forRoot({}),
    QdAuthModule.forRoot(appSetup, authConfig),
    QdFrameworkModule.forRoot(appEnvironment),
    QdLanguageModule.forRoot({
      static: [{prefix: './assets/i18n/qd-ui/'}, {prefix: './assets/i18n/'}]
    }),
    CoreModule,
    PersonModule
  ],
  exports: [QdAuthModule],
  providers: [
    provideHttpClient(withInterceptorsFromDi()),
    {
      provide: APP_ENVIRONMENT,
      useValue: appEnvironment
    }
  ]
})
export class AppModule {}
