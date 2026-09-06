import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { SILENT_HTTP_ERROR } from '../interceptors/http-context';
import { TelemetryService } from './telemetry.service';

describe('TelemetryService', () => {
  it('sends an anonymous event batch through the silent HTTP path', () => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const service = TestBed.inject(TelemetryService);
    const http = TestBed.inject(HttpTestingController);

    service.track('booking_completed', {
      screen: 'booking_checkout', result: 'success', durationMs: 850,
    });
    service.flush();

    const request = http.expectOne('http://localhost:8080/telemetry/events');
    expect(request.request.method).toBe('POST');
    expect(request.request.context.get(SILENT_HTTP_ERROR)).toBe(true);
    expect(request.request.body.events).toHaveLength(1);
    expect(request.request.body.events[0]).toMatchObject({
      name: 'booking_completed', properties: { screen: 'booking_checkout', result: 'success', durationMs: 850 },
    });
    expect(request.request.body.events[0].userId).toBeUndefined();
    request.flush({ accepted: 1 });
    http.verify();
  });
});
