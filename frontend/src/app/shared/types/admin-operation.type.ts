import { InterestList } from './interest-list.type';
import { Trip } from './trip.type';

export interface AdminOperation {
  demand: InterestList;
  bookingCount: number;
  trip: Trip | null;
}

export type OperationAttentionKind = 'danger' | 'warning' | 'info';

export interface OperationAttention {
  kind: OperationAttentionKind;
  title: string;
  description: string;
}
