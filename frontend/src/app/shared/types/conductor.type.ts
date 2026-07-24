import { User, UserUpdateRequest } from "./user.type";

export interface Conductor {
    user: User;
    license: string;
    licenseExpirationDate: string;
}

export interface ConductorUpdateRequest {
    user: UserUpdateRequest;
    license: string;
    licenseExpirationDate: string;
}