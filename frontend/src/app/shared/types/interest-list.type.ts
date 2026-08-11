import { University } from "./university.type";

export enum ListStatus {
    OPEN = 'OPEN',
    PROCESSING = 'PROCESSING',
    CLOSED = 'CLOSED'
}

export interface InterestList {
    id: number;
    referenceDate: string;
    closingTime: string;
    departureTime: string;
    arrivalTime: string;
    returnDepartureTime: string;
    returnArrivalTime: string;
    destination: University;
    listStatus: ListStatus;
}

export interface InterestListCreateRequest {
    referenceDate: string;
    closingTime: string;
    departureTime: string;
    arrivalTime: string;
    returnDepartureTime: string;
    returnArrivalTime: string;
    destinationId: number;
}

export interface InterestListUpdateRequest {
    referenceDate: string;
    closingTime: string;
    departureTime: string;
    arrivalTime: string;
    returnDepartureTime: string;
    returnArrivalTime: string;
    destinationId: number;
    listStatus: ListStatus;
}