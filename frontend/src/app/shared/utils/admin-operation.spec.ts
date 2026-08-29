import { describe, expect, it } from 'vitest';

import { AdminOperation } from '../types/admin-operation.type';
import { ListStatus } from '../types/interest-list.type';
import { TripStatus } from '../types/trip.type';
import { UserRole } from '../types/user.type';
import { operationAttention, operationStage } from './admin-operation';

const baseOperation: AdminOperation = {
  bookingCount: 20,
  demand: {
    id: 1,
    referenceDate: '2026-08-28',
    closingTime: '16:00:00',
    departureTime: '17:30:00',
    arrivalTime: '19:00:00',
    returnDepartureTime: '23:00:00',
    returnArrivalTime: '00:30:00',
    destination: { id: 1, name: 'University', address: 'Campus' },
    listStatus: ListStatus.CLOSED,
  },
  trip: null,
};

describe('admin operation utilities', () => {
  it('flags closed demand that has no generated trip', () => {
    expect(operationAttention(baseOperation)[0].title).toBe('Trip not generated');
    expect(operationStage(baseOperation)).toBe('Planning');
  });

  it('flags missing assignments and insufficient capacity', () => {
    const operation: AdminOperation = {
      ...baseOperation,
      trip: {
        id: 10,
        status: TripStatus.PROCESSING,
        interestList: baseOperation.demand,
        conductor: null,
        vehicle: { id: 3, plate: 'ABC1D23', capacity: 10 },
      },
    };

    expect(operationAttention(operation).map((attention) => attention.title))
      .toEqual(['Driver required', 'Capacity exceeded']);
  });

  it('treats a fully assigned trip as assigned', () => {
    const operation: AdminOperation = {
      ...baseOperation,
      trip: {
        id: 10,
        status: TripStatus.PROCESSING,
        interestList: baseOperation.demand,
        conductor: {
          user: { id: 4, cpf: '12345678900', firstName: 'Alex', lastName: 'Driver', phone: '11999999999', active: true, role: UserRole.Conductor },
          license: '123',
          licenseExpirationDate: '2027-01-01',
        },
        vehicle: { id: 3, plate: 'ABC1D23', capacity: 30 },
      },
    };

    expect(operationStage(operation)).toBe('Assigned');
    expect(operationAttention(operation)).toEqual([]);
  });
});
