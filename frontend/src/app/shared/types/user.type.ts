export enum UserRole {
    Admin = 'ADMIN',
    Student = 'STUDENT',
    Conductor = 'CONDUCTOR',
}

export interface User {
    id: number;
    cpf: string;
    firstName: string;
    lastName: string;
    phone: string;
    active: boolean;
    role: UserRole;
}

export interface UserProfile {
    id: number;
    firstName: string;
    lastName: string;
    role: UserRole;
}

export interface UserUpdateRequest {
    cpf: string;
    firstName: string;
    lastName: string;
    phone: string;
}