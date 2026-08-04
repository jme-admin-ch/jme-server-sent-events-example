import {TestBed} from '@angular/core/testing';
import {QdPushEventsService} from '@quadrel-enterprise-ui/framework';
import {Subject} from 'rxjs';
import {PushEventService} from './pushevent.service';

describe('PushEventService', () => {
  let service: PushEventService;
  let events$: Subject<MessageEvent>;
  let qdPushEventsService: {
    connect: jest.Mock;
    disconnect: jest.Mock;
    observe: jest.Mock;
    unobserveAll: jest.Mock;
  };

  beforeEach(() => {
    events$ = new Subject<MessageEvent>();
    qdPushEventsService = {
      connect: jest.fn(),
      disconnect: jest.fn(),
      observe: jest.fn().mockReturnValue(events$),
      unobserveAll: jest.fn()
    };

    TestBed.configureTestingModule({
      providers: [
        PushEventService,
        {provide: QdPushEventsService, useValue: qdPushEventsService}
      ]
    });

    service = TestBed.inject(PushEventService);
  });

  it('connects to the authenticated SSE endpoint only once', async () => {
    await service.connect();
    await service.connect();

    expect(qdPushEventsService.unobserveAll).toHaveBeenCalledTimes(1);
    expect(qdPushEventsService.connect).toHaveBeenCalledTimes(1);
    expect(qdPushEventsService.connect).toHaveBeenCalledWith(
      expect.stringContaining('ui-api/sse/events')
    );
  });

  it('forwards matching SSE resource events to the UI', () => {
    const callback = jest.fn();
    service.observe('RESOURCE_CREATED', 'persons', callback);

    const event = new MessageEvent('RESOURCE_CREATED', {
      data: JSON.stringify({path: 'persons'})
    });
    events$.next(event);

    expect(qdPushEventsService.observe).toHaveBeenCalledWith('RESOURCE_CREATED');
    expect(callback).toHaveBeenCalledWith(event);
  });

  it('ignores SSE resource events for another UI resource', () => {
    const callback = jest.fn();
    service.observe('RESOURCE_DELETED', 'persons', callback);

    events$.next(
      new MessageEvent('RESOURCE_DELETED', {
        data: JSON.stringify({path: 'orders'})
      })
    );

    expect(callback).not.toHaveBeenCalled();
  });
});
