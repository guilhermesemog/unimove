import { describe, expect, it } from 'vitest';

import { InterestList, ListStatus } from '../types/interest-list.type';
import { isBookingOpen, parseLocalDate, relativeDateLabel } from './date-time';

const trip: InterestList = {
  id: '00000000-0000-4000-8000-000000000001',
  referenceDate: '2026-08-27',
  closingTime: '16:00:00',
  departureTime: '17:30:00',
  arrivalTime: '19:00:00',
  returnDepartureTime: '23:00:00',
  returnArrivalTime: '00:30:00',
  destination: { id: '00000000-0000-4000-8000-000000000002', name: 'University', address: 'Campus' },
  listStatus: ListStatus.OPEN,
};

describe('date-time utilities', () => {
  it('parses date-only values in local time', () => {
    const result = parseLocalDate('2026-08-27');

    expect(result.getFullYear()).toBe(2026);
    expect(result.getMonth()).toBe(7);
    expect(result.getDate()).toBe(27);
  });

  it('uses contextual English labels', () => {
    const now = new Date(2026, 7, 26, 10, 0);

    expect(relativeDateLabel('2026-08-26', now)).toBe('Today');
    expect(relativeDateLabel('2026-08-27', now)).toBe('Tomorrow');
  });

  it('uses the list status as the source of truth for booking availability', () => {
    expect(isBookingOpen(trip)).toBe(true);
    expect(isBookingOpen({ ...trip, referenceDate: '2020-01-01', closingTime: '00:00:00' })).toBe(true);
    expect(isBookingOpen({ ...trip, listStatus: ListStatus.CLOSED })).toBe(false);
  });
});
