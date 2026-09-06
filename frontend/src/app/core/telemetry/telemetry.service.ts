import { HttpClient, HttpContext } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { environment } from '../../../environments/environment.development';
import { SILENT_HTTP_ERROR } from '../interceptors/http-context';

export type TelemetryEventName =
  | 'availability_viewed' | 'booking_started' | 'booking_step_completed'
  | 'booking_validation_failed' | 'booking_completed' | 'booking_abandoned' | 'booking_cancelled'
  | 'recurrence_started' | 'recurrence_previewed' | 'recurrence_conflict_found'
  | 'recurrence_published' | 'trip_generated' | 'assignment_completed'
  | 'operation_opened' | 'manifest_viewed' | 'operation_load_failed';

export type TelemetryScreen = 'availability' | 'booking_checkout' | 'my_trips' | 'recurrence_editor'
  | 'recurrence_list' | 'admin_operation' | 'operation_schedule' | 'operation_detail';
export type TelemetryStep = 'trip' | 'trip_type' | 'boarding_stop' | 'review' | 'schedule'
  | 'dates' | 'times' | 'preview' | 'publish' | 'manifest';
export type TelemetryResult = 'success' | 'failure' | 'abandoned' | 'conflict';
export type TelemetryErrorCategory = 'validation' | 'network' | 'authorization' | 'not_found' | 'server' | 'unknown';

export interface TelemetryProperties {
  screen?: TelemetryScreen;
  step?: TelemetryStep;
  durationMs?: number;
  result?: TelemetryResult;
  errorCategory?: TelemetryErrorCategory;
}

interface QueuedEvent {
  id: string;
  name: TelemetryEventName;
  sessionId: string;
  occurredAt: string;
  properties: TelemetryProperties;
}

@Injectable({ providedIn: 'root' })
export class TelemetryService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = `${environment.apiUrl}/telemetry/events`;
  private readonly sessionId = this.resolveSessionId();
  private readonly queue: QueuedEvent[] = [];
  private readonly flowStarts = new Map<string, number>();
  private flushTimer: ReturnType<typeof setTimeout> | null = null;

  track(name: TelemetryEventName, properties: TelemetryProperties = {}): void {
    this.queue.push({ id: crypto.randomUUID(), name, sessionId: this.sessionId,
      occurredAt: new Date().toISOString(), properties });
    if (this.queue.length >= 10) this.flush();
    else this.scheduleFlush();
  }

  startFlow(flow: 'booking' | 'recurrence' | 'driver_operation' | 'assignment'): void {
    this.flowStarts.set(flow, performance.now());
  }

  elapsed(flow: 'booking' | 'recurrence' | 'driver_operation' | 'assignment', finish = false): number | undefined {
    const startedAt = this.flowStarts.get(flow);
    if (startedAt === undefined) return undefined;
    if (finish) this.flowStarts.delete(flow);
    return Math.max(0, Math.round(performance.now() - startedAt));
  }

  flush(): void {
    if (this.flushTimer) clearTimeout(this.flushTimer);
    this.flushTimer = null;
    if (!this.queue.length) return;
    const events = this.queue.splice(0, 20);
    this.http.post(this.endpoint, { events }, {
      context: new HttpContext().set(SILENT_HTTP_ERROR, true),
    }).subscribe({ error: () => undefined });
    if (this.queue.length) this.scheduleFlush();
  }

  private scheduleFlush(): void {
    if (this.flushTimer) return;
    this.flushTimer = setTimeout(() => this.flush(), 5_000);
  }

  private resolveSessionId(): string {
    const key = 'unimove.telemetry.session';
    const existing = sessionStorage.getItem(key);
    if (existing) return existing;
    const created = crypto.randomUUID();
    sessionStorage.setItem(key, created);
    return created;
  }
}
