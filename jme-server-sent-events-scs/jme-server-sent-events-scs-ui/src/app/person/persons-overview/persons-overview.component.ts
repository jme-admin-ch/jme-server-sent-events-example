import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  QD_TABLE_DATA_RESOLVER_TOKEN,
  QdNotification,
  QdNotificationsService,
  QdPageConfig,
  QdPageModule,
  QdSectionConfig,
  QdSectionModule,
  QdTableConfig,
  QdTableModule
} from '@quadrel-enterprise-ui/framework';
import {BehaviorSubject, Subscription} from 'rxjs';
import {PersonService} from '../../shared/services/person.service';
import {ActivatedRoute, Router} from '@angular/router';
import {QdAuthorizationService} from '@quadrel-enterprise-ui/auth';
import {writeQdRoleFilter} from '../../shared/common.constants';
import {EventName, PushEventService} from '../../shared/services/pushevent.service';
import {PersonTableDataResolverService} from '../../shared/services/person-table-data-resolver.service';

export type tableColumns = 'lastname' | 'firstname' | 'id';

@Component({
  selector: 'app-persons-overview',
  templateUrl: './persons-overview.component.html',
  styleUrls: ['./persons-overview.component.scss'],
  imports: [CommonModule, QdSectionModule, QdTableModule, QdPageModule],
  providers: [
    {
      provide: QD_TABLE_DATA_RESOLVER_TOKEN,
      useClass: PersonTableDataResolverService
    }
  ],
  standalone: true
})
export class PersonsOverviewComponent implements OnInit, OnDestroy {
  refreshTable$: BehaviorSubject<number> = new BehaviorSubject(0);

  qdPageConfig: QdPageConfig = {
    title: {
      i18n: 'i18n.overview.title'
    },
    pageType: 'overview'
  };

  qdSectionConfig: QdSectionConfig = {
    title: {
      i18n: 'i18n.overview.section.title'
    },
    action: {
      i18n: 'i18n.overview.section.addBtn',
      type: 'addNew'
    }
  };

  qdTableConfig: QdTableConfig<tableColumns> = {
    columns: [
      {
        column: 'firstname',
        type: 'text'
      },
      {
        column: 'lastname',
        type: 'text'
      },
      {
        column: 'id',
        type: 'text'
      }
    ],
    secondaryActions: [
      {
        type: 'delete',
        handler: selectedRow => this.deletePerson(selectedRow)
      }
    ],
    i18ns: 'i18n.overview.table',
    refreshOnPushEvent: true,
    refreshingEvents: ['RESOURCE_CREATED', 'RESOURCE_DELETED'],
    uid: 'persons',
    pagination: {
      pageSizeDefault: 10,
      pageSizes: [10, 20, 50],
      hasFirstLastPageNavigation: false
    },
    refresh: this.refreshTable$
  };

  qdNotificationInsufficientPermissions: QdNotification = {
    type: 'critical',
    i18n: 'i18n.error.insufficientPermissions',
    showAsSnackbar: true
  };

  hasWriteRoleSubscrition$?: Subscription;

  constructor(
    private readonly personService: PersonService,
    private readonly qdAuthorizationService: QdAuthorizationService,
    private readonly qdNotificationsService: QdNotificationsService,
    private readonly pushEventService: PushEventService,
    private readonly notificationService: QdNotificationsService,
    private readonly router: Router,
    private readonly activatedRoute: ActivatedRoute
  ) {}

  hasWriteRole: boolean | undefined;

  ngOnInit(): void {
    this.hasWriteRoleSubscrition$ = this.qdAuthorizationService
      .hasRole(writeQdRoleFilter)
      .subscribe(value => (this.hasWriteRole = value));
    this.pushEventService.connect();
    this.addSnackbarObservationToPushEventService('RESOURCE_CREATED', 'created');
    this.addSnackbarObservationToPushEventService('RESOURCE_UPDATED', 'updated');
    this.addSnackbarObservationToPushEventService('RESOURCE_DELETED', 'deleted');
  }

  addSnackbarObservationToPushEventService(eventName: EventName, text: string): void {
    this.pushEventService.observe(eventName, this.qdTableConfig.uid, (event: MessageEvent) => {
      console.log('Resource updated event received:', event);
      this.notificationService.add('qd-page', {
        type: 'success',
        i18n: 'resource ' + text + ' ' + event.data,
        showAsSnackbar: true,
        lifeTime: 7
      });
    });
  }

  ngOnDestroy(): void {
    this.hasWriteRoleSubscrition$?.unsubscribe();
    this.pushEventService.unobserveAll();
  }

  addNewPerson(): void {
    if (this.hasWriteRole) {
      void this.router.navigate(['create'], {
        relativeTo: this.activatedRoute
      });
    } else {
      this.qdNotificationsService.add('', this.qdNotificationInsufficientPermissions);
    }
  }

  deletePerson($event: any): void {
    if (this.hasWriteRole) {
      this.personService.deletePerson($event.rowData.id).subscribe({
        next: response => {
          console.log('PersonDto deleted successfully:', response);
        },
        error: error => {
          console.error('Error deleting person:', error);
        }
      });
      this.refreshTable(0);
    } else {
      this.qdNotificationsService.add('', this.qdNotificationInsufficientPermissions);
    }
  }

  refreshTable(page: number): void {
    this.refreshTable$.next(page);
  }
}
