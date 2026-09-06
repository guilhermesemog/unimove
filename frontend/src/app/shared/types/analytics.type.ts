export interface AnalyticsMetric {
  value: number | null;
  previousValue: number | null;
  changePercent: number | null;
  unit: 'bookings' | 'percent' | 'hours' | 'trips' | 'demands' | 'conflicts';
  definition: string;
}

export interface AnalyticsPeriod {
  from: string;
  to: string;
  timezone: string;
  generatedAt: string;
  complete: boolean;
  completeSince: string | null;
}

export interface AnalyticsSummary {
  confirmedBookings: AnalyticsMetric;
  cancellationRate: AnalyticsMetric;
  capacityUtilization: AnalyticsMetric;
  planningLeadTime: AnalyticsMetric;
  assignmentLeadTime: AnalyticsMetric;
  tripsAtRisk: AnalyticsMetric;
}

export interface AnalyticsTrendPoint {
  date: string;
  bookingsCreated: number;
  bookingsCancelled: number;
  tripsCreated: number;
}

export interface UniversityAnalytics {
  universityId: string;
  universityName: string;
  bookings: number;
  trips: number;
}

export interface RecurrenceAnalytics {
  activePlans: number;
  generatedOccurrences: AnalyticsMetric;
  coverage: AnalyticsMetric;
  conflictsAvoided: AnalyticsMetric;
}

export interface OperationalExceptions {
  missingDriver: number;
  missingVehicle: number;
  insufficientCapacity: number;
}

export interface OperationalAnalytics {
  period: AnalyticsPeriod;
  summary: AnalyticsSummary;
  trend: AnalyticsTrendPoint[];
  universities: UniversityAnalytics[];
  recurrence: RecurrenceAnalytics;
  exceptions: OperationalExceptions;
}
