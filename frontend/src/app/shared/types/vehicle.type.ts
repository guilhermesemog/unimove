export interface Vehicle {
    id: string;
    plate: string;
    capacity: number;
}

export interface VehicleCreateRequest {
    plate: string;
    capacity: number;
}

export interface VehicleUpdateRequest {
    plate: string;
    capacity: number;
}