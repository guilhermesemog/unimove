export interface Vehicle {
    id: number;
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