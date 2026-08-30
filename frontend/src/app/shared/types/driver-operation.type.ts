import { BoardingStop } from './boarding-stop.type';
import { TripType } from './booking.type';
import { Trip } from './trip.type';

export interface DriverManifestPassenger {
  bookingId: number;
  firstName: string;
  lastName: string;
  phone: string;
  boardingStop: BoardingStop;
  tripType: TripType;
}

export interface DriverOperation {
  trip: Trip;
  passengers: DriverManifestPassenger[];
}

export interface DriverManifestGroup {
  stop: BoardingStop;
  passengers: DriverManifestPassenger[];
}
