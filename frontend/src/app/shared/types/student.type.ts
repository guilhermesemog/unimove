import { User, UserCommomCreate, UserCreateRequest, UserUpdateRequest } from "./user.type";

export interface Student {
    user: User;
    period: number;
    course: string;
    address: string;
    universityId: number;
    preferredBoardingStopId: number | null;
}

export interface StudentUpdateRequest {
    user: UserUpdateRequest;
    period: number;
    course: string;
    address: string;
    universityId: number;
    preferredBoardingStopId: number | null;
}

export interface StudentCreateRequest {
    user: UserCommomCreate;
    period: number;
    course: string;
    address: string;
    universityId: number;
    preferredBoardingStopId: number | null;
}