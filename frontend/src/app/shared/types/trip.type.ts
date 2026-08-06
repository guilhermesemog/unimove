import { Vehicle } from './vehicle.type';
import { Conductor } from './conductor.type';
import { InterestList } from './interest-list.type';

export interface Trip {
    id: number;
    status: TripStatus;
    interestList: InterestList;
    conductor: Conductor;
    vehicle: Vehicle;
}

export interface TripCreateRequest {
    interestListId: number;
}

export interface TripUpdateRequest {
    conductorId?: number;
    vehicleId?: number;
}

export enum TripStatus {
    PROCESSING = 'PROCESSING',
    SCHEDULED = 'SCHEDULED',
    STARTED = 'STARTED',
    COMPLETED = 'COMPLETED',
}