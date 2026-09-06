export interface University {
    id: string;
    name: string;
    address: string;
}

export interface UniversityCreateRequest {
    name: string;
    address: string;
}

export interface UniversityUpdateRequest {
    name: string;
    address: string;
}