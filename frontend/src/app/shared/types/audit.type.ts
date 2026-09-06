export type AuditAction =
  | 'DEMAND_PUBLISHED' | 'DEMAND_UPDATED' | 'DEMAND_STATUS_CHANGED'
  | 'TRIP_CREATED' | 'TRIP_ASSIGNMENT_CHANGED'
  | 'BOOKING_CREATED' | 'BOOKING_REACTIVATED' | 'BOOKING_CANCELLED'
  | 'USER_ACTIVATED' | 'USER_DEACTIVATED'
  | 'RECURRENCE_PLAN_CREATED' | 'RECURRENCE_PLAN_UPDATED'
  | 'RECURRENCE_PLAN_STATUS_CHANGED' | 'RECURRENCE_PLAN_ARCHIVED' | 'RECURRENCE_PLAN_GENERATED';

export interface AuditChange {
  field: string;
  label: string;
  previousValue: string;
  resultingValue: string;
}

export interface AuditEvent {
  id: string;
  actorId: string | null;
  actorName: string;
  actorRole: string;
  action: AuditAction;
  actionLabel: string;
  entityType: string;
  entityId: string;
  occurredAt: string;
  correlationId: string;
  description: string;
  changes: AuditChange[];
}

export interface AuditEventDetail {
  event: AuditEvent;
  previousState: Record<string, unknown>;
  resultingState: Record<string, unknown>;
  metadata: Record<string, unknown>;
}

export interface AuditFilters {
  from?: string;
  to?: string;
  actor?: string;
  action?: AuditAction;
  entityType?: string;
  entityId?: string;
  entityIds?: string[];
  correlationId?: string;
}
