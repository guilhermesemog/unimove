export interface InterestList {
    id: number;
    refereceDate: string;
    closingTime: string;
    departureTime: string;
    arrivalTime: string;
    returnDepartureTime: string;
    returnArrivalTime: string;
    destinationId: number;
    listStatus: 'OPEN' | 'PROCESSING' | 'CLOSED';
}