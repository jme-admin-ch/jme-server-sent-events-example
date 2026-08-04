import {Injectable} from '@angular/core';
import {appSetup} from '../../../environments/environment';
import {QdPushEventsService} from '@quadrel-enterprise-ui/framework';
import {catchError, filter} from 'rxjs/operators';

export type EventName = 'RESOURCE_CREATED' | 'RESOURCE_UPDATED' | 'RESOURCE_DELETED';

@Injectable({
  providedIn: 'root'
})
export class PushEventService {
  private static readonly url: string = appSetup.serviceEndpoint + 'ui-api/sse/events';

  private isConnected = false;

  constructor(private readonly qdPushEventService: QdPushEventsService) {
  }

  async connect() {
    if (!this.isConnected) {
      await this.initializeConnection();
    }
  }

  disconnect() {
    if (this.isConnected) {
      this.qdPushEventService.disconnect();
    }
  }

  observe(eventName: EventName, filterUid: string | undefined, callback: (event: MessageEvent) => void): void {
    this.qdPushEventService
      .observe(eventName)
      .pipe(
        filter(messageEvent => JSON.parse(messageEvent.data).path === filterUid),
        catchError((error, stream) => stream)
      )
      .subscribe(callback);
  }

  unobserveAll(): void {
    this.qdPushEventService.unobserveAll();
  }

  private async initializeConnection() {
    try {
      console.log('Connecting to push events at:', PushEventService.url);
      //this.initEventSource();
      this.qdPushEventService.unobserveAll();
      this.qdPushEventService.connect(PushEventService.url);
      this.isConnected = true;
      console.log('Connected at:', PushEventService.url);
    } catch (error) {
      console.error('Failed to connect to push events:', error);
    }
  }

}
