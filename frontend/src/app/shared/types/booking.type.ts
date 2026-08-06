import { BoardingStop } from "./boarding-stop.type";
import { InterestList } from "./interest-list.type";
import { Student } from "./student.type";
import { University } from "./university.type";

export interface Booking {
    id: number;
    tripType: TripType;
    bookingStatus: BookingStatus;
    student: Student;
    interestList: InterestList;
    destination: University;
    boardingLocation: BoardingStop;
}

export interface BookingCreateRequest {
    interestListId: number;
    tripType: TripType;
    destinationId?: number;
    boardingLocationId?: number;
}

export enum TripType {
    OUTBOUND = 'OUTBOUND',
    INBOUND = 'INBOUND',
    ROUND_TRIP = 'ROUND_TRIP',
}

export enum BookingStatus {
    PENDING = 'PENDING',
    APPROVED = 'APPROVED',
    REJECTED = 'REJECTED',
}