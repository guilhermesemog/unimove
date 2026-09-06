import { University } from './university.type';

export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
export type RecurrencePlanStatus = 'ACTIVE' | 'PAUSED' | 'ARCHIVED';
export type RecurrenceOccurrenceAction = 'CREATE' | 'SKIP' | 'CONFLICT';

export interface RecurrencePlanRequest {
  name: string;
  destinationId: string;
  daysOfWeek: DayOfWeek[];
  startDate: string;
  endDate: string;
  closingTime: string;
  departureTime: string;
  arrivalTime: string;
  returnDepartureTime: string;
  returnArrivalTime: string;
  horizonWeeks: number;
}

export interface RecurrencePlan extends Omit<RecurrencePlanRequest, 'destinationId'> {
  id: string;
  destination: University;
  status: RecurrencePlanStatus;
  nextEligibleDate: string | null;
  createdById: string | null;
  futureOccurrences: number;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface RecurrenceOccurrence {
  date: string;
  action: RecurrenceOccurrenceAction;
  interestListId: string | null;
  reason: string | null;
}

export interface RecurrencePreview {
  rangeStart: string;
  rangeEnd: string;
  createCount: number;
  skipCount: number;
  conflictCount: number;
  occurrences: RecurrenceOccurrence[];
}

export interface RecurrenceGeneration {
  planId: string;
  createdCount: number;
  skippedCount: number;
  conflictCount: number;
  occurrences: RecurrenceOccurrence[];
}
