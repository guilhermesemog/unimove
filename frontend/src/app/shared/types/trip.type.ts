import { Vehicle } from './vehicle.type';
import { Conductor } from './conductor.type';
import { InterestList } from './interest-list.type';

export interface Trip {
    id: string;
    status: TripStatus;
    interestList: InterestList;
    conductor: Conductor | null;
    vehicle: Vehicle | null;
}

export interface TripCreateRequest {
    interestListId: string;
}

export interface TripUpdateRequest {
    conductorId?: string;
    vehicleId?: string;
}

export enum TripStatus {
    PROCESSING = 'PROCESSING',
    SCHEDULED = 'SCHEDULED',
    STARTED = 'STARTED',
    COMPLETED = 'COMPLETED',
}
