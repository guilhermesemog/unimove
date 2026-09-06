import { University } from "./university.type";

export enum ListStatus {
    OPEN = 'OPEN',
    PROCESSING = 'PROCESSING',
    CLOSED = 'CLOSED'
}

export interface InterestList {
    id: string;
    referenceDate: string;
    closingTime: string;
    departureTime: string;
    arrivalTime: string;
    returnDepartureTime: string;
    returnArrivalTime: string;
    destination: University;
    listStatus: ListStatus;
    recurrencePlanId?: string | null;
    occurrenceDate?: string | null;
}

export interface InterestListCreateRequest {
    referenceDate: string;
    closingTime: string;
    departureTime: string;
    arrivalTime: string;
    returnDepartureTime: string;
    returnArrivalTime: string;
    destinationId: string;
}

export interface InterestListUpdateRequest {
    referenceDate: string;
    closingTime: string;
    departureTime: string;
    arrivalTime: string;
    returnDepartureTime: string;
    returnArrivalTime: string;
    destinationId: string;
    listStatus: ListStatus;
}
