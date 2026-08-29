import { AdminOperation, OperationAttention } from '../types/admin-operation.type';
import { parseLocalDate } from './date-time';

export function operationAttention(operation: AdminOperation, now = new Date()): OperationAttention[] {
  const attention: OperationAttention[] = [];
  const trip = operation.trip;
  const operationDate = parseLocalDate(operation.demand.referenceDate);
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

  if (!trip && operation.demand.listStatus !== 'OPEN') {
    attention.push({
      kind: 'danger',
      title: 'Trip not generated',
      description: `${operation.bookingCount} bookings are waiting for planning.`,
    });
  }

  if (trip && !trip.conductor) {
    attention.push({
      kind: operationDate.getTime() <= today.getTime() ? 'danger' : 'warning',
      title: 'Driver required',
      description: 'Assign a driver before this operation starts.',
    });
  }

  if (trip?.conductor && trip.conductor.licenseExpirationDate < operation.demand.referenceDate) {
    attention.push({
      kind: 'danger',
      title: 'Driver license invalid for this date',
      description: 'Assign a driver whose license remains valid through the operation date.',
    });
  }

  if (trip && !trip.vehicle) {
    attention.push({
      kind: operationDate.getTime() <= today.getTime() ? 'danger' : 'warning',
      title: 'Vehicle required',
      description: 'Assign a vehicle to confirm capacity.',
    });
  }

  if (trip?.vehicle && operation.bookingCount > trip.vehicle.capacity) {
    attention.push({
      kind: 'danger',
      title: 'Capacity exceeded',
      description: `${operation.bookingCount - trip.vehicle.capacity} passengers exceed the assigned vehicle capacity.`,
    });
  }

  if (!trip && operation.demand.listStatus === 'OPEN' && operationDate.getTime() <= today.getTime()) {
    attention.push({
      kind: 'info',
      title: 'Demand closes today',
      description: 'Review bookings and generate the trip when demand is ready.',
    });
  }

  return attention;
}

export function operationStage(operation: AdminOperation): string {
  if (!operation.trip) return operation.demand.listStatus === 'OPEN' ? 'Bookings Open' : 'Planning';
  if (operation.trip.status === 'COMPLETED') return 'Completed';
  if (operation.trip.status === 'STARTED') return 'In Progress';
  if (operation.trip.conductor && operation.trip.vehicle) return 'Assigned';
  return 'Planning';
}
